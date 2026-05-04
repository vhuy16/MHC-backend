package com.topick.superapp.mhc.booking.BookingRepository;

import com.topick.superapp.mhc.enums.CallSessionRole;
import com.topick.superapp.mhc.enums.CallSessionsStatus;
import com.topick.superapp.mhc.model.CallSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CallSessionRepository extends JpaRepository<CallSession, UUID> {

    // =========================================================================
    // USE CASE 1: Check patient đã join chưa (cho complete endpoint)
    // =========================================================================
    /**
     * Trả về true nếu đã tồn tại record của Booking này với role cụ thể (VD: "PATIENT").
     * Dùng exists... sẽ tối ưu hiệu năng hơn find... vì nó dùng "SELECT 1" thay vì SELECT toàn bộ data.
     */
    boolean existsByBookingIdAndRole(UUID bookingId, CallSessionRole role);

    // Nếu lúc complete bạn cần lấy nguyên object CallSession ra để update status/leftAt:
    List<CallSession> findByBookingIdAndRole(UUID bookingId, CallSessionRole role);


    // =========================================================================
    // USE CASE 2: Check caller đã join chưa (cho renew-token endpoint)
    // =========================================================================
    /**
     * Tìm chính xác session của một user (bác sĩ hoặc bệnh nhân) trong một cuộc gọi.
     * Trả về Optional để bạn dễ dàng throw Exception (VD: User chưa join nên không cho renew).
     */
    Optional<CallSession> findByBookingIdAndUserId(UUID bookingId, UUID userId);

    // Nếu chỉ cần check true/false để validate:
    boolean existsByBookingIdAndUserId(UUID bookingId, UUID userId);


    // =========================================================================
    // USE CASE 3: Scheduled job - Tìm sessions JOINED quá giờ
    // =========================================================================
    /**
     * Lấy danh sách các session đang có status "JOINED" nhưng joinedAt đã quá một khoảng thời gian.
     * Cách dùng ở Service:
     * LocalDateTime threshold = LocalDateTime.now().minusMinutes(60); // Quá 60 phút
     * repository.findByStatusAndJoinedAtBefore("JOINED", threshold);
     */
    List<CallSession> findByStatusAndJoinedAtBefore(CallSessionRole status, LocalDateTime threshold);

    // Nếu bạn muốn job này query danh sách để "đóng" luôn các phòng không ai vào (status INITIATED quá hạn):
    // List<CallSession> findByStatusAndJoinedAtBefore(String status, LocalDateTime threshold);
    // -> Hàm trên vẫn dùng lại được cho case này!
    List<CallSession> findAllByStatus(CallSessionsStatus status);
}