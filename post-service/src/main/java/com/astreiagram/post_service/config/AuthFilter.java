package com.astreiagram.post_service.config;

import com.astreiagram.post_service.client.UserClient;
import com.astreiagram.post_service.dto.ValidateTokenResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private final UserClient userClient;

    public AuthFilter(UserClient userClient) {
        this.userClient = userClient;
    }
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		String path = request.getServletPath();
		String method = request.getMethod();

		// públicos: health, swagger, docs
		if (path.startsWith("/actuator") || path.startsWith("/swagger-ui") || path.startsWith("/api-docs")) {
			chain.doFilter(request, response);
			return;
		}

		// leitura de posts é pública (GET em /posts/**)
		if (path.startsWith("/posts") && method.equals("GET")) {
			chain.doFilter(request, response);
			return;
		}

		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token ausente");
			return;
		}

		String token = authHeader.substring(7);
		ValidateTokenResponse validation = userClient.validateToken(token);

		if (!validation.valid()) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
			return;
		}

		request.setAttribute("userId", validation.userId());
		request.setAttribute("username", validation.username());

		chain.doFilter(request, response);
	}
}
