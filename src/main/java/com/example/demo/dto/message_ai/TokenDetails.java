package com.example.demo.dto.message_ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenDetails {

    private int cached_tokens;
    private int cache_write_tokens;
    private int audio_tokens;
    private int video_tokens;
    private int reasoning_tokens;
    private int image_tokens;
}
