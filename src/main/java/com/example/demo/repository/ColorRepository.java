package com.example.demo.repository;

import com.example.demo.models.Color;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ColorRepository extends JpaRepository<Color, Integer> {
     Optional<Color> findByHexCode(String hexCode);
}
