package com.streaker.controller.auth.dto;

public record AuthTokensResponse(
        String accessToken,
        String refreshToken
) {
}
