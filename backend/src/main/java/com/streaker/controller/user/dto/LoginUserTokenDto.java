package com.streaker.controller.user.dto;

public record LoginUserTokenDto(
        String accessToken,
        String refreshToken
) {}
