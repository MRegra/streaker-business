package com.streaker.service;

import com.streaker.controller.auth.dto.RefreshTokenRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<?> refreshToken(RefreshTokenRequest request);
}
