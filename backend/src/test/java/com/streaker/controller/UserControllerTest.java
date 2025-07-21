package com.streaker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.config.JwtService;
import com.streaker.controller.user.dto.CreateUserDto;
import com.streaker.controller.user.dto.LoginUserDto;
import com.streaker.controller.user.dto.UserResponseDto;
import com.streaker.exception.ResourceNotFoundException;
import com.streaker.service.UserService;
import com.streaker.utlis.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testadmin", roles = {"USER"})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtService jwtService;

    private UUID userId;
    private UserResponseDto userDto;
    private CreateUserDto createUserDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userDto = new UserResponseDto(userId, "john", "john@example.com", Role.USER);
        createUserDto = new CreateUserDto("john", "john@example.com", "password12345");
    }

    @Test
    void getAllUsers_shouldReturnList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(userDto));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("john"));
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        when(userService.getUserById(userId)).thenReturn(userDto);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"));
    }

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        when(userService.createUser(any())).thenReturn(userDto);

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(createUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"));
    }

    @Test
    void getUserById_shouldReturn404_whenUserNotFound() throws Exception {
        when(userService.getUserById(userId))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found"))
                .andExpect(jsonPath("$.path").value("/users/" + userId));
    }

    @Test
    void createUser_shouldReturn400_whenInvalidInput() throws Exception {
        when(userService.createUser(any()))
                .thenThrow(new IllegalArgumentException("Invalid email"));

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(createUserDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid email"))
                .andExpect(jsonPath("$.path").value("/users/register"));
    }

    @Test
    void getAllUsers_shouldReturn500_whenServiceFails() throws Exception {
        when(userService.getAllUsers())
                .thenThrow(new RuntimeException("Database crash"));

        mockMvc.perform(get("/users"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Database crash"))
                .andExpect(jsonPath("$.path").value("/users"));
    }

    @Test
    void createUser_shouldReturn400_whenUsernameIsMissing() throws Exception {
        String invalidJson = """
            {
                "username": "",
                "email": "invalid@example.com",
                "password": "password1234"
            }
        """;

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Username is required")));
    }

    @Test
    void login_shouldReturnJwtToken() throws Exception {
        LoginUserDto loginDto = new LoginUserDto("john@example.com", "password12345");
        String expectedToken = "mocked.jwt.token";

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "john",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null);

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generateToken(userDetails)).thenReturn(expectedToken);

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(expectedToken));
    }


}
