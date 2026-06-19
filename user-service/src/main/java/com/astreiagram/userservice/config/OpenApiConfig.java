package com.astreiagram.userservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * Configuração geral da documentação OpenAPI/Swagger do User Service.
 *
 * Acesse a interface interativa em /swagger-ui.html e o JSON cru em /v3/api-docs.
 */
@OpenAPIDefinition(
        info = @Info(
                title = "AstreiaGram — User Service API",
                version = "1.0.0",
                description = """
                        Microsserviço responsável pelo cadastro, autenticação e gerenciamento de perfis \
                        de usuários da rede social AstreiaGram.

                        Principais responsabilidades:
                        - Cadastro e login de usuários, com senhas protegidas via Argon2id.
                        - Emissão e validação de tokens JWT.
                        - Gerenciamento de perfil (bio, avatar).
                        - Relacionamento de seguidores entre usuários.

                        O endpoint de validação de token (/api/auth/validate) é consumido pelos demais \
                        microsserviços (Post Service, Feed Service) para autenticar requisições de forma \
                        centralizada.
                        """,
                contact = @Contact(name = "Equipe AstreiaGram")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Ambiente local")
        },
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token JWT obtido em /api/auth/login ou /api/auth/register. " +
                "Envie como: Authorization: Bearer {token}"
)
public class OpenApiConfig {
}
