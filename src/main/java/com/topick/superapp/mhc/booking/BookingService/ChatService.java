package com.topick.superapp.mhc.booking.BookingService;

import com.topick.superapp.mhc.ApiResponse;
import com.topick.superapp.mhc.Util.SecurityUtils;
import com.topick.superapp.mhc.booking.BookingRepository.BookingRepository;
import com.topick.superapp.mhc.model.Booking;
import com.topick.superapp.mhc.enums.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final WebClient supabaseClient;
    private final BookingRepository bookingRepository;

    public ApiResponse getOrCreateConversation(UUID bookingId) {
        // 1. Verify booking tồn tại
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return new ApiResponse("Booking not found", false, null);
        }

        Booking booking = bookingOpt.get();
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        // Kiểm tra quyền truy cập
        boolean isPatient = booking.getPatient().getId().equals(currentUserId);
        boolean isDoctor = booking.getDoctor().getId().equals(currentUserId);

        if (!isPatient && !isDoctor) {
            return new ApiResponse("Access denied", false, null);
        }

        // Kiểm tra trạng thái booking
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return new ApiResponse("Chat disabled for cancelled booking", false, null);
        }

        // 2. Check conversation đã tồn tại chưa
        Map[] existing = supabaseClient.get()
                .uri("/rest/v1/chat_conversation?booking_id=eq." + bookingId + "&select=*")
                .retrieve()
                .bodyToMono(Map[].class)
                .block();

        if (existing != null && existing.length > 0) {
            return new ApiResponse("Success", true, existing[0]);
        }

        // 3. Tạo mới nếu chưa có
        Map[] created = supabaseClient.post()
                .uri("/rest/v1/chat_conversation")
                .header("Prefer", "return=representation")
                .bodyValue(Map.of("booking_id", bookingId.toString()))
                .retrieve()
                .bodyToMono(Map[].class)
                .block();

        if (created != null && created.length > 0) {
            return new ApiResponse("Conversation created", true, created[0]);
        }

        return new ApiResponse("Failed to create conversation", false, null);
    }

    public ApiResponse sendMessage(UUID bookingId, String content, String senderType) {
        ApiResponse convRes = getOrCreateConversation(bookingId);
        if (!convRes.isSuccess()) {
            return convRes; // Trả về lỗi nếu không lấy được conversation
        }

        Map conversation = (Map) convRes.getData();
        String conversationId = conversation.get("id").toString();
        UUID senderId = SecurityUtils.getCurrentUserId();

        Map[] messageResult = supabaseClient.post()
                .uri("/rest/v1/chat_message")
                .header("Prefer", "return=representation")
                .bodyValue(Map.of(
                        "conversation_id", conversationId,
                        "sender_id", senderId.toString(),
                        "sender_type", senderType,
                        "content", content
                ))
                .retrieve()
                .bodyToMono(Map[].class)
                .block();

        if (messageResult != null && messageResult.length > 0) {
            return new ApiResponse("Message sent", true, messageResult[0]);
        }

        return new ApiResponse("Failed to send message", false, null);
    }

    public ApiResponse getMessages(UUID bookingId) {
        ApiResponse convRes = getOrCreateConversation(bookingId);
        if (!convRes.isSuccess()) {
            return convRes; // Trả về lỗi nếu không có quyền xem
        }

        Map conversation = (Map) convRes.getData();
        String conversationId = conversation.get("id").toString();

        Map[] messages = supabaseClient.get()
                .uri("/rest/v1/chat_message?conversation_id=eq."
                        + conversationId + "&order=sent_at.asc&select=*")
                .retrieve()
                .bodyToMono(Map[].class)
                .block();

        return new ApiResponse("Success", true, messages);
    }
}