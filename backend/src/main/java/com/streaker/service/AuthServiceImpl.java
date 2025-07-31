package com.streaker.service;

import com.streaker.config.JwtService;
import com.streaker.controller.auth.dto.RefreshTokenRequest;
import com.streaker.controller.auth.dto.AuthTokensResponse;
import com.streaker.controller.user.dto.UserResponseDto;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserService userService;

    public ResponseEntity<?> refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        if (refreshToken == null) {
            return ResponseEntity.badRequest().body("Missing refresh token");
        }

        try {
            String username = jwtService.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (!jwtService.isTokenValid(refreshToken, userDetails)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
            }

            UserResponseDto userResponseDto = userService.getUserByUsername(username);

            String newAccessToken = jwtService.generateToken(userDetails, userResponseDto);
            String newRefreshToken = jwtService.generateRefreshToken(userDetails);

            return ResponseEntity.ok(new AuthTokensResponse(newAccessToken, refreshToken));

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token expired");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }
}
