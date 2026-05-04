package com.topick.superapp.mhc.booking.BookingController;

import com.topick.superapp.mhc.ApiResponse;
import com.topick.superapp.mhc.Util.SecurityUtils;
import com.topick.superapp.mhc.booking.BookingRepository.BookingRepository;
import com.topick.superapp.mhc.booking.BookingService.ChatService;
import com.topick.superapp.mhc.exception.BusinessException;
import com.topick.superapp.mhc.model.Booking;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final BookingRepository bookingRepository;
    /**
     * Lấy thông tin cuộc hội thoại hoặc tạo mới nếu chưa có.
     * Thường dùng khi bắt đầu mở màn hình chat.
     */
    @GetMapping("/conversation/{bookingId}")
    public ResponseEntity<ApiResponse> getConversation(@PathVariable UUID bookingId) {
        ApiResponse response = chatService.getOrCreateConversation(bookingId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gửi tin nhắn mới.
     * Body truyền vào cần content và senderType (ví dụ: "PATIENT" hoặc "DOCTOR").
     */
    @PostMapping("/send/{bookingId}")
    public ResponseEntity<ApiResponse> sendMessage(
            @PathVariable UUID bookingId,
            @RequestBody Map<String, String> request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Booking not found"));
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        String content = request.get("content");
        String senderType = booking.getDoctor().getId().equals(currentUserId)
                ? "DOCTOR" : "PATIENT";
        ApiResponse response = chatService.sendMessage(bookingId, content, senderType);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách toàn bộ tin nhắn của một lịch hẹn.
     */
    @GetMapping("/messages/{bookingId}")
    public ResponseEntity<ApiResponse> getMessages(@PathVariable UUID bookingId) {
        ApiResponse response = chatService.getMessages(bookingId);
        return ResponseEntity.ok(response);
    }
}