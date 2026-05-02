package com.example.demo.services;

import com.example.demo.Enums.OrderStatus;
import com.example.demo.dto.request.PlaceOrderRequest;
import com.example.demo.models.Address;
import com.example.demo.models.junction.AddressSnapShot;
import com.example.demo.models.Order;
import com.example.demo.models.User;
import com.example.demo.models.products.OrderItem;
import com.example.demo.models.products.ProductVariant;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.OrderRepository;

import com.example.demo.repository.UserRepository;
import com.example.demo.repository.products.ProductVariantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    private final VoucherService voucherService;
    private final CartRepository cartRepository;

    @Transactional
    public Order placeOrder(PlaceOrderRequest request) {
        try {
            // 1. Validate user
            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            // 2. Validate address of user
            Address address = addressRepository.findByIdAndUserId(request.addressId(), request.userId())
                    .orElseThrow(() -> new EntityNotFoundException("Address not found"));

            // 3. create Order
            Order order = new Order();
            order.setUser(user);
            order.setShippingAddress(AddressSnapShot.from(address)); // snapshot địa chỉ
            order.setPaymentMethod(request.paymentMethod() != null ? request.paymentMethod() : "COD");
            order.setNote(request.note() != null ? request.note() : "");
            order.setStatus(OrderStatus.PENDING);
            order.setShippingFee(BigDecimal.ZERO);
            order.setDiscountAmount(BigDecimal.ZERO);

            // 4. handle items + check stock + checkout
            BigDecimal subtotal = BigDecimal.ZERO;

            List<Long> variantIds = request.items().stream()
                    .map(PlaceOrderRequest.OrderItemRequest::variantId)
                    .toList();
            // Just one query to retrieve all variants and products.
            List<ProductVariant> variants = productVariantRepository
                    .findAllWithProductByIdIn(variantIds);

            //Create a map for quick searching by ID)
            Map<Long, ProductVariant> variantMap = variants.stream()
                    .collect(Collectors.toMap(ProductVariant::getId, v -> v));
            for (PlaceOrderRequest.OrderItemRequest itemReq : request.items()) {
                ProductVariant variant = variantMap.get(itemReq.variantId());

                if (variant == null) {
                    throw new EntityNotFoundException("can not find a variant has id: " + itemReq.variantId());
                }

                // check stock
                if (variant.getStockQuantity() < itemReq.quantity()) {
                    throw new IllegalStateException(
                            String.format("Không đủ hàng cho sản phẩm %s (SKU: %s). Còn: %d, yêu cầu: %d",
                                    variant.getProduct().getName(),
                                    variant.getSku(),
                                    variant.getStockQuantity(),
                                    itemReq.quantity()));
                }

                // create OrderItem
                OrderItem orderItem = new OrderItem();
                orderItem.setProductVariant(variant);
                orderItem.setQuantity(itemReq.quantity());
                orderItem.setPrice(variant.getPrice());   // snapshot price

                order.addItem(orderItem);

                // decrease stock
                variant.setStockQuantity(variant.getStockQuantity() - itemReq.quantity());

                // subtotal
                subtotal = subtotal.add(
                        variant.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity()))
                );
            }

            // 5. handle voucher
            BigDecimal discount = BigDecimal.ZERO;
            if (request.voucherCode() != null && !request.voucherCode().isBlank()) {
                discount = voucherService.applyVoucher(request.voucherCode(), request.userId(), subtotal);
            }
            order.setDiscountAmount(discount);

            //finalTotal =  subtotal - discount + shippingFee (default value is 1.2 dollars)
            BigDecimal finalTotal = subtotal.subtract(discount).add(BigDecimal.valueOf(1.2));
            order.setTotalPrice(finalTotal);
            order.setOrderCode(generateOrderCode());
            cartRepository.deleteByUserId(user.getId());
            return orderRepository.save(order);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new IllegalStateException("The product has just been changed, please try again");
        }
    }


    @Transactional
    public void DeleteOrder(Long orderId) {
        orderRepository.deleteById(orderId);
    }

    public String generateOrderCode() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        return "ORD-" + date + "-" + randomCode(4);
    }

    private String randomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

}