package com.example.demo.services.products;

import com.example.demo.Enums.ProductType;
import com.example.demo.dto.request.ProductRequest;
import com.example.demo.dto.request.VariantRequest;
import com.example.demo.dto.response.TopProductProjection;
import com.example.demo.models.Color;
import com.example.demo.models.Product;
import com.example.demo.models.products.Laptop;
import com.example.demo.models.products.Mobile;
import com.example.demo.models.products.ProductVariant;
import com.example.demo.models.products.Television;
import com.example.demo.models.products.Watches;
import com.example.demo.repository.ColorRepository;
import com.example.demo.repository.products.ProductRepository;
import com.example.demo.repository.products.ProductVariantRepository;
import com.example.demo.utils.ProductUtil;
import com.example.demo.utils.SkuGenerator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final SkuGenerator skuGenerator;
    private final ProductRepository productRepository;
    private final ColorRepository colorRepository;
    private final ProductUtil productUtil;

    @Transactional
    public ProductVariant createProduct(ProductRequest request) {

        if (request.stockQuantity() < 0) {
            throw new IllegalArgumentException("stockQuantity must be >= 0");
        }

        Product product = instantiateProduct(request.productType());

        product.setBrand(request.brand());
        product.setThumbnail(request.thumbnail());
        product.setName(request.name());
        product.setDescription(request.description());

        productUtil.mapAttributesToEntity(product, request.attributes());

        product = productRepository.save(product);

        if (!product.getClass().getSimpleName().equalsIgnoreCase(request.productType())) {
            throw new IllegalStateException("Product type mismatch after save");
        }

        Color color = colorRepository.findByHexCode(request.hexCode())
                .orElseGet(() -> {
                    Color c = new Color();
                    c.setName(request.colorName());
                    c.setHexCode(request.hexCode());
                    return colorRepository.save(c);
                });

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setColor(color);
        variant.setSku(skuGenerator.generate(product.getName(), color.getHexCode()));
        variant.setPrice(request.price());
        variant.setImage(request.thumbnail());
        variant.setStockQuantity(request.stockQuantity());
        variant.setIsDefault(Boolean.TRUE.equals(request.isDefault()));
        variant.setActive(request.active() == null || request.active());

        return productVariantRepository.save(variant);
    }


    @Transactional
    public ProductVariant updateProduct(Long variantId, ProductRequest request) {

        if (request.stockQuantity() < 0) {
            throw new IllegalArgumentException("stockQuantity must be >= 0");
        }

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        Product product = variant.getProduct();

        // update product info
        product.setBrand(request.brand());
        product.setThumbnail(request.thumbnail());
        product.setName(request.name());
        product.setDescription(request.description());

        // map dynamic attributes
        productUtil.mapAttributesToEntity(product, request.attributes());

        product = productRepository.save(product);

        // validate type
        if (!product.getClass().getSimpleName().equalsIgnoreCase(request.productType())) {
            throw new IllegalStateException("Product type mismatch after update");
        }

        // update/create color
        Color color = colorRepository.findByHexCode(request.hexCode())
                .orElseGet(() -> {
                    Color c = new Color();
                    c.setName(request.colorName());
                    c.setHexCode(request.hexCode());
                    return colorRepository.save(c);
                });

        // update variant info
        variant.setColor(color);

        // regenerate SKU if needed
        variant.setSku(skuGenerator.generate(product.getName(), color.getHexCode()));

        variant.setPrice(request.price());
        variant.setImage(request.thumbnail());
        variant.setStockQuantity(request.stockQuantity());
        variant.setIsDefault(Boolean.TRUE.equals(request.isDefault()));
        variant.setActive(request.active() == null || request.active());

        return productVariantRepository.save(variant);
    }

    @Transactional
    public ProductVariant createVariant(Long productId, VariantRequest request) {
        if (request.price() == null || request.price().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("price must be >= 0");
        }
        if (request.stock() != null && request.stock() < 0) {
            throw new IllegalArgumentException("stock must be >= 0");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("product not found"));

        Color color = colorRepository.findByHexCode(request.hexCode())
                .orElseGet(() -> {
                    Color c = new Color();
                    c.setName(request.colorName());
                    c.setHexCode(request.hexCode());
                    return colorRepository.save(c);
                });

        String sku = skuGenerator.generate(product.getName(), color.getHexCode());

        ProductVariant variant = new ProductVariant();
        if(Boolean.TRUE.equals(request.isDefault())) {
            productVariantRepository.clearDefaultByProductId(productId);
            variant.setIsDefault(Boolean.TRUE);
        }else{
            variant.setIsDefault(Boolean.FALSE);
        }
        variant.setSku(sku);
        variant.setProduct(product);
        variant.setColor(color);
        variant.setPrice(request.price());
        variant.setImage(request.image());
        variant.setStockQuantity(request.stock() == null ? 0 : request.stock());
        variant.setActive(true);
        try {
            return productVariantRepository.save(variant);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("SKU conflict, please retry");
        }
    }

    @Transactional
    public ProductVariant updateVariant(Long variantId, VariantRequest request) {
        if (request.price() == null || request.price().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("price must be >= 0");
        }
        if (request.stock() != null && request.stock() < 0) {
            throw new IllegalArgumentException("stock must be >= 0");
        }

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new EntityNotFoundException("variant not found"));

        Color color = colorRepository.findByHexCode(request.hexCode())
                .orElseGet(() -> {
                    Color c = new Color();
                    c.setName(request.colorName());
                    c.setHexCode(request.hexCode());
                    return colorRepository.save(c);
                });

        variant.setColor(color);
        variant.setSku(skuGenerator.generate(variant.getProduct().getName(), color.getHexCode()));
        variant.setPrice(request.price());
        if (request.image() != null) {
            variant.setImage(request.image());
        }
        if(Boolean.TRUE.equals(request.isDefault())) {
            Long productId = variant.getProduct().getId();
            productVariantRepository.clearDefaultByProductId(productId);
            variant.setIsDefault(Boolean.TRUE);
        }else{
            variant.setIsDefault(Boolean.FALSE);
        }
        variant.setStockQuantity(request.stock() == null ? variant.getStockQuantity() : request.stock());

        try {
            return productVariantRepository.save(variant);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("SKU conflict, please retry");
        }
    }

    @Transactional
    public void deleteVariant(Long variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new EntityNotFoundException("variant not found"));
        Long productId = variant.getProduct().getId();
        boolean wasDefault = Boolean.TRUE.equals(variant.getIsDefault());

        productVariantRepository.delete(variant);

        if (wasDefault) {
            productVariantRepository.findFirstByProductIdAndIsDefaultFalse(productId)
                    .ifPresent(replacement -> {
                        replacement.setIsDefault(Boolean.TRUE);
                        productVariantRepository.save(replacement);
                    });
        }
    }

    public Page<Product> getAllProducts(Pageable pageable) {

        Specification<Product> spec = (root, query, cb) -> {

            if (query.getResultType() == Long.class || query.getResultType() == long.class) {
                return cb.conjunction();
            }

            List<jakarta.persistence.criteria.Order> finalOrders = new ArrayList<>();

            for (Sort.Order order : pageable.getSort()) {

                String field = order.getProperty();

                if ("price".equalsIgnoreCase(field)) {
                    Subquery<BigDecimal> sub = query.subquery(BigDecimal.class);
                    Root<ProductVariant> subRoot = sub.from(ProductVariant.class);

                    sub.select(subRoot.get("price"))
                            .where(
                                    cb.equal(subRoot.get("product"), root),
                                    cb.isTrue(subRoot.get("isDefault"))
                            );

                    Expression<BigDecimal> priceExpr = cb.coalesce(sub, BigDecimal.ZERO);
                    finalOrders.add(order.isAscending() ? cb.asc(priceExpr) : cb.desc(priceExpr));
                } else if ("totalStock".equalsIgnoreCase(field)) {
                    Subquery<Integer> sub = query.subquery(Integer.class);
                    Root<ProductVariant> subRoot = sub.from(ProductVariant.class);

                    sub.select(cb.sum(
                                    cb.<Integer>coalesce(subRoot.get("stockQuantity"), 0)
                            ))
                            .where(cb.equal(subRoot.get("product"), root));

                    Expression<Integer> stockExpr = cb.coalesce(sub, 0);

                    finalOrders.add(order.isAscending() ? cb.asc(stockExpr) : cb.desc(stockExpr));
                } else {
                    Path<?> path = root.get(field);
                    finalOrders.add(order.isAscending() ? cb.asc(path) : cb.desc(path));
                }
            }

            if (finalOrders.isEmpty()) {
                finalOrders.add(cb.desc(root.get("created")));
            }

            // Stable ordering for pagination when primary sort keys are equal.
            finalOrders.add(cb.asc(root.get("id")));
            query.orderBy(finalOrders);
            return cb.conjunction();
        };

        Pageable cleanPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        return productRepository.findAll(spec, cleanPageable);
    }

    public List<String> getNameByIds(List<Long> ids) {
        return productRepository.findByIds(ids);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("product not found"));
    }

    public Page<Product> getProductByType(List<String> types, Pageable pageable) {
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

    public Page<Product> getProductsByPriceRange(List<String> types, BigDecimal min, BigDecimal max, Pageable pageable) {
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


    public long countProductVariants() {
        return productVariantRepository.countProductVariants();
    }


    public Page<TopProductProjection> getTopProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productVariantRepository.getTopProducts(pageable);
    }

    public void deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
        }
        log.error("cannot delete product with id " + id);
    }

    public Page<Product> search(
            String keyword,
            Pageable pageable
    ) {

        String booleanKeyword = productUtil.toBooleanKeyword(keyword);

        Page<Long> ids =
                productRepository.searchIds(
                        booleanKeyword,
                        pageable
                );

        List<Product> products =
                productRepository.findAllById(ids.getContent());

        return new PageImpl<>(
                products,
                pageable,
                ids.getTotalElements()
        );
    }

    private Product instantiateProduct(String type) {
        try {
            ProductType productType = ProductType.valueOf(type.toUpperCase());

            return productType.getClazz()
                    .getDeclaredConstructor()
                    .newInstance();

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid product type: " + type, e);
        }
    }
}
