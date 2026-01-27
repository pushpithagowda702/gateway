package com.fitness.gateway.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserValidationService {
    private static final Logger log = LoggerFactory.getLogger(UserValidationService.class);
    private final WebClient userServiceWebClient;

    public Mono<Boolean> validateUser(String userId) {
            log.info("Calling user service");
            return userServiceWebClient.get()
                    .uri("/api/users/validate-user/{userId}", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .onErrorResume(WebClientResponseException.class, e -> {
                        if(e.getStatusCode() == HttpStatus.NOT_FOUND) {
                            return Mono.error(new RuntimeException("User not found"));
                        } else {
                            return Mono.error(new RuntimeException("Bad request"));
                        }
                    });

    }

    public Mono<UserResponse> registerUser(RegisterRequest registerRequest) {
        return userServiceWebClient.post()
                .uri("/api/users/register")
                .bodyValue(registerRequest)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    if(e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return Mono.error(new RuntimeException("Bad request "+ e.getMessage()));
                    }
                    return Mono.error(new RuntimeException("Internal server error " + e.getMessage()));
                });
    }
}
