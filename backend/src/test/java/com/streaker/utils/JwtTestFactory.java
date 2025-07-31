package com.streaker.utils;

import com.streaker.config.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collections;
import java.util.Date;

@Component
public class JwtTestFactory {

    @Autowired
    private JwtService jwtService;

    public static String createExpiredRefreshToken(String username, String secret) {
        UserDetails userDetails = new User(username, "", Collections.emptyList());

        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now - 2 * 60 * 60 * 1000); // 2 hours ago
        Date expiration = new Date(now - 60 * 60 * 1000);    // 1 hour ago

        return generateRefreshToken(userDetails, issuedAt, expiration, secret);
    }

    private static Key getSigningKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private static String generateRefreshToken(UserDetails user, Date issuedAt, Date expiration, String secret) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(getSigningKey(secret), SignatureAlgorithm.HS256)
                .compact();
    }
}
