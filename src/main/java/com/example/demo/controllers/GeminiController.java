package com.example.demo.controllers;


import com.example.demo.services.GeminiService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ask")
@AllArgsConstructor
public class GeminiController {

    private final GeminiService geminiService;


    @GetMapping
    public String response(@RequestBody String message){
        return geminiService.ask(message);
    }
}
