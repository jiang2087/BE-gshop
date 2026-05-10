package com.example.demo.dto.message_ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Usage {

    private int prompt_tokens;
    private int completion_tokens;
    private int total_tokens;

    private TokenDetails prompt_tokens_details;
    private TokenDetails completion_tokens_details;
}