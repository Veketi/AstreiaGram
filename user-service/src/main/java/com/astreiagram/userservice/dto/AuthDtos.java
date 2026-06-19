package com.astreiagram.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

public class AuthDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    @Schema(description = "Dados necessários para criar uma nova conta de usuário")
    public static class RegisterRequest {

        @Schema(description = "Nome de usuário único, usado para login e identificação pública",
                example = "joaosilva", minLength = 3, maxLength = 50)
        @NotBlank(message = "Username é obrigatório")
        @Size(min = 3, max = 50, message = "Username deve ter entre 3 e 50 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username só pode conter letras, números e underscore (_)")
        private String username;

        @Schema(description = "Endereço de e-mail único do usuário", example = "joao@email.com")
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email em formato inválido")
        private String email;

        @Schema(description = "Senha de acesso. Deve ter no mínimo 8 caracteres, contendo pelo menos " +
                "1 letra maiúscula, 1 letra minúscula, 1 número e 1 caractere especial.",
                example = "Senha@123", minLength = 8)
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, max = 128, message = "Senha deve ter no mínimo 8 caracteres")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).+$",
                message = "Senha deve conter ao menos 1 letra maiúscula, 1 minúscula, 1 número e 1 caractere especial"
        )
        private String password;

        @Schema(description = "Biografia opcional exibida no perfil do usuário", example = "Apaixonado por fotografia")
        private String bio;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    @Schema(description = "Credenciais para autenticação de um usuário já cadastrado")
    public static class LoginRequest {

        @Schema(description = "Nome de usuário cadastrado", example = "joaosilva")
        @NotBlank(message = "Username é obrigatório")
        private String username;

        @Schema(description = "Senha de acesso", example = "Senha@123")
        @NotBlank(message = "Senha é obrigatória")
        private String password;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Resposta de autenticação contendo o token JWT e dados básicos do usuário")
    public static class AuthResponse {

        @Schema(description = "Token JWT a ser usado no header Authorization das próximas requisições",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2FvIn0.abc123...")
        private String token;

        @Schema(description = "Tipo do token, sempre 'Bearer'", example = "Bearer")
        private String tokenType;

        @Schema(description = "Identificador único (UUID) do usuário autenticado")
        private UUID userId;

        @Schema(description = "Nome de usuário do usuário autenticado", example = "joaosilva")
        private String username;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Resposta da validação de um token JWT, usada por outros microsserviços")
    public static class ValidateTokenResponse {

        @Schema(description = "Identificador único (UUID) do usuário dono do token")
        private UUID userId;

        @Schema(description = "Nome de usuário do dono do token", example = "joaosilva")
        private String username;
    }
}
