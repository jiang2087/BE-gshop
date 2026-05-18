package com.example.demo.rag.client;

import com.example.demo.rag.ChatClientException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.util.retry.Retry;

@Component
@RequiredArgsConstructor
public class RetryPolicy {
    private static final Logger logger = LoggerFactory.getLogger(RetryPolicy.class);
    
    private final ChatClientConfig config;

    public Retry createRetrySpec() {
        return Retry.backoff(config.getMaxRetryAttempts(), config.getRetryDelay())
                .filter(this::isRetryableError)
                .doBeforeRetry(retrySignal -> {
                    logger.warn("Retrying chat API call (attempt {}/{}) due to: {}",
                            retrySignal.totalRetries() + 1,
                            config.getMaxRetryAttempts(),
                            retrySignal.failure().getMessage());
                })
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                    throw new ChatClientException(
                            "Max retry attempts exceeded: " + retrySignal.failure().getMessage(),
                            retrySignal.failure()
                    );
                });
    }

    private boolean isRetryableError(Throwable throwable) {
        if (throwable instanceof WebClientRequestException) {
            return true;
        }
        if (throwable instanceof ChatClientException cce) {
            return cce.getStatusCode() >= 500 || cce.getStatusCode() == 429;
        }
        return false;
    }
}
