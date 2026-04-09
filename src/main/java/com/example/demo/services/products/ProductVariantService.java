package com.example.demo.services.products;

import com.example.demo.models.Color;
import com.example.demo.models.Product;
import com.example.demo.models.products.ProductVariant;
import com.example.demo.repository.products.ProductRepository;
import com.example.demo.repository.products.ProductVariantRepository;
import com.example.demo.utils.SkuGenerator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final SkuGenerator skuGenerator;
    private final ProductRepository productRepository;

    @Transactional
    public ProductVariant createVariant(Product product, Color color, BigDecimal price){
        String sku = skuGenerator.generate(product.getName(), color.getHexCode());

        ProductVariant variant = new ProductVariant();
        variant.setSku(sku);
        variant.setProduct(product);
        variant.setColor(color);
        variant.setPrice(price);
        try {
            return productVariantRepository.save(variant);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("SKU conflict, please retry");
        }
    }

    public List<ProductVariant> findAll(){
        return productVariantRepository.findAll();
    }

    public Product getProductById(Long id){
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("product not found"));
    }
}
