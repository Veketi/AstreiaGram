package com.astreiagram.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Argon2id — parâmetros OWASP (saltLength=16, hashLength=32, parallelism=2, memory=4MB, iterations=4)
        return new Argon2PasswordEncoder(16, 32, 2, 4096, 4);
    }
}
