package com.astreiagram.post_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.astreiagram.post_service.dto.ValidateTokenResponse;

@Component
public class UserClient {

    private final RestClient restClient;

	private static final Logger log = LoggerFactory.getLogger(UserClient.class);

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
        } catch (HttpClientErrorException.Unauthorized ex) {
            log.warn("Token rejected by user-service: status={}", ex.getStatusCode().value());
            return null;
        } catch (Exception e) {
            log.error("Failed to validate token against user-service", e);
            return null;
        }
    }

	public boolean userExists(String userId) {
        try {
            restClient.get()
                .uri("/users/{id}/exists", userId)
                .retrieve()
                .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound ex) {
            return false;
        } catch (HttpClientErrorException.BadRequest ex) {
            log.warn("Invalid userId format when checking existence: userId={}", userId);
            return false;
        } catch (Exception e) {
            log.error("Failed to check user existence: userId={}", userId, e);
            return false;
        }
    }
}
