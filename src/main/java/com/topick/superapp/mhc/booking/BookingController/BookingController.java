package com.topick.superapp.mhc.booking.BookingController;

import com.topick.superapp.mhc.ApiResponse;
import com.topick.superapp.mhc.booking.BookingService.BookingService;
import com.topick.superapp.mhc.booking.BookingService.PaymentService;
import com.topick.superapp.mhc.booking.Dto.CreateBookingRequest;
import com.topick.superapp.mhc.enums.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {
@Autowired
private PaymentService paymentService;
@Autowired
private BookingService bookingService;
//    @PostMapping("/webhook")
//    public ResponseEntity<Map<String, Object>> handleWebhook(@RequestBody String body) {
//        try {
//            // 1. Gọi service xử lý logic (verify signature, update DB...)
//            paymentService.handleWebhook(body);
//
//            // 2. Nếu mọi thứ suôn sẻ, trả về 200 OK
//            return ResponseEntity.ok(Map.of("success", true, "message", "Webhook processed"));
//        } catch (Exception e) {
//            // 3. LOG LỖI RA CONSOLE ĐỂ DEBUG (Rất quan trọng)
//            System.err.println("Webhook Error: " + e.getMessage());
//            e.printStackTrace();
//
//        /* 4. CHIẾN THUẬT QUAN TRỌNG:
//           Luôn trả về 200 OK cho PayOS kể cả khi gặp lỗi xử lý bên trong.
//           Điều này giúp bạn vượt qua bước "Lưu URL" và tránh việc PayOS retry liên tục
//           khi dữ liệu không khớp (như trường hợp dữ liệu test hoặc giao dịch không tồn tại).
//        */
//            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
//        }
//    }
@PostMapping("/webhook")
public ResponseEntity<Map<String, Object>> handleWebhook(@RequestBody String body) {
    try {
        paymentService.handleWebhook(body);
        return ResponseEntity.ok(Map.of("success", true, "message", "Webhook processed"));

    } catch (Exception e) {
        // Lỗi hệ thống (DB down, unexpected)
        // → 500 để PayOS retry
        System.err.println("Webhook system error: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.internalServerError()
                .body(Map.of("erro", false, "message", "System error"));
    }
}
    @PostMapping()
    public ResponseEntity<ApiResponse> createBooking(@RequestBody CreateBookingRequest createBookingRequest) {
        try {
            ApiResponse response = bookingService.CreateBooking(createBookingRequest);
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false, null));
        }
    }
    // API Get All / Filter theo Status
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ApiResponse> getDoctorBookings(
            @PathVariable UUID doctorId,
            @RequestParam(required = false) BookingStatus status) {
        try {
            ApiResponse response = bookingService.getDoctorBookings(doctorId, status);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false, null));
        }
    }

    // API Get Detail
    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse> getBookingDetail(@PathVariable UUID bookingId) {
        try {
            ApiResponse response = bookingService.getBookingDetail(bookingId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false, null));
        }
    }
}
