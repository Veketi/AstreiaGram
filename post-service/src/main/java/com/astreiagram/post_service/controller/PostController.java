package com.astreiagram.post_service.controller;

import com.astreiagram.post_service.dto.CreatePostRequest;
import com.astreiagram.post_service.dto.PostResponse;
import com.astreiagram.post_service.dto.CommentRequest;
import com.astreiagram.post_service.model.Comment;
import com.astreiagram.post_service.model.Post;
import com.astreiagram.post_service.service.PostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(
        summary = "Criar uma nova postagem",
        description = "Cria um post associado ao usuário autenticado (extraído do token JWT) e publica " +
            "de forma assíncrona o evento \"post-created\" no Kafka, para que o Feed Service " +
            "distribua a postagem na timeline dos seguidores."
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

    @Operation(summary = "Buscar uma postagem pelo ID", security = {})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Post encontrado",
            content = @Content(schema = @Schema(implementation = PostResponse.class))),
        @ApiResponse(responseCode = "404", description = "Post não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(PostResponse.from(postService.getPostById(id)));
    }

    @Operation(
        summary = "Buscar várias postagens por uma lista de IDs",
        description = "Usado principalmente pelo Feed Service para resolver, em lote, os posts que " +
            "compõem o feed cronológico de um usuário.",
        security = {}
    )
    @ApiResponse(responseCode = "200", description = "Lista de posts encontrados (pode vir menor que a lista de IDs pedida)")
	@GetMapping
	public ResponseEntity<List<PostResponse>> getByIds(@RequestParam List<String> ids) {
		List<PostResponse> posts = postService.getPostsByIds(ids).stream()
			.map(PostResponse::from)
			.toList();
		return ResponseEntity.ok(posts);
	}

    @Operation(
        summary = "Listar postagens de um usuário (grid de perfil)",
        description = "Retorna, paginado e ordenado por data decrescente, o histórico de posts de um " +
            "usuário. Valida no User Service se o usuário informado realmente existe.",
        security = {}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Página de posts do usuário"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostResponse>> getByUserId(
			@PathVariable String userId,
			@PageableDefault(size = 50, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
		Page<PostResponse> posts = postService.getPostsByUserId(userId, pageable)
			.map(PostResponse::from);
        return ResponseEntity.ok(posts);
    }

    @Operation(
        summary = "Deletar uma postagem",
        description = "Apenas o autor do post pode removê-lo."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Post removido com sucesso", content = @Content),
        @ApiResponse(responseCode = "403", description = "Usuário autenticado não é o dono do post", content = @Content),
        @ApiResponse(responseCode = "404", description = "Post não encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(HttpServletRequest httpRequest, @PathVariable String id) {
        String userId = (String) httpRequest.getAttribute("userId");
        postService.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Curtir uma postagem", description = "Operação idempotente: curtir um post já curtido pelo mesmo usuário não duplica a curtida.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Curtida registrada", content = @Content),
        @ApiResponse(responseCode = "404", description = "Post não encontrado", content = @Content)
    })
    @PostMapping("/{id}/likes")
    public ResponseEntity<Void> like(HttpServletRequest httpRequest, @PathVariable String id) {
        String userId = (String) httpRequest.getAttribute("userId");
		postService.likePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar usuários que curtiram a postagem", security = {})
	@ApiResponse(responseCode = "200", description = "Lista paginada de IDs de usuário")
	@GetMapping("/{id}/likes")
	public ResponseEntity<List<String>> getLikes(
			@PathVariable String id,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {
		return ResponseEntity.ok(postService.getLikes(id, page, size));
	}

    @Operation(summary = "Remover a curtida de uma postagem")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Curtida removida (ou já não existia)", content = @Content),
        @ApiResponse(responseCode = "404", description = "Post não encontrado", content = @Content)
    })
    @DeleteMapping("/{id}/likes")
    public ResponseEntity<Void> unlike(HttpServletRequest httpRequest, @PathVariable String id) {
        String userId = (String) httpRequest.getAttribute("userId");
		postService.unlikePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Adicionar um comentário à postagem")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Comentário adicionado", content = @Content),
        @ApiResponse(responseCode = "400", description = "Conteúdo do comentário inválido (vazio)", content = @Content),
        @ApiResponse(responseCode = "404", description = "Post não encontrado", content = @Content)
    })
    @PostMapping("/{id}/comments")
    public ResponseEntity<Void> comment(HttpServletRequest httpRequest,
                                         @PathVariable String id,
                                         @Valid @RequestBody CommentRequest request) {
        String userId = (String) httpRequest.getAttribute("userId");
		postService.addComment(id, userId, request.content());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar comentários de uma postagem", security = {})
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Lista paginada de comentários",
			content = @Content(schema = @Schema(implementation = Comment.class))),
		@ApiResponse(responseCode = "404", description = "Post não encontrado", content = @Content)
	})
	@GetMapping("/{id}/comments")
	public ResponseEntity<List<Comment>> getComments(
			@PathVariable String id,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(postService.getComments(id, page, size));
	}

    @Operation(
        summary = "Deletar um comentário",
        description = "Apenas o autor do comentário pode removê-lo."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Comentário removido", content = @Content),
        @ApiResponse(responseCode = "403", description = "Usuário autenticado não é o dono do comentário", content = @Content),
        @ApiResponse(responseCode = "404", description = "Post ou comentário não encontrado", content = @Content)
    })
	@DeleteMapping("{id}/comments/{commentId}")
	public ResponseEntity<Void> deleteComment(HttpServletRequest httpRequest,
			@PathVariable String id,
			@PathVariable String commentId) {
		String userId = (String) httpRequest.getAttribute("userId");
		postService.deleteComment(id, commentId, userId);
		return ResponseEntity.noContent().build();
	}
}
