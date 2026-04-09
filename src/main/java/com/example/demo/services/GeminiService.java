package com.example.demo.services;

import com.example.demo.utils.Constant;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GeminiService {

    private final Client client;
    private final GenerateContentConfig config;

    public String ask(String message){
        GenerateContentResponse res = client.models.generateContent(Constant.GEMINI_MODEL_NAME, message, config);
        return res.text();
    }
}
