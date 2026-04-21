package com.topick.superapp.mhc.booking;

import com.topick.superapp.mhc.booking.BookingRepository.BookingRepository;
import com.topick.superapp.mhc.doctorAvailability.Repository.DoctorAvailabilityRepository;
import com.topick.superapp.mhc.enums.BookingStatus;
import com.topick.superapp.mhc.model.Booking;
import com.topick.superapp.mhc.model.DoctorAvailability;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Transactional
public class BookingScheduler {
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private DoctorAvailabilityRepository doctorAvailabilityRepository;
    @Scheduled(fixedRate = 60000) // chạy mỗi 1 phút
    public void cancelExpiredBookings() {
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(3);
        List<Booking> expiredBookings = bookingRepository
                .findByStatusAndCreatedAtBefore(BookingStatus.PENDING, expiredTime);
        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.getAvailability().setStatus("AVAILABLE");
            bookingRepository.save(booking);
            doctorAvailabilityRepository.save(booking.getAvailability());

        }
    }
}
