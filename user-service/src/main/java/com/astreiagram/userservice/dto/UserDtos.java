package com.astreiagram.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class UserDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Perfil completo de um usuário")
    public static class UserProfileResponse {

        @Schema(description = "Identificador único (UUID) do usuário")
        private UUID id;

        @Schema(description = "Nome de usuário", example = "joaosilva")
        private String username;

        @Schema(description = "Endereço de e-mail do usuário", example = "joao@email.com")
        private String email;

        @Schema(description = "Biografia do usuário", example = "Apaixonado por fotografia")
        private String bio;

        @Schema(description = "URL da foto de perfil do usuário")
        private String avatarUrl;

        @Schema(description = "Data e hora de criação da conta")
        private LocalDateTime createdAt;

        @Schema(description = "Quantidade de seguidores deste usuário", example = "128")
        private long followerCount;

        @Schema(description = "Quantidade de usuários que este usuário está seguindo", example = "57")
        private long followingCount;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Versão resumida de um usuário, usada em listagens (ex.: lista de seguidores)")
    public static class UserSummary {

        @Schema(description = "Identificador único (UUID) do usuário")
        private UUID id;

        @Schema(description = "Nome de usuário", example = "joaosilva")
        private String username;

        @Schema(description = "URL da foto de perfil do usuário")
        private String avatarUrl;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Lista de usuários (seguidores ou seguindo) com a contagem total")
    public static class FollowListResponse {

        @Schema(description = "Lista resumida dos usuários")
        private List<UserSummary> users;

        @Schema(description = "Quantidade total de usuários na lista", example = "42")
        private int total;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Dados para atualização do próprio perfil")
    public static class UpdateProfileRequest {

        @Schema(description = "Nova biografia do usuário", example = "Desenvolvedor e fotógrafo amador")
        private String bio;

        @Schema(description = "Nova URL da foto de perfil do usuário")
        private String avatarUrl;
    }
}
