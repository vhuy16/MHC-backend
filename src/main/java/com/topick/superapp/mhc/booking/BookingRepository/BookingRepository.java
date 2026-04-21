package com.topick.superapp.mhc.booking.BookingRepository;

import com.topick.superapp.mhc.enums.BookingStatus;
import com.topick.superapp.mhc.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime time);
}
