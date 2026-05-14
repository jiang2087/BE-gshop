package com.example.demo.controllers.rag;

import com.example.demo.rag.connection.RagConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rag/connection")
public class RagConnectionController {

    private final RagConnectionService ragConnectionService;

    @GetMapping("/test")
    public ResponseEntity<RagConnectionService.ConnectionTestResponse> testConnection() {
        return ResponseEntity.ok(ragConnectionService.testAll());
    }
}
