package com.astreiagram.post_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.astreiagram.post_service.dto.ValidateTokenRequest;
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
                .uri("/auth/validate}")
				.body(new ValidateTokenRequest(token))
                .retrieve()
                .body(ValidateTokenResponse.class);
        } catch (Exception e) {
            return new ValidateTokenResponse(false, null, null);
        }
    }
}
