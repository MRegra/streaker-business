package com.streaker.config;

import com.streaker.model.User;
import com.streaker.repository.UserRepository;
import com.streaker.utlis.enums.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldReturnUserDetails_whenUserExists() {
        User user = new User();
        user.setUsername("testadmin");
        user.setEmail("admin@test.com");
        user.setPassword("hashedpassword");
        user.setRole(Role.ADMIN);

        when(userRepository.findByUsername("testadmin")).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername("testadmin");

        assertEquals("testadmin", result.getUsername());
        assertEquals("hashedpassword", result.getPassword());
        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void shouldThrowException_whenUserNotFound() {
        when(userRepository.findByUsername("testadmin")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("testadmin"));
    }
}
