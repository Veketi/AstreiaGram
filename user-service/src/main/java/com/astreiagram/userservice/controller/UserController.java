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
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuários", description = "Endpoints de perfil e relacionamento de seguidores entre usuários")
public class UserController {

        private final UserService userService;

        public UserController(UserService userService) {
                this.userService = userService;
        }

        /*
         * BUSCAR PERFIL POR UUID
         */

        @GetMapping("/{userId}")
        @Operation(summary = "Buscar usuário por ID", description = """
                        Retorna o perfil de um usuário a partir do UUID.
                        Requer token JWT válido no header Authorization.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Perfil retornado com sucesso"),
                        @ApiResponse(responseCode = "400", description = "UUID em formato inválido"),
                        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
                        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
        })
        public ResponseEntity<UserProfileResponse> getUserById(
                        @Parameter(description = "UUID do usuário", example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID userId) {
                return ResponseEntity.ok(
                                userService.getUserProfile(userId));
        }

        /*
         * BUSCAR PERFIL POR USERNAME
         */

        @GetMapping("/username/{username}")
        @Operation(summary = "Buscar usuário por username", description = """
                        Retorna o perfil de um usuário a partir do username,
                        sem a necessidade de informar o UUID.
                        Requer token JWT válido no header Authorization.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Perfil retornado com sucesso"),
                        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
                        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
        })
        public ResponseEntity<UserProfileResponse> getUserByUsername(
                        @Parameter(description = "Nome de usuário utilizado no cadastro", example = "joaosilva") @PathVariable String username) {
                return ResponseEntity.ok(
                                userService.getUserProfileByUsername(username));
        }

        /*
         * PERFIL DO USUÁRIO AUTENTICADO
         */

        @GetMapping("/me")
        @Operation(summary = "Buscar perfil próprio", description = """
                        Retorna o perfil do usuário autenticado,
                        identificado pelo token JWT.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Perfil retornado com sucesso"),
                        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
        })
        public ResponseEntity<UserProfileResponse> getMyProfile(
                        @AuthenticationPrincipal User currentUser) {
                return ResponseEntity.ok(
                                userService.getUserProfile(currentUser.getId()));
        }

        /*
         * ATUALIZAR PERFIL
         */

        @PatchMapping("/me")
        @Operation(summary = "Atualizar perfil próprio", description = """
                        Atualiza a biografia e/ou a URL do avatar do
                        usuário autenticado.

                        Campos não enviados permanecem inalterados.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso"),
                        @ApiResponse(responseCode = "400", description = "Dados enviados são inválidos"),
                        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
        })
        public ResponseEntity<UserProfileResponse> updateMyProfile(
                        @AuthenticationPrincipal User currentUser,
                        @Valid @RequestBody UpdateProfileRequest request) {
                return ResponseEntity.ok(
                                userService.updateProfile(
                                                currentUser.getId(),
                                                request));
        }

        /*
         * VERIFICAR EXISTÊNCIA POR UUID
         */

        @GetMapping("/{userId}/exists")
        @SecurityRequirements
        @Operation(summary = "Verificar usuário por ID", description = """
                        Endpoint interno para verificar se um usuário existe.

                        Retorna 200 quando o usuário existe e 404 quando
                        não existe.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Usuário existe"),
                        @ApiResponse(responseCode = "400", description = "UUID em formato inválido"),
                        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
        })
        public ResponseEntity<Void> checkUserExists(
                        @Parameter(description = "UUID do usuário", example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID userId) {
                userService.assertUserExists(userId);

                return ResponseEntity.ok().build();
        }

        /*
         * VERIFICAR EXISTÊNCIA POR USERNAME
         */

        @GetMapping("/username/{username}/exists")
        @SecurityRequirements
        @Operation(summary = "Verificar usuário por username", description = """
                        Endpoint interno para verificar se um usuário
                        existe a partir do username.

                        Retorna 200 quando existe e 404 quando não existe.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Usuário existe"),
                        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
        })
        public ResponseEntity<Void> checkUserExistsByUsername(
                        @Parameter(description = "Username do usuário", example = "joaosilva") @PathVariable String username) {
                userService.assertUserExistsByUsername(username);

                return ResponseEntity.ok().build();
        }

        /*
         * LISTAR SEGUIDORES POR UUID
         */

        @GetMapping("/{userId}/followers")
        @Operation(summary = "Listar seguidores por ID", description = """
                        Retorna os usuários que seguem o usuário
                        identificado pelo UUID.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Seguidores retornados com sucesso"),
                        @ApiResponse(responseCode = "400", description = "UUID em formato inválido"),
                        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
                        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
        })
        public ResponseEntity<FollowListResponse> getFollowers(
                        @Parameter(description = "UUID do usuário", example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID userId) {
                return ResponseEntity.ok(
                                userService.getFollowers(userId));
        }

        /*
         * LISTAR SEGUIDORES POR USERNAME
         */

        @GetMapping("/username/{username}/followers")
        @Operation(summary = "Listar seguidores por username", description = """
                        Retorna os usuários que seguem o usuário
                        identificado pelo username.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Seguidores retornados com sucesso"),
                        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
                        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
        })
        public ResponseEntity<FollowListResponse> getFollowersByUsername(
                        @Parameter(description = "Username do usuário", example = "joaosilva") @PathVariable String username) {
                return ResponseEntity.ok(
                                userService.getFollowersByUsername(username));
        }

        /*
         * LISTAR USUÁRIOS SEGUIDOS POR UUID
         */

        @GetMapping("/{userId}/following")
        @Operation(summary = "Listar usuários seguidos por ID", description = """
                        Retorna os usuários que o usuário identificado
                        pelo UUID está seguindo.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
                        @ApiResponse(responseCode = "400", description = "UUID em formato inválido"),
                        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
                        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
        })
        public ResponseEntity<FollowListResponse> getFollowing(
                        @Parameter(description = "UUID do usuário", example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID userId) {
                return ResponseEntity.ok(
                                userService.getFollowing(userId));
        }

        /*
         * LISTAR USUÁRIOS SEGUIDOS POR USERNAME
         */

        @GetMapping("/username/{username}/following")
        @Operation(summary = "Listar usuários seguidos por username", description = """
                        Retorna os usuários que o usuário identificado
                        pelo username está seguindo.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
                        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
                        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
        })
        public ResponseEntity<FollowListResponse> getFollowingByUsername(
                        @Parameter(description = "Username do usuário", example = "joaosilva") @PathVariable String username) {
                return ResponseEntity.ok(
                                userService.getFollowingByUsername(username));
        }

        /*
         * SEGUIR POR UUID
         */

        @PostMapping("/{userId}/followers")
        @Operation(summary = "Seguir usuário por ID", description = """
                        O usuário autenticado passa a seguir o usuário
                        identificado pelo UUID.

                        A operação é idempotente.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "Usuário seguido com sucesso"),
                        @ApiResponse(responseCode = "400", description = "Tentativa de seguir a si mesmo"),
                        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
                        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
        })
        public ResponseEntity<Void> follow(
                        @AuthenticationPrincipal User currentUser,

                        @Parameter(description = "UUID do usuário a ser seguido", example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID userId) {
                userService.follow(
                                currentUser.getId(),
                                userId);

                return ResponseEntity.noContent().build();
        }

        /*
         * SEGUIR POR USERNAME
         */

        @PostMapping("/username/{username}/followers")
        @Operation(summary = "Seguir usuário por username", description = """
                        O usuário autenticado passa a seguir o usuário
                        identificado pelo username.

                        A operação é idempotente.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "Usuário seguido com sucesso"),
                        @ApiResponse(responseCode = "400", description = "Tentativa de seguir a si mesmo"),
                        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
                        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
        })
        public ResponseEntity<Void> followByUsername(
                        @AuthenticationPrincipal User currentUser,

                        @Parameter(description = "Username do usuário a ser seguido", example = "joaosilva") @PathVariable String username) {
                userService.followByUsername(
                                currentUser.getId(),
                                username);

                return ResponseEntity.noContent().build();
        }

        /*
         * DEIXAR DE SEGUIR POR UUID
         */

        @DeleteMapping("/{userId}/followers")
        @Operation(summary = "Deixar de seguir usuário por ID", description = """
                        O usuário autenticado deixa de seguir o usuário
                        identificado pelo UUID.

                        A operação é idempotente.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "Deixou de seguir com sucesso"),
                        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
                        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
        })
        public ResponseEntity<Void> unfollow(
                        @AuthenticationPrincipal User currentUser,

                        @Parameter(description = "UUID do usuário", example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID userId) {
                userService.unfollow(
                                currentUser.getId(),
                                userId);

                return ResponseEntity.noContent().build();
        }

        /*
         * DEIXAR DE SEGUIR POR USERNAME
         */

        @DeleteMapping("/username/{username}/followers")
        @Operation(summary = "Deixar de seguir usuário por username", description = """
                        O usuário autenticado deixa de seguir o usuário
                        identificado pelo username.

                        A operação é idempotente.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "Deixou de seguir com sucesso"),
                        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
                        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
        })
        public ResponseEntity<Void> unfollowByUsername(
                        @AuthenticationPrincipal User currentUser,

                        @Parameter(description = "Username do usuário", example = "joaosilva") @PathVariable String username) {
                userService.unfollowByUsername(
                                currentUser.getId(),
                                username);

                return ResponseEntity.noContent().build();
        }
}
