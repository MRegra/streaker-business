package com.streaker.controller.user;

import com.streaker.annotations.CustomRateLimit;
import com.streaker.config.JwtAuthorizationValidator;
import com.streaker.config.JwtService;
import com.streaker.controller.user.dto.CreateUserDto;
import com.streaker.controller.user.dto.LoginUserDto;
import com.streaker.controller.user.dto.LoginUserTokenDto;
import com.streaker.controller.user.dto.UserResponseDto;
import com.streaker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "User API", description = "Operations related to users")
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final JwtAuthorizationValidator jwtAuthorizationValidator;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private final UserService userService;

    @Operation(summary = "Get all users")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers(@RequestHeader("Authorization") String authHeader) {
        jwtAuthorizationValidator.validateToken(authHeader);
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Get a user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id) {
        jwtAuthorizationValidator.validateToken(authHeader, id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Create a new user")
    @PostMapping("register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody CreateUserDto userDto) {
        return ResponseEntity.ok(userService.createUser(userDto));
    }

    @Operation(summary = "Authenticate and return JWT")
    @CustomRateLimit(limit = 5, windowSeconds = 60)
    @PostMapping("login")
    public ResponseEntity<LoginUserTokenDto> login(@Valid @RequestBody LoginUserDto loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username(),
                        loginRequest.password()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserResponseDto user = userService.getUserByUsername(loginRequest.username());
        String authorizationToken = jwtService.generateToken(userDetails, user);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return ResponseEntity.ok(new LoginUserTokenDto(authorizationToken, refreshToken));
    }

}
