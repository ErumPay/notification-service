package com.erumpay.notification.config;

import java.time.Clock;
import java.time.ZoneId;
import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration
public class AsyncConfig {

    public static final String PUSH_TASK_EXECUTOR = "pushTaskExecutor";

    @Bean
    public Executor pushTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("push-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Clock seoulClock() {
        return Clock.system(ZoneId.of("Asia/Seoul"));
    }
}
