package com.topick.superapp.mhc.booking.BookingController;

import com.topick.superapp.mhc.Util.SecurityUtils;
import com.topick.superapp.mhc.booking.BookingService.CallService;
import com.topick.superapp.mhc.booking.Dto.JoinCallResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/calls")
@RequiredArgsConstructor
public class CallController {

    private final CallService callService;

    @PostMapping("/join/{bookingId}")
    public ResponseEntity<JoinCallResponse> joinCall(
            @PathVariable UUID bookingId
    ) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        JoinCallResponse response = callService.joinCall(bookingId, currentUserId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/renew-token/{bookingId}")
    public ResponseEntity<JoinCallResponse> renewToken(
            @PathVariable UUID bookingId
            /* @AuthenticationPrincipal UserDetailsImpl userDetails */
    ) {
        // Tạm mốc UserId để test
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        JoinCallResponse response = callService.renewToken(bookingId, currentUserId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/complete/{bookingId}")
    public ResponseEntity<String> completeCall(
            @PathVariable UUID bookingId
            /* @AuthenticationPrincipal UserDetailsImpl userDetails */
    ) {
        // Tạm mốc UserId của Bác sĩ để test
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        callService.completeCall(bookingId, currentUserId);
        return ResponseEntity.ok("Cuộc gọi đã được kết thúc và ghi nhận thành công.");
    }
}