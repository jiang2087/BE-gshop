package com.example.demo.dto.message_ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Choice {

    private Integer index;
    private String logprobs;
    private String finish_reason;
    private String native_finish_reason;

    private Message message;
}
