package com.topick.superapp.mhc.booking.BookingService;

import com.topick.superapp.mhc.auth.repository.UserRepository;
import com.topick.superapp.mhc.booking.BookingRepository.BookingRepository;
import com.topick.superapp.mhc.booking.BookingRepository.CallSessionRepository;
import com.topick.superapp.mhc.booking.Dto.JoinCallResponse;
import com.topick.superapp.mhc.enums.BookingStatus;
import com.topick.superapp.mhc.enums.CallSessionRole;
import com.topick.superapp.mhc.enums.CallSessionsStatus;
import com.topick.superapp.mhc.enums.CompletedBy;
import com.topick.superapp.mhc.model.Booking;
import com.topick.superapp.mhc.model.CallSession;
import com.topick.superapp.mhc.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class CallService {

    private final CallSessionRepository callSessionRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final AgoraTokenService agoraTokenService;
    @Transactional
    public JoinCallResponse joinCall(UUID bookingId, UUID currentUserId) {
        // 1. Kiểm tra Booking tồn tại và trạng thái
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

        if (booking.getStatus() != BookingStatus.CONFIRMED) { // THIẾU 1 & 3: Phải CONFIRMED và không CANCELLED
            throw new RuntimeException("Lịch hẹn không ở trạng thái hợp lệ để tham gia cuộc gọi");
        }

        // 2. Time window check (startTime - 5p <= now <= endTime + 15p)
        validateTimeWindow(booking);

        // 3. Xác định Role
        CallSessionRole role;
        if (booking.getDoctor().getId().equals(currentUserId)) {
            role = CallSessionRole.DOCTOR;
        } else if (booking.getPatient().getId().equals(currentUserId)) {
            role = CallSessionRole.PATIENT;
        } else {
            throw new RuntimeException("Bạn không có quyền tham gia cuộc gọi này");
        }

        // 4. LUÔN TẠO MỚI SESSION (Không reuse)[cite: 1]
        User user = userRepository.getReferenceById(currentUserId);

        CallSession newSession = new CallSession();
        newSession.setBooking(booking);
        newSession.setUser(user);
        newSession.setRole(role);
        newSession.setStatus(CallSessionsStatus.JOINED); // Hoặc dùng Enum CallSessionStatus.JOINED
        newSession.setJoinedAt(LocalDateTime.now()); // NOT NULL theo thiết kế SQL

        callSessionRepository.save(newSession);

        String channelName = booking.getAgoraChannelName().toString();

// Generate token
        String token = agoraTokenService.generateToken(channelName, currentUserId, role);

// Dùng token thật trong response
        return JoinCallResponse.builder()
                .channelName(channelName)
                .agoraToken(token)  // không phải "mock-token-here"
                .role(role.name())
                .build();
    }

    private void validateTimeWindow(Booking booking) {
        var availability = booking.getAvailability();
        LocalDateTime now = LocalDateTime.now();

        // Kiểm tra đúng ngày
        if (!now.toLocalDate().equals(availability.getDate())) {
            throw new RuntimeException("Hôm nay không phải ngày hẹn");
        }

        // Window: -5p đến +15p
        LocalDateTime startWindow = LocalDateTime.of(availability.getDate(), availability.getStartTime()).minusMinutes(5);
        LocalDateTime endWindow = LocalDateTime.of(availability.getDate(), availability.getEndTime()).plusMinutes(15);

        if (now.isBefore(startWindow)) {
            throw new RuntimeException("Còn quá sớm để tham gia cuộc gọi");
        }
        if (now.isAfter(endWindow)) {
            throw new RuntimeException("Cuộc gọi đã kết thúc hoặc quá giờ tham gia");
        }
    }
    @Transactional(readOnly = true)
    public JoinCallResponse renewToken(UUID bookingId, UUID currentUserId) {
        // 1. Kiểm tra Booking tồn tại và trạng thái
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

        if (booking.getStatus() != BookingStatus.CONFIRMED){
            throw new RuntimeException("Lịch hẹn không khả dụng");
        }
        validateRenewTimeWindow(booking);
        // 2. Kiểm tra định danh (User có thuộc Booking này không)
        CallSessionRole role;
        if (booking.getDoctor().getId().equals(currentUserId)) {
            role = CallSessionRole.DOCTOR;
        } else if (booking.getPatient().getId().equals(currentUserId)) {
            role = CallSessionRole.PATIENT;
        } else {
            throw new RuntimeException("Bạn không có quyền trong cuộc gọi này");
        }

        // 3. USE CASE 2: Kiểm tra caller đã từng thực hiện JOIN chưa
        // Nếu chưa có bất kỳ session nào, nghĩa là họ chưa bao giờ nhấn Join thành công
        boolean hasJoinedBefore = callSessionRepository.existsByBookingIdAndUserId(bookingId, currentUserId);
        if (!hasJoinedBefore) {
            throw new RuntimeException("Bạn phải tham gia cuộc gọi (Join) trước khi làm mới token");
        }

        // 4. Tạo token mới (Logic token giống hệt bên join)
        String channelName = booking.getAgoraChannelName().toString();
        String newToken = agoraTokenService.generateToken(channelName, currentUserId, role);

        return JoinCallResponse.builder()
                .channelName(channelName)
                .agoraToken(newToken)
                .role(role.name())
                .build();
    }
    private void validateRenewTimeWindow(Booking booking) {
        var availability = booking.getAvailability();
        LocalDateTime now = LocalDateTime.now();

        // Lấy thời điểm kết thúc slot khám
        LocalDateTime slotEndDateTime = LocalDateTime.of(availability.getDate(), availability.getEndTime());

        // Deadline là endTime + 30 phút
        LocalDateTime deadline = slotEndDateTime.plusMinutes(30);

        if (now.isAfter(deadline)) {
            throw new RuntimeException("Đã quá thời gian gia hạn cuộc gọi (giới hạn 30 phút sau khi kết thúc)");
        }

        // Lưu ý: Không cần check startWindow vì muốn renew thì chắc chắn họ đã qua bước joinCall trước đó rồi.
    }
    @Transactional
    public void completeCall(UUID bookingId, UUID currentUserId) {
        // 1. Kiểm tra Booking tồn tại
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

        // 2. Kiểm tra quyền: Chỉ Bác sĩ của lịch hẹn này mới có quyền "Kết thúc" khám
        if (!booking.getDoctor().getId().equals(currentUserId)) {
            throw new RuntimeException("Chỉ bác sĩ phụ trách mới có quyền kết thúc lịch hẹn này");
        }

        // 3. Kiểm tra trạng thái: Chỉ kết thúc được khi đang ở trạng thái CONFIRMED
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Lịch hẹn không ở trạng thái hợp lệ để kết thúc");
        }

        // 4. USE CASE 1: Kiểm tra Patient đã từng join chưa để ghi nhận No-Show
        boolean patientJoined = callSessionRepository.existsByBookingIdAndRole(bookingId, CallSessionRole.PATIENT);
        if (!patientJoined) {
            booking.setNoShowPatient(true); // Ghi nhận bệnh nhân không đến
        }

        // 5. Cập nhật thông tin hoàn tất cho Booking
        booking.setStatus(BookingStatus.DONE);

        // completed_by là VARCHAR(10), lưu 10 ký tự đầu của UUID người thực hiện

        booking.setCompletedBy(CompletedBy.DOCTOR);

        bookingRepository.save(booking);


    }
}