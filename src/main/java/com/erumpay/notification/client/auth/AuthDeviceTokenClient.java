package com.erumpay.notification.client.auth;

import com.erumpay.notification.client.auth.config.AuthFeignConfig;
import com.erumpay.notification.client.auth.dto.AuthActiveDeviceTokenResponse;
import com.erumpay.notification.client.auth.dto.AuthDeviceTokenDeactivateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(
        name = "auth-service",
        url = "${notification.clients.auth-service.url:http://localhost:8081}",
        configuration = AuthFeignConfig.class
)
public interface AuthDeviceTokenClient {

    @GetMapping(value = "/api/v1/internal/users/{userId}/device-tokens")
    AuthActiveDeviceTokenResponse getActiveDeviceTokens(@PathVariable("userId") Long userId);

    @PatchMapping(value = "/api/v1/internal/device-tokens/inactive", consumes = APPLICATION_JSON_VALUE)
    void deactivateInvalidToken(@RequestBody AuthDeviceTokenDeactivateRequest request);
}
