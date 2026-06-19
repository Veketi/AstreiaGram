package com.astreiagram.userservice.controller;

import com.astreiagram.userservice.dto.AuthDtos.*;
import com.astreiagram.userservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints de cadastro, login e validação de token JWT")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @SecurityRequirements // sobrescreve a segurança global: este endpoint é público
    @Operation(
            summary = "Cadastrar novo usuário",
            description = "Cria uma nova conta de usuário. Endpoint público (não requer autenticação). " +
                    "A senha é armazenada como hash Argon2id e nunca em texto plano. " +
                    "Em caso de sucesso, já retorna um token JWT válido, dispensando login imediato após o cadastro."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso, token JWT retornado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (ex.: senha fraca, email mal formatado, corpo ausente)"),
            @ApiResponse(responseCode = "409", description = "Username ou email já cadastrados")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @SecurityRequirements // público
    @Operation(
            summary = "Autenticar usuário",
            description = "Realiza login com username e senha, retornando um token JWT válido " +
                    "para ser usado no header Authorization das próximas requisições."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login bem-sucedido, token JWT retornado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou corpo da requisição ausente"),
            @ApiResponse(responseCode = "401", description = "Username ou senha incorretos")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/validate")
    @SecurityRequirements // público — a segurança vem do próprio token sendo validado
    @Operation(
            summary = "Validar token JWT",
            description = "Valida um token JWT enviado via header Authorization (Bearer Token). " +
                    "Destinado ao uso interno pelos demais microsserviços (Post Service, Feed Service) " +
                    "para autenticar requisições de forma centralizada antes de processá-las. " +
                    "Não recebe nada no corpo da requisição — o token deve vir exclusivamente no header."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token válido — retorna o ID e username do usuário dono do token"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido, mal formado ou expirado")
    })
    public ResponseEntity<ValidateTokenResponse> validate(
            @Parameter(
                    description = "Token JWT no formato 'Bearer {token}'",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2FvIn0.abc123..."
            )
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = extractBearerToken(authorizationHeader);
        return ResponseEntity.ok(authService.validateToken(token));
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new com.astreiagram.userservice.exception.InvalidTokenException(
                    "Header Authorization ausente ou em formato inválido. Esperado: 'Bearer {token}'"
            );
        }
        return authorizationHeader.substring(7);
    }
}
