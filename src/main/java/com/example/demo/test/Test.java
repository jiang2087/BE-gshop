package com.example.demo.test;

import com.example.demo.utils.Constant;
import com.google.common.collect.ImmutableMap;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.HttpRetryOptions;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;

public class Test {
    public static void main(String[] args) {

        Client client = new Client();
        String modelAI = Constant.GEMINI_MODEL_NAME;
        if(client.vertexAI()){
            System.out.println("using vertex ai");
        }else{
            System.out.println("using gemini developer ai");
        }
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String base64Key = Encoders.BASE64.encode(key.getEncoded());
        System.out.println(base64Key);
        // set a customized header and retry options per request config
        GenerateContentConfig config = GenerateContentConfig.builder()
                .httpOptions(
                        HttpOptions.builder()
                                .headers(ImmutableMap.of("my-header", "my value"))
                                .retryOptions(HttpRetryOptions.builder().attempts(3).httpStatusCodes(408, 429).build())
                                .build()
                )
                .build();

        GenerateContentResponse res = client.models.generateContent(modelAI, "hi can you write a simple program by python language", config);

        System.out.println("Response: " + res.text());
    }
}
