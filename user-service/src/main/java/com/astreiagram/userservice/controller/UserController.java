package com.astreiagram.userservice.controller;

import com.astreiagram.userservice.dto.UserDtos.*;
import com.astreiagram.userservice.entity.User;
import com.astreiagram.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "bearerAuth") // todos os endpoints deste controller exigem JWT no header Authorization
@Tag(name = "Usuários", description = "Endpoints de perfil e relacionamento de seguidores entre usuários")
public class UserController {

        private final UserService userService;

        public UserController(UserService userService) {
                this.userService = userService;
        }

        @GetMapping("/{userId}")
        @Operation(summary = "Buscar usuário por ID", description = "Retorna o perfil público de um usuário a partir do seu UUID. "
                        +
                        "Requer token JWT válido no header Authorization (Bearer Token). " +
                        "Usado, por exemplo, pelo Post Service para exibir os dados do autor de um post.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Perfil do usuário retornado com sucesso"),
                        @ApiResponse(responseCode = "400", description = "UUID em formato inválido"),
                        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
                        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
        })
        public ResponseEntity<UserProfileResponse> getUserById(
                        @Parameter(description = "UUID do usuário", example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID userId) {
                return ResponseEntity.ok(userService.getUserProfile(userId));
        }

        @GetMapping("/me")
        @Operation(summary = "Buscar perfil próprio", description = "Retorna o perfil completo do usuário autenticado (identificado pelo token JWT enviado). "
                        +
                        "Requer token JWT válido no header Authorization (Bearer Token).")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Perfil retornado com sucesso"),
                        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
        })
        public ResponseEntity<UserProfileResponse> getMyProfile(@AuthenticationPrincipal User currentUser) {
                return ResponseEntity.ok(userService.getUserProfile(currentUser.getId()));
        }

        @PatchMapping("/me")
        @Operation(summary = "Atualizar perfil próprio", description = "Atualiza a biografia e/ou a URL do avatar do usuário autenticado. "
                        +
                        "Campos não enviados (null) permanecem inalterados. " +
                        "Requer token JWT válido no header Authorization (Bearer Token).")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso"),
                        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
        })
        public ResponseEntity<UserProfileResponse> updateMyProfile(
                        @AuthenticationPrincipal User currentUser,
                        @Valid @RequestBody UpdateProfileRequest request) {
                return ResponseEntity.ok(userService.updateProfile(currentUser.getId(), request));
        }

        @GetMapping("/{userId}/exists")
        @SecurityRequirements // público
        @Operation(summary = "Verificar se um usuário existe", description = "Endpoint de uso interno entre microsserviços. Retorna apenas o status HTTP "
                        +
                        "(200 se existe, 404 se não existe), sem corpo com dados sensíveis. " +
                        "Usado pelo Post Service e Feed Service para validar um userId antes de criar " +
                        "posts, comentários ou curtidas, sem precisar expor dados pessoais do usuário.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Usuário existe"),
                        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
        })
        public ResponseEntity<Void> checkUserExists(
                        @Parameter(description = "UUID do usuário", example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID userId) {
                userService.assertUserExists(userId);
                return ResponseEntity.ok().build();
        }

        @GetMapping("/{userId}/followers")
        @Operation(summary = "Listar seguidores de um usuário", description = "Retorna a lista de usuários que seguem o usuário informado. "
                        +
                        "Requer token JWT válido no header Authorization (Bearer Token).")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lista de seguidores retornada com sucesso"),
                        @ApiResponse(responseCode = "400", description = "UUID em formato inválido"),
                        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
                        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
        })
        public ResponseEntity<FollowListResponse> getFollowers(
                        @Parameter(description = "UUID do usuário", example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID userId) {
                return ResponseEntity.ok(userService.getFollowers(userId));
        }

        @GetMapping("/{userId}/following")
        @Operation(summary = "Listar usuários seguidos", description = "Retorna a lista de usuários que o usuário informado está seguindo. "
                        +
                        "Requer token JWT válido no header Authorization (Bearer Token).")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
                        @ApiResponse(responseCode = "400", description = "UUID em formato inválido"),
                        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
                        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
        })
        public ResponseEntity<FollowListResponse> getFollowing(
                        @Parameter(description = "UUID do usuário", example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID userId) {
                return ResponseEntity.ok(userService.getFollowing(userId));
        }

        @PostMapping("/{userId}/followers")
        @Operation(summary = "Seguir um usuário", description = "O usuário autenticado passa a seguir o usuário identificado por userId. "
                        +
                        "Operação idempotente: chamar novamente não gera erro nem duplicidade. " +
                        "Requer token JWT válido no header Authorization (Bearer Token).")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "Usuário seguido com sucesso (ou já era seguido)"),
                        @ApiResponse(responseCode = "400", description = "Tentativa de seguir a si mesmo"),
                        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
                        @ApiResponse(responseCode = "404", description = "Usuário a ser seguido não encontrado")
        })
        public ResponseEntity<Void> follow(
                        @AuthenticationPrincipal User currentUser,
                        @Parameter(description = "UUID do usuário a ser seguido", example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID userId) {
                userService.follow(currentUser.getId(), userId);
                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/{userId}/followers")
        @Operation(summary = "Deixar de seguir um usuário", description = "O usuário autenticado deixa de seguir o usuário identificado por userId. "
                        +
                        "Operação idempotente: chamar mesmo sem seguir não gera erro. " +
                        "Requer token JWT válido no header Authorization (Bearer Token).")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "Deixou de seguir com sucesso"),
                        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
                        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
        })
        public ResponseEntity<Void> unfollow(
                        @AuthenticationPrincipal User currentUser,
                        @Parameter(description = "UUID do usuário a deixar de seguir", example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID userId) {
                userService.unfollow(currentUser.getId(), userId);
                return ResponseEntity.noContent().build();
        }
}
