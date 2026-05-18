package com.example.demo.dto.message_ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {

    private String role;
    private String content;
    private String refusal;
    private String reasoning;

    @JsonProperty("tool_calls")
    private List<ToolCall> toolCalls;
}
