package com.topick.superapp.mhc.booking.BookingService;

import com.topick.superapp.mhc.ApiResponse;
import com.topick.superapp.mhc.booking.Dto.CreatePaymentRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.exception.PayOSException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import java.util.Random;

@Service
public class PayOsService {

    @Autowired
    private PayOS payOS;
    public CreatePaymentLinkResponse CreatePaymentLink(CreatePaymentRequest createPaymentRequest){
        String description = "Dat lich BS " + createPaymentRequest.getDoctorName();
        description = description.length() > 25 ? description.substring(0, 25) : description;
        long orderCode = System.currentTimeMillis() % 1000000000000L * 1000 + new Random().nextInt(1000);
    CreatePaymentLinkRequest paymentData =
            CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .amount(createPaymentRequest.getAmount())
                    .description(description)
                    .returnUrl("https://your-url.com/success")
                    .cancelUrl("https://your-url.com/cancel")
                    .build();
    try{
        CreatePaymentLinkResponse response = payOS.paymentRequests().create(paymentData);
        return response;
    }
    catch (PayOSException payOSException){
        throw new RuntimeException("PayOS error: " + payOSException.getMessage());
    }


    }
}
