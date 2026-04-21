package com.topick.superapp.mhc.booking.BookingRepository;

import com.topick.superapp.mhc.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository  extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByTransactionId(String transactionId);
}
