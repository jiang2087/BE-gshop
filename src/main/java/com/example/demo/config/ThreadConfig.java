package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Configuration
@EnableAsync
public class ThreadConfig {
    @Bean
    public ThreadFactory virtualThreadFactory() {
        return Thread.ofVirtual().factory();
    }

    @Bean
    public Executor taskExecutor(ThreadFactory threadFactory) {
        return Executors.newThreadPerTaskExecutor(threadFactory);
    }
}
