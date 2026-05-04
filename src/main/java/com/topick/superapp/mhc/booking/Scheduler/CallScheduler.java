package com.topick.superapp.mhc.booking.Scheduler;

import com.topick.superapp.mhc.booking.BookingRepository.BookingRepository;
import com.topick.superapp.mhc.booking.BookingRepository.CallSessionRepository;
import com.topick.superapp.mhc.enums.BookingStatus;
import com.topick.superapp.mhc.enums.CallSessionRole;
import com.topick.superapp.mhc.enums.CallSessionsStatus;
import com.topick.superapp.mhc.enums.CompletedBy;
import com.topick.superapp.mhc.model.Booking;
import com.topick.superapp.mhc.model.CallSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CallScheduler {

    private final CallSessionRepository callSessionRepository;
    private final BookingRepository bookingRepository;

    /**
     * JOB 1: Auto-close các CallSession vẫn ở trạng thái JOINED sau khi slot khám kết thúc.
     * Chạy mỗi 10 phút một lần.
     */
    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void autoCloseJoinedSessions() {
        log.info("Bắt đầu Job 1: Auto-close các JOINED sessions quá giờ slot");

        LocalDateTime now = LocalDateTime.now();
        LocalTime nowTime = now.toLocalTime();

        // 1. Tìm các session vẫn đang JOINED
        // Lưu ý: Source [1] đang để tham số là CallSessionRole status, hãy đảm bảo status truyền vào đúng
        // Ở đây giả định bạn dùng String hoặc Enum Status cho tham số này
        List<CallSession> joinedSessions = callSessionRepository.findAllByStatus(CallSessionsStatus.JOINED);

        for (CallSession session : joinedSessions) {
            Booking booking = session.getBooking();
            LocalTime endTime = booking.getAvailability().getEndTime();

            // Nếu giờ hiện tại đã quá giờ kết thúc slot (cộng thêm 15p grace period)
            if (nowTime.isAfter(endTime)) {
                session.setStatus(CallSessionsStatus.DISCONNECTED);
                session.setLeftAt(now);
                log.info("Auto-close session ID: {} cho Booking: {}", session.getId(), booking.getId());
            }
        }
        callSessionRepository.saveAll(joinedSessions);
    }

    /**
     * JOB 2: Auto-complete các Booking CONFIRMED quá 30 phút sau slot_end.
     * Chạy mỗi 15 phút một lần.
     */
    @Scheduled(cron = "0 */15 * * * *")
    @Transactional
    public void autoCompleteExpiredBookings() {
        log.info("Bắt đầu Job 2: Auto-complete các Booking CONFIRMED quá hạn");

        LocalDateTime now = LocalDateTime.now();

        // Tìm các booking vẫn đang CONFIRMED
        List<Booking> confirmedBookings = bookingRepository.findAllByStatus(BookingStatus.CONFIRMED);

        for (Booking booking : confirmedBookings) {
            var availability = booking.getAvailability();
            LocalDateTime slotEndDateTime = LocalDateTime.of(availability.getDate(), availability.getEndTime());

            // Nếu đã quá 30 phút sau khi slot kết thúc mà bác sĩ chưa nhấn Complete
            if (now.isAfter(slotEndDateTime.plusMinutes(30))) {

                // Kiểm tra Patient đã từng join chưa để ghi nhận No-Show (Sử dụng Use Case 1 của Repository [1])
                boolean patientJoined = callSessionRepository.existsByBookingIdAndRole(booking.getId(), CallSessionRole.PATIENT);
                boolean doctorJoined = callSessionRepository.existsByBookingIdAndRole(booking.getId(), CallSessionRole.DOCTOR);

                if (!patientJoined && !doctorJoined) {
                    booking.setStatus(BookingStatus.MISSED);
                } else {
                    booking.setStatus(BookingStatus.DONE);
                    booking.setCompletedBy(CompletedBy.SYSTEM);
                    booking.setNoShowPatient(!patientJoined);
                }
            }
        }
        bookingRepository.saveAll(confirmedBookings);
    }
}