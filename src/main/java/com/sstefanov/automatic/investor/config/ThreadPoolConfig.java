package com.sstefanov.automatic.investor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
public class ThreadPoolConfig {

    @Bean(name = "investTaskExecutor")
    public Executor getThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(30);
        executor.setMaxPoolSize(35);
        executor.setThreadNamePrefix("investTaskExecutor-");
        executor.initialize();
        return executor;
    }
}