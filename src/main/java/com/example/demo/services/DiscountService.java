package com.example.demo.services;

import com.example.demo.Enums.DiscountType;
import com.example.demo.dto.request.DiscountRequest;
import com.example.demo.dto.response.DiscountResponse;
import com.example.demo.models.Discount;
import com.example.demo.models.products.ProductVariant;
import com.example.demo.repository.DiscountRepository;
import com.example.demo.repository.products.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiscountService{

    private final DiscountRepository discountRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public DiscountResponse createDiscount(DiscountRequest request) {
        Discount discount = new Discount();
        discount.setName(request.name());
        discount.setType(request.type());
        discount.setValue(request.value());
        discount.setStartDate(request.startDate());
        discount.setEndDate(request.endDate());
        discount.setActive(request.active());

        return toResponse(discountRepository.save(discount));
    }

    @Transactional
    public DiscountResponse updateDiscount(Long discountId, DiscountRequest request) {
        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new RuntimeException("Discount not found"));

        discount.setName(request.name());
        discount.setType(request.type());
        discount.setValue(request.value());
        discount.setStartDate(request.startDate());
        discount.setEndDate(request.endDate());
        discount.setActive(request.active());

        return toResponse(discountRepository.save(discount));
    }

    @Transactional
    public void applyDiscountToProducts(Long discountId, List<Long> productVariantIds) {
        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new RuntimeException("Discount not found"));

        List<ProductVariant> products = productVariantRepository.findAllById(productVariantIds);

        discount.setProductVariants(products);
        discountRepository.save(discount);
    }

    @Transactional
    public void addVariantDiscount(Long discountId, List<Long> productVariantIds) {
        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new RuntimeException("Discount not found"));

        List<ProductVariant> variantsToAdd = productVariantRepository.findAllById(productVariantIds);
        Set<ProductVariant> mergedVariants = new HashSet<>(discount.getProductVariants());
        mergedVariants.addAll(variantsToAdd);
        discount.setProductVariants(new ArrayList<>(mergedVariants));
        discountRepository.save(discount);
    }

    public Page<DiscountResponse> getDiscounts(Boolean active, String name, Pageable pageable) {
        return discountRepository.findByActiveAndName(active, name, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public void deleteDiscount(Long discountId) {
        if (!discountRepository.existsById(discountId)) {
            throw new RuntimeException("Discount not found");
        }
        discountRepository.deleteById(discountId);
    }

    public Double calculateDiscountPrice(Long productVariantId, Double originalPrice) {
        List<Discount> discounts = discountRepository
                .findActiveDiscountByProduct(productVariantId, LocalDateTime.now());

        if (discounts.isEmpty()) return originalPrice;

        Discount discount = discounts.getFirst();

        if (discount.getType() == DiscountType.PERCENTAGE) {
            return originalPrice * (1 - discount.getValue() / 100);
        } else {
            return originalPrice - discount.getValue();
        }
    }

    public Page<ProductVariant> getVariantNotInDisCount(Long discountId, String sku, Pageable pageable) {
        if (!discountRepository.existsById(discountId)) {
            throw new RuntimeException("Discount not found");
        }
        return discountRepository.findProductVariantsNotInDiscount(discountId, sku, pageable);
    }

    public Page<ProductVariant> getVariantInDiscount(Long discountId, String sku, Pageable pageable) {
        if (!discountRepository.existsById(discountId)) {
            throw new RuntimeException("Discount not found");
        }
        return discountRepository.findProductVariantsInDiscount(discountId, sku, pageable);
    }

    public Map<Long, Double> getDiscountedValuesOfVariants(Long discountId) {
        Discount discount = discountRepository.findByIdWithProductVariants(discountId)
                .orElseThrow(() -> new RuntimeException("Discount not found"));

        Map<Long, Double> discountedValues = new LinkedHashMap<>();
        for (ProductVariant variant : discount.getProductVariants()) {
            double originalPrice = variant.getPrice().doubleValue();
            double discountedPrice;

            if (discount.getType() == DiscountType.PERCENTAGE) {
                log.info("discount value: {}", discount.getValue());
                discountedPrice = originalPrice *
                        ((100 - discount.getValue()) / 100);

            } else {

                discountedPrice = Math.max(
                        0d,
                        originalPrice - discount.getValue()
                );
            }

            discountedPrice = BigDecimal
                    .valueOf(discountedPrice)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
            discountedValues.put(variant.getId(), discountedPrice);
        }
        return discountedValues;
    }

    @Transactional
    public void deleteVariantInDiscount(Long discountId, Long variantId) {
        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new RuntimeException("Discount not found"));

        List<ProductVariant> currentVariants = new ArrayList<>(discount.getProductVariants());
        currentVariants.removeIf(variant -> variantId.equals(variant.getId()));
        discount.setProductVariants(currentVariants);
        discountRepository.save(discount);
    }

    private DiscountResponse toResponse(Discount discount) {
        return new DiscountResponse(
                discount.getId(),
                discount.getName(),
                discount.getType(),
                discount.getValue(),
                discount.getStartDate(),
                discount.getEndDate(),
                discount.getActive()
        );
    }
}
