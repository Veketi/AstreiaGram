package com.astreiagram.userservice;

import com.astreiagram.userservice.config.JwtProperties;
import com.astreiagram.userservice.dto.AuthDtos.*;
import com.astreiagram.userservice.entity.User;
import com.astreiagram.userservice.exception.InvalidTokenException;
import com.astreiagram.userservice.exception.UserAlreadyExistsException;
import com.astreiagram.userservice.repository.UserRepository;
import com.astreiagram.userservice.security.JwtUtil;
import com.astreiagram.userservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;
    private AuthenticationManager authenticationManager;
    private UserDetailsService userDetailsService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository       = mock(UserRepository.class);
        authenticationManager = mock(AuthenticationManager.class);
        userDetailsService   = mock(UserDetailsService.class);

        passwordEncoder = new Argon2PasswordEncoder(16, 32, 1, 4096, 2);

        JwtProperties props = new JwtProperties();
        props.setSecret("test-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256");
        props.setExpirationMs(3600000L);
        props.setServiceSecret("test-service-secret");

        jwtUtil     = new JwtUtil(props);
        authService = new AuthService(userRepository, passwordEncoder, jwtUtil,
                authenticationManager, userDetailsService);
    }

    @Test
    void register_shouldCreateUserAndReturnToken() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("joao");
        req.setEmail("joao@email.com");
        req.setPassword("Senha@123");
        req.setBio("Bio aqui");

        when(userRepository.existsByUsername("joao")).thenReturn(false);
        when(userRepository.existsByEmail("joao@email.com")).thenReturn(false);

        User saved = User.builder().id(UUID.randomUUID()).username("joao").email("joao@email.com")
                .password("hashed").active(true).build();
        when(userRepository.save(any())).thenReturn(saved);

        AuthResponse response = authService.register(req);

        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getUsername()).isEqualTo("joao");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    void register_shouldThrowWhenUsernameExists() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("joao");
        req.setEmail("joao@email.com");
        req.setPassword("Senha@123");

        when(userRepository.existsByUsername("joao")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username");
    }

    @Test
    void login_shouldReturnTokenOnValidCredentials() {
        LoginRequest req = new LoginRequest();
        req.setUsername("joao");
        req.setPassword("Senha@123");

        User user = User.builder().id(UUID.randomUUID()).username("joao").email("joao@email.com")
                .password("hashed").active(true).build();

        when(userRepository.findByUsername("joao")).thenReturn(Optional.of(user));

        AuthResponse response = authService.login(req);

        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getUserId()).isEqualTo(user.getId());
    }

    @Test
    void login_shouldThrowOnBadCredentials() {
        LoginRequest req = new LoginRequest();
        req.setUsername("joao");
        req.setPassword("senhaErrada");

        doThrow(new BadCredentialsException("bad creds"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void validateToken_shouldReturnDataForGoodToken() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id).username("maria").active(true).build();
        String token = jwtUtil.generateToken(user, id);

        when(userDetailsService.loadUserByUsername("maria")).thenReturn(user);

        ValidateTokenResponse response = authService.validateToken(token);

        assertThat(response.getUserId()).isEqualTo(id);
        assertThat(response.getUsername()).isEqualTo("maria");
    }

    @Test
    void validateToken_shouldThrowForBadToken() {
        assertThatThrownBy(() -> authService.validateToken("token.invalido.aqui"))
                .isInstanceOf(InvalidTokenException.class);
    }
}
