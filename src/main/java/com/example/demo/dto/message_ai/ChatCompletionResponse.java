package com.example.demo.dto.message_ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatCompletionResponse {

    private String id;
    private String object;
    private Long created;
    private String model;
    private String provider;
    private String system_fingerprint;
    private String service_tier;

    private List<Choice> choices;
    private Usage usage;
}
