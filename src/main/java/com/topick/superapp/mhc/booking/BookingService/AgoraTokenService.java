package com.topick.superapp.mhc.booking.BookingService;

import com.topick.superapp.mhc.enums.CallSessionRole;
import io.agora.media.RtcTokenBuilder2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;
@Service
public class AgoraTokenService {

    @Value("${agora.app-id}")
    private String appId;

    @Value("${agora.app-certificate}")
    private String appCertificate;

    public String generateToken(String channelName, UUID userId, CallSessionRole role) {
        // TTL 90 phút tính từ now
        int expireTs = (int)(System.currentTimeMillis() / 1000) + 5400;

        // Agora UID dùng int — hash userId
        int uid = Math.abs(userId.hashCode());

        RtcTokenBuilder2 token = new RtcTokenBuilder2();
        return token.buildTokenWithUid(
                appId,
                appCertificate,
                channelName,
                uid,
                RtcTokenBuilder2.Role.ROLE_PUBLISHER,
                expireTs,
                expireTs
        );
    }
}