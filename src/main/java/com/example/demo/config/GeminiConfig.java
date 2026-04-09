package com.example.demo.config;

import com.google.common.collect.ImmutableMap;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.HttpRetryOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {
    @Bean
    public Client googleClient(){
        return Client.builder()
                .httpOptions(HttpOptions.builder().apiVersion("v1").build())
                .build();
    }
    @Bean
    public GenerateContentConfig googleConfig(){
        return GenerateContentConfig.builder()
                .httpOptions(
                        HttpOptions.builder()
                                .headers(ImmutableMap.of("my-header", "my value"))
                                .retryOptions(HttpRetryOptions.builder().attempts(3).httpStatusCodes(408, 302).build())
                                .build()
                )
                .build();
    }
}
