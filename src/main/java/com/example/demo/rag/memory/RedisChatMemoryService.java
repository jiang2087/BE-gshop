package com.example.demo.rag.memory;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RedisChatMemoryService implements ChatMemoryService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatMemoryConfig config;

    @Override
    public List<ChatTurn> getConversation(String conversationId) {
        List<ChatTurn> turns = readTurns(buildKey(conversationId));
        return turns == null ? List.of() : turns;
    }

    @Override
    public void appendUserMessage(String conversationId, String content) {
        append(conversationId, "user", content);
    }

    @Override
    public void appendAssistantMessage(String conversationId, String content) {
        appendAssistantMessage(conversationId, content, null);
    }

    @Override
    public void appendAssistantMessage(String conversationId, String content, String reasoningContent) {
        append(conversationId, "assistant", content, reasoningContent);
    }

    @Override
    public void clearConversation(String conversationId) {
        redisTemplate.delete(buildKey(conversationId));
    }

    @Override
    public void clearAllConversations() {
        String pattern = config.getKeyPrefix() + "*";
        var keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private void append(String conversationId, String role, String content) {
        append(conversationId, role, content, null);
    }

    private void append(String conversationId, String role, String content, String reasoningContent) {
        String key = buildKey(conversationId);
        List<ChatTurn> turns = readTurns(key);
        if (turns == null) {
            turns = new ArrayList<>();
        }
        turns.add(new ChatTurn(role, content, reasoningContent, Instant.now()));

        int maxTurns = Math.max(1, config.getMaxTurns());
        if (turns.size() > maxTurns) {
            turns = new ArrayList<>(turns.subList(turns.size() - maxTurns, turns.size()));
        }

        redisTemplate.opsForValue().set(key, turns, config.getTtl());
    }

    @SuppressWarnings("unchecked")
    private List<ChatTurn> readTurns(String key) {
        Object raw = redisTemplate.opsForValue().get(key);
        if (raw instanceof List<?> list) {
            if (list.isEmpty()) {
                return List.of();
            }

            Object first = list.getFirst();
            if (first instanceof ChatTurn) {
                return (List<ChatTurn>) list;
            }

            if (first instanceof Map<?, ?>) {
                List<ChatTurn> converted = new ArrayList<>(list.size());
                for (Object item : list) {
                    if (item instanceof Map<?, ?> map) {
                        converted.add(mapToChatTurn((Map<String, Object>) map));
                    }
                }
                return converted;
            }
        }
        return null;
    }

    private ChatTurn mapToChatTurn(Map<String, Object> map) {
        String role = toStringValue(map.get("role"));
        String content = toStringValue(map.get("content"));
        String reasoningContent = toStringValueOrNull(map.get("reasoningContent"));
        Instant timestamp = toInstant(map.get("timestamp"));
        return new ChatTurn(role, content, reasoningContent, timestamp);
    }

    private String toStringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String toStringValueOrNull(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Instant toInstant(Object value) {
        if (value == null) {
            return Instant.now();
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof LinkedHashMap<?, ?> map) {
            Object epochSecond = map.get("epochSecond");
            Object nano = map.get("nano");
            if (epochSecond instanceof Number seconds && nano instanceof Number nanos) {
                return Instant.ofEpochSecond(seconds.longValue(), nanos.longValue());
            }
        }
        try {
            return Instant.parse(String.valueOf(value));
        } catch (Exception ignored) {
            return Instant.now();
        }
    }

    private String buildKey(String conversationId) {
        return config.getKeyPrefix() + conversationId;
    }
}
