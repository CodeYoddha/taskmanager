package com.taskmanager.service;

import com.taskmanager.dto.AuthResponse;
import com.taskmanager.dto.RegisterRequest;
import com.taskmanager.entity.Role;
import com.taskmanager.entity.User;
import com.taskmanager.exception.ApiException;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.CustomUserDetailsService;
import com.taskmanager.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_savesNewUser_andReturnsTokens() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Ada Lovelace");
        request.setEmail("ada@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("ada@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userDetailsService.loadUserByUsername("ada@example.com")).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("refresh-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getEmail()).isEqualTo("ada@example.com");
        assertThat(response.getRole()).isEqualTo(Role.MEMBER.name());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_throwsConflict_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Ada Lovelace");
        request.setEmail("ada@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("ada@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any());
    }
}
