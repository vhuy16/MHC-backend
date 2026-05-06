package com.topick.superapp.mhc.booking.BookingRepository;

import com.topick.superapp.mhc.enums.BookingStatus;
import com.topick.superapp.mhc.model.Booking;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime time);
    List<Booking> findAllByStatus(BookingStatus status);
    @Query("SELECT b FROM Booking b WHERE b.doctor.id = :doctorId " +
            "AND (:status IS NULL OR b.status = :status) " +
            "ORDER BY b.availability.date ASC, b.availability.startTime ASC")
    List<Booking> findDoctorBookingsWithFilter(@Param("doctorId") UUID doctorId,
                                               @Param("status") BookingStatus status);
}
