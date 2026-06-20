package com.astreiagram.post_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.astreiagram.post_service.dto.ValidateTokenResponse;

@Component
public class UserClient {

    private final RestClient restClient;

    public UserClient(@Value("${user-service.url}") String userServiceUrl) {
        this.restClient = RestClient.create(userServiceUrl);
    }

    public ValidateTokenResponse validateToken(String token) {
        try {
            return restClient.post()
                .uri("/auth/validate")
				.header("Authorization", "Bearer " + token)
                .retrieve()
                .body(ValidateTokenResponse.class);
        } catch (Exception e) {
			System.out.println("Erro ValidateTokenResponse no validateToken: " + e);
            return null;
        }
    }

	public boolean userExists(String userId) {
		try {
			restClient.get()
				.uri("/users/{id}", userId)
				.retrieve()
				.toBodilessEntity();
			return true;
		} catch(HttpClientErrorException.NotFound ex) {
			return false;
		} catch(Exception e) {
			System.out.println("Erro ao verificar o usuário: " + e);	
			return false;
		}
	}
}
