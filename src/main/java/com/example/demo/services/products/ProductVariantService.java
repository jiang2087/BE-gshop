package com.example.demo.services.products;

import com.example.demo.Enums.ProductType;
import com.example.demo.models.Color;
import com.example.demo.models.Product;
import com.example.demo.models.products.ProductVariant;
import com.example.demo.repository.products.ProductRepository;
import com.example.demo.repository.products.ProductVariantRepository;
import com.example.demo.utils.SkuGenerator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public Page<?> getAllProducts(Pageable pageable){
            return productRepository.findAll(pageable);
    }

    public List<String> getNameByIds(List<Long> ids){
        return productRepository.findByIds(ids);
    }

    public Product getProductById(Long id){
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("product not found"));
    }

    public Page<Product> getProductByType(List<String> types, Pageable pageable){
        var clazz = types.stream()
                .map(type -> ProductType.valueOf(type).getClazz())
                .toList();
        return productRepository.findByType(clazz, pageable);
    }

    public Map<String, Long> countProductsByType(List<String> types) {
        var clazz = types.stream()
                .map(type -> ProductType.valueOf(type).getClazz())
                .toList();

        var result = productRepository.countByType(clazz);

        return result.stream().collect(Collectors.toMap(
                r -> ((Class<?>) r[0]).getSimpleName(),
                r -> (Long) r[1]
        ));
    }

    public Page<Product> getProductsByPriceRange(List<String> types,BigDecimal min, BigDecimal max, Pageable pageable) {
        if (min.compareTo(BigDecimal.ZERO) < 0 || max.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price values must be non-negative");
        }
        if (min.compareTo(max) > 0) {
            throw new IllegalArgumentException("min price must be <= max price");
        }

        List<String> safeTypes = Optional.ofNullable(types).orElse(List.of());

        if (safeTypes.isEmpty()) {
            return productRepository.findByPriceRange(min, max, pageable);
        }

        var clazz = safeTypes.stream()
                .map(type -> ProductType.valueOf(type).getClazz())
                .toList();

        return productRepository.findByCategoryAndPriceRange(clazz, min, max, pageable);
    }

}
