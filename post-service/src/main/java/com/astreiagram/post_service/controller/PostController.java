package com.astreiagram.post_service.controller;

import com.astreiagram.post_service.dto.CommentRequest;
import com.astreiagram.post_service.dto.CreatePostRequest;
import com.astreiagram.post_service.dto.PostResponse;
import com.astreiagram.post_service.model.Comment;
import com.astreiagram.post_service.model.Post;
import com.astreiagram.post_service.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/posts")
@Tag(name = "Postagens", description = "Endpoints para postagens, curtidas e comentários")
@SecurityRequirement(name = "bearerAuth")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(
            summary = "Criar uma nova postagem",
            description = "Cria um post associado ao usuário autenticado, extraído do token JWT, e publica " +
                    "de forma assíncrona o evento post-created no Kafka para distribuição no Feed Service."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Post criado com sucesso",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos no corpo da requisição", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content)
    })
    @PostMapping
    public ResponseEntity<PostResponse> create(
            HttpServletRequest httpRequest,
            @Valid @RequestBody CreatePostRequest request) {
        String userId = (String) httpRequest.getAttribute("userId");
        Post post = postService.createPost(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PostResponse.from(post));
    }

    @Operation(
            summary = "Buscar uma postagem pelo ID",
            description = "Retorna uma postagem específica pelo identificador do MongoDB.",
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post encontrado",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "404", description = "Post não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getById(
            @Parameter(description = "ID da postagem no MongoDB", example = "664f1c2e8a1b2c3d4e5f6789")
            @PathVariable String id) {
        return ResponseEntity.ok(PostResponse.from(postService.getPostById(id)));
    }

    @Operation(
            summary = "Buscar várias postagens por uma lista de IDs",
            description = "Retorna em lote as postagens correspondentes aos IDs informados. É utilizado principalmente " +
                    "pelo Feed Service para resolver os posts que compõem o feed cronológico.",
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de posts encontrados. A lista pode ser menor que a quantidade de IDs solicitada.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PostResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Parâmetro ids ausente ou inválido", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<PostResponse>> getByIds(
            @Parameter(description = "IDs das postagens. No Swagger, adicione um item por ID.",
                    example = "664f1c2e8a1b2c3d4e5f6789")
            @RequestParam List<String> ids) {
        List<PostResponse> posts = postService.getPostsByIds(ids).stream()
                .map(PostResponse::from)
                .toList();
        return ResponseEntity.ok(posts);
    }

    @Operation(
            summary = "Listar postagens de um usuário",
            description = "Retorna o histórico paginado de posts de um usuário, ordenado por data decrescente. " +
                    "Antes da busca, valida no User Service se o usuário existe.",
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de posts do usuário"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostResponse>> getByUserId(
            @Parameter(description = "UUID do usuário autor dos posts", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
            @PathVariable String userId,
            @ParameterObject
            @PageableDefault(size = 50, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostResponse> posts = postService.getPostsByUserId(userId, pageable)
                .map(PostResponse::from);
        return ResponseEntity.ok(posts);
    }

    @Operation(
            summary = "Deletar uma postagem",
            description = "Remove uma postagem. Apenas o autor do post pode executar esta operação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Post removido com sucesso", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário autenticado não é o dono do post", content = @Content),
            @ApiResponse(responseCode = "404", description = "Post não encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            HttpServletRequest httpRequest,
            @Parameter(description = "ID da postagem", example = "664f1c2e8a1b2c3d4e5f6789")
            @PathVariable String id) {
        String userId = (String) httpRequest.getAttribute("userId");
        postService.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Curtir uma postagem",
            description = "Adiciona a curtida do usuário autenticado. A operação é idempotente: curtir novamente não gera duplicidade."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Curtida registrada ou já existente", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Post não encontrado", content = @Content)
    })
    @PostMapping("/{id}/likes")
    public ResponseEntity<Void> like(
            HttpServletRequest httpRequest,
            @Parameter(description = "ID da postagem", example = "664f1c2e8a1b2c3d4e5f6789")
            @PathVariable String id) {
        String userId = (String) httpRequest.getAttribute("userId");
        postService.likePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Listar usuários que curtiram a postagem",
            description = "Retorna uma página da lista de UUIDs dos usuários que curtiram a postagem.",
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista paginada de UUIDs de usuários",
                    content = @Content(array = @ArraySchema(schema = @Schema(type = "string", format = "uuid")))),
            @ApiResponse(responseCode = "404", description = "Post não encontrado", content = @Content)
    })
    @GetMapping("/{id}/likes")
    public ResponseEntity<List<String>> getLikes(
            @Parameter(description = "ID da postagem", example = "664f1c2e8a1b2c3d4e5f6789")
            @PathVariable String id,
            @Parameter(description = "Número da página, iniciando em 0", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de usuários por página", example = "50")
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(postService.getLikes(id, page, size));
    }

    @Operation(
            summary = "Remover a curtida de uma postagem",
            description = "Remove a curtida do usuário autenticado. Se a curtida não existir, a operação continua sem erro."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Curtida removida ou já inexistente", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Post não encontrado", content = @Content)
    })
    @DeleteMapping("/{id}/likes")
    public ResponseEntity<Void> unlike(
            HttpServletRequest httpRequest,
            @Parameter(description = "ID da postagem", example = "664f1c2e8a1b2c3d4e5f6789")
            @PathVariable String id) {
        String userId = (String) httpRequest.getAttribute("userId");
        postService.unlikePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Adicionar um comentário à postagem",
            description = "Adiciona um comentário do usuário autenticado à postagem informada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Comentário adicionado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Conteúdo do comentário vazio ou inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Post não encontrado", content = @Content)
    })
    @PostMapping("/{id}/comments")
    public ResponseEntity<Void> comment(
            HttpServletRequest httpRequest,
            @Parameter(description = "ID da postagem", example = "664f1c2e8a1b2c3d4e5f6789")
            @PathVariable String id,
            @Valid @RequestBody CommentRequest request) {
        String userId = (String) httpRequest.getAttribute("userId");
        postService.addComment(id, userId, request.content());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Listar comentários de uma postagem",
            description = "Retorna uma página dos comentários da postagem, usando paginação iniciada em 0.",
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista paginada de comentários",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Comment.class)))),
            @ApiResponse(responseCode = "404", description = "Post não encontrado", content = @Content)
    })
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<Comment>> getComments(
            @Parameter(description = "ID da postagem", example = "664f1c2e8a1b2c3d4e5f6789")
            @PathVariable String id,
            @Parameter(description = "Número da página, iniciando em 0", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de comentários por página", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getComments(id, page, size));
    }

    @Operation(
            summary = "Deletar um comentário",
            description = "Remove um comentário. Apenas o autor do comentário pode executar esta operação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Comentário removido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário autenticado não é o dono do comentário", content = @Content),
            @ApiResponse(responseCode = "404", description = "Post ou comentário não encontrado", content = @Content)
    })
    @DeleteMapping("{id}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            HttpServletRequest httpRequest,
            @Parameter(description = "ID da postagem", example = "664f1c2e8a1b2c3d4e5f6789")
            @PathVariable String id,
            @Parameter(description = "UUID do comentário", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable String commentId) {
        String userId = (String) httpRequest.getAttribute("userId");
        postService.deleteComment(id, commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
