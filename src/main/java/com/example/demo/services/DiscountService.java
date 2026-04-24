package com.example.demo.services;

import com.example.demo.Enums.DiscountType;
import com.example.demo.dto.request.DiscountRequest;
import com.example.demo.models.Discount;
import com.example.demo.models.products.ProductVariant;
import com.example.demo.repository.DiscountRepository;
import com.example.demo.repository.products.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiscountService{

    private final DiscountRepository discountRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public Discount createDiscount(DiscountRequest request) {
        Discount discount = new Discount();
        discount.setName(request.name());
        discount.setType(request.type());
        discount.setValue(request.value());
        discount.setStartDate(request.startDate());
        discount.setEndDate(request.endDate());
        discount.setActive(true);

        return discountRepository.save(discount);
    }

    @Transactional
    public void applyDiscountToProducts(Long discountId, List<Long> productVariantIds) {
        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new RuntimeException("Discount not found"));

        List<ProductVariant> products = productVariantRepository.findAllById(productVariantIds);

        discount.setProductVariants(products);
        discountRepository.save(discount);
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
}