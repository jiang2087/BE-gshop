package com.example.demo.services.products;

import com.example.demo.Enums.ProductType;
import com.example.demo.Enums.DiscountType;
import com.example.demo.dto.product.ColorDto;
import com.example.demo.dto.product.ProductDetailDto;
import com.example.demo.dto.product.ProductVariantDto;
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
import com.example.demo.repository.DiscountRepository;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final DiscountRepository discountRepository;
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

    public Page<ProductDetailDto> getAllProducts(Pageable pageable) {

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

        Page<Product> page = productRepository.findAll(spec, cleanPageable);
        List<Long> productIds = page.getContent().stream().map(Product::getId).toList();
        if (productIds.isEmpty()) {
            return page.map(p -> toProductDetailDto(p, List.of(), Map.of()));
        }

        List<ProductVariant> variants = productVariantRepository.findAllByProductIdInWithColor(productIds);
        Map<Long, List<ProductVariant>> variantsByProductId = variants.stream()
                .collect(Collectors.groupingBy(v -> v.getProduct().getId()));

        List<Long> variantIds = variants.stream().map(ProductVariant::getId).toList();
        Map<Long, DiscountRepository.ActiveVariantDiscountRow> discountByVariantId = new HashMap<>();
        if (!variantIds.isEmpty()) {
            for (DiscountRepository.ActiveVariantDiscountRow row :
                    discountRepository.findActiveDiscountRowsByVariantIds(variantIds, LocalDateTime.now())) {
                discountByVariantId.putIfAbsent(row.getVariantId(), row);
            }
        }

        return page.map(product -> toProductDetailDto(
                product,
                variantsByProductId.getOrDefault(product.getId(), List.of()),
                discountByVariantId
        ));
    }

    public List<String> getNameByIds(List<Long> ids) {
        return productRepository.findByIds(ids);
    }

    public ProductDetailDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("product not found"));

        List<ProductVariant> variants = productVariantRepository.findAllByProductIdInWithColor(List.of(id));
        List<Long> variantIds = variants.stream().map(ProductVariant::getId).toList();

        Map<Long, DiscountRepository.ActiveVariantDiscountRow> discountByVariantId = new HashMap<>();
        if (!variantIds.isEmpty()) {
            for (DiscountRepository.ActiveVariantDiscountRow row :
                    discountRepository.findActiveDiscountRowsByVariantIds(variantIds, LocalDateTime.now())) {
                discountByVariantId.putIfAbsent(row.getVariantId(), row);
            }
        }

        return toProductDetailDto(product, variants, discountByVariantId);
    }


    /**
     * Batch load products by IDs to avoid N+1 query problem
     * @param productIds List of product IDs
     * @return Map of productId -> ProductDetailDto
     */
    public Map<Long, ProductDetailDto> getProductsByIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }

        // Batch load all products
        List<Product> products = productRepository.findAllById(productIds);
        if (products.isEmpty()) {
            return Map.of();
        }

        // Batch load all variants for these products
        List<ProductVariant> variants = productVariantRepository.findAllByProductIdInWithColor(productIds);
        List<Long> variantIds = variants.stream().map(ProductVariant::getId).toList();

        // Batch load all discounts
        Map<Long, DiscountRepository.ActiveVariantDiscountRow> discountByVariantId = new HashMap<>();
        if (!variantIds.isEmpty()) {
            for (DiscountRepository.ActiveVariantDiscountRow row :
                    discountRepository.findActiveDiscountRowsByVariantIds(variantIds, LocalDateTime.now())) {
                discountByVariantId.putIfAbsent(row.getVariantId(), row);
            }
        }

        // Group variants by product ID
        Map<Long, List<ProductVariant>> variantsByProductId = variants.stream()
                .collect(Collectors.groupingBy(v -> v.getProduct().getId()));

        // Build ProductDetailDto map
        Map<Long, ProductDetailDto> result = new LinkedHashMap<>();
        for (Product product : products) {
            List<ProductVariant> productVariants = variantsByProductId.getOrDefault(product.getId(), List.of());
            result.put(product.getId(), toProductDetailDto(product, productVariants, discountByVariantId));
        }

        return result;
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
    public Page<ProductVariant> getAllVariants(Pageable pageable) {
        return productVariantRepository.findAll(pageable);
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

    private ProductDetailDto toProductDetailDto(
            Product product,
            List<ProductVariant> variants,
            Map<Long, DiscountRepository.ActiveVariantDiscountRow> discountByVariantId
    ) {
        Map<String, Object> productAttributes = new LinkedHashMap<>();
        if (product instanceof Mobile mobile) {
            productAttributes.put("model", mobile.getModel());
            productAttributes.put("screenSize", mobile.getScreenSize());
            productAttributes.put("resolution", mobile.getResolution());
            productAttributes.put("camera", mobile.getCamera());
            productAttributes.put("battery", mobile.getBattery());
            productAttributes.put("dimension", mobile.getDimension());
        } else if (product instanceof Watches watch) {
            productAttributes.put("model", watch.getModel());
            productAttributes.put("gender", watch.getGender());
            productAttributes.put("material", watch.getMaterial());
            productAttributes.put("batteryLife", watch.getBatteryLife());
            productAttributes.put("screenSize", watch.getScreenSize());
            productAttributes.put("gps", watch.getGps());
            productAttributes.put("weight", watch.getWeight());
        } else if (product instanceof Laptop laptop) {
            productAttributes.put("cpu", laptop.getCpu());
            productAttributes.put("ram", laptop.getRam());
            productAttributes.put("storage", laptop.getStorage());
            productAttributes.put("gpu", laptop.getGpu());
            productAttributes.put("resolution", laptop.getResolution());
            productAttributes.put("screenSize", laptop.getScreenSize());
            productAttributes.put("dimension", laptop.getDimension());
        } else if (product instanceof Television television) {
            productAttributes.put("resolution", television.getResolution());
            productAttributes.put("refreshRate", television.getRefreshRate());
            productAttributes.put("screenSize", television.getScreenSize());
            productAttributes.put("weight", television.getWeight());
            productAttributes.put("warrantyMonths", television.getWarrantyMonths());
        }

        List<ProductVariantDto> variantDtos = variants.stream()
                .sorted(Comparator.comparing(ProductVariant::getId))
                .map(variant -> toProductVariantDto(variant, discountByVariantId.get(variant.getId())))
                .toList();

        return new ProductDetailDto(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getDescription(),
                product.getProductType(),
                product.getThumbnail(),
                product.getCreated(),
                productAttributes,
                variantDtos
        );
    }

    private ProductVariantDto toProductVariantDto(
            ProductVariant variant,
            DiscountRepository.ActiveVariantDiscountRow discountRow
    ) {
        double originalPrice = variant.getPrice().doubleValue();
        double discountedPrice = originalPrice;

        if (discountRow != null) {
            if (discountRow.getDiscountType() == DiscountType.PERCENTAGE) {
                discountedPrice = originalPrice * (1 - discountRow.getDiscountValue() / 100d);
            } else {
                discountedPrice = Math.max(0d, originalPrice - discountRow.getDiscountValue());
            }
        }

        Color color = variant.getColor();
        ColorDto colorDto = color == null
                ? null
                : new ColorDto(color.getId(), color.getName(), color.getHexCode());

        return new ProductVariantDto(
                variant.getId(),
                variant.getSku(),
                originalPrice,
                variant.getStockQuantity(),
                variant.getActive(),
                variant.getIsDefault(),
                variant.getImage(),
                variant.getVersion(),
                colorDto,
                discountedPrice,
                discountRow == null ? null : discountRow.getDiscountType()
        );
    }

}
