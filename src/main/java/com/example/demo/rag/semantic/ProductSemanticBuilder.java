package com.example.demo.rag.semantic;

import com.example.demo.dto.product.ProductDetailDto;

public interface ProductSemanticBuilder {

    /**
     * Build semantic text representation from ProductDetailDto
     * @param product Product detail DTO
     * @return Semantic text for embedding
     */
    String build(ProductDetailDto product);
    
    /**
     * Check if this builder supports the given product type
     * @param productType Product type string
     * @return true if supported
     */
    boolean supports(String productType);
}