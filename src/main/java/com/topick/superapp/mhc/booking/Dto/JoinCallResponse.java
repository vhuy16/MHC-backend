package com.topick.superapp.mhc.booking.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JoinCallResponse {
    private String channelName;
    private String agoraToken;
    private String role;
}