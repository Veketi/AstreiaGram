package com.astreiagram.userservice.service;

import com.astreiagram.userservice.dto.AuthDtos.*;
import com.astreiagram.userservice.entity.User;
import com.astreiagram.userservice.exception.InvalidTokenException;
import com.astreiagram.userservice.exception.UserAlreadyExistsException;
import com.astreiagram.userservice.repository.UserRepository;
import com.astreiagram.userservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username is already in use");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email is already registered");
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .bio(request.getBio())
                .build();

        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(savedUser, savedUser.getId());

        log.info("Novo usuário registrado: {}", savedUser.getUsername());

        return buildAuthResponse(token, savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        String token = jwtUtil.generateToken(user, user.getId());

        log.info("Login bem-sucedido: {}", user.getUsername());

        return buildAuthResponse(token, user);
    }

    @Transactional(readOnly = true)
    public ValidateTokenResponse validateToken(String token) {
        String username;

        try {
            username = jwtUtil.extractUsername(token);
        } catch (Exception e) {
            log.warn("Token mal formado ou assinatura inválida: {}", e.getMessage());
            throw new InvalidTokenException("Invalid or malformed token");
        }

        UserDetails userDetails;

        try {
            userDetails = userDetailsService.loadUserByUsername(username);
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid token: user not found");
        }

        if (!jwtUtil.isTokenValid(token, userDetails)) {
            throw new InvalidTokenException("Invalid or expired token");
        }

        return ValidateTokenResponse.builder()
                .userId(jwtUtil.extractUserId(token))
                .username(username)
                .build();
    }

    private AuthResponse buildAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }
}