package com.example.demo.controllers;

import com.example.demo.repository.ColorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/colors")
@RequiredArgsConstructor
public class ColorController {

    private final ColorRepository colorRepository;

    @GetMapping
    public ResponseEntity<?> getColors() {
        return ResponseEntity.ok(colorRepository.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteColors(Integer id) {
        colorRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

