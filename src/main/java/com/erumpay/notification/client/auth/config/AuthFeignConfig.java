package com.erumpay.notification.client.auth.config;

import feign.Request;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;

public class AuthFeignConfig {

    @Bean
    public Request.Options authRequestOptions() {
        return new Request.Options(1, TimeUnit.SECONDS, 2, TimeUnit.SECONDS, true);
    }
}
