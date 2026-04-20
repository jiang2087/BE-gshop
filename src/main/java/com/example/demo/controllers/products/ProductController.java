package com.example.demo.controllers.products;

import com.example.demo.services.products.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductVariantService  productVariantService;

    @GetMapping
    public ResponseEntity<?> getAllProducts(Pageable pageable){
        return ResponseEntity.ok(productVariantService.getAllProducts(pageable));
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable long id){
        return ResponseEntity.ok(productVariantService.getProductById(id));
    }
}
