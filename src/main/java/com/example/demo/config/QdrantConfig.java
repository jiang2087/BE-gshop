package com.example.demo.config;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class QdrantConfig {

    @Value("${qdrant.host}")
    private String host;

    @Value("${qdrant.port}")
    private int port;

    @Bean
    public QdrantClient qdrantClient() {
        log.info("[QdrantConfig] Connecting to Qdrant at {}:{}", host, port);
        
        QdrantClient client = new QdrantClient(
            QdrantGrpcClient.newBuilder(host, port, false).build()
        );
        
        log.info("[QdrantConfig] Qdrant client initialized successfully");
        return client;
    }
}
