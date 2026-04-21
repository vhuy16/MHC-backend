package com.topick.superapp.mhc.booking.Dto;

import com.topick.superapp.mhc.model.Booking;
import lombok.Data;

import java.util.UUID;
@Data
public class CreateBookingRequest {
    private UUID avai_ID;
}
