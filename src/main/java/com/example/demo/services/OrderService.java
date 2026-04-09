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
import com.example.demo.repository.OrderRepository;

import com.example.demo.repository.UserRepository;
import com.example.demo.repository.products.ProductVariantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional
    public Order placeOrder(PlaceOrderRequest request){


        // 1. Validate user
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 2. Validate địa chỉ thuộc user
        Address address = addressRepository.findById(request.addressId())
                .orElseThrow(() -> new EntityNotFoundException("Address not found"));
        // 3. Tạo Order
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(AddressSnapShot.from(address)); // snapshot địa chỉ
        order.setPaymentMethod(request.paymentMethod() != null ? request.paymentMethod() : "COD");
        order.setNote(request.note() != null ? request.note() : "");
        order.setStatus(OrderStatus.PENDING);
        order.setShippingFee(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);

        // 4. Xử lý items + kiểm tra stock + tính tiền
        BigDecimal subtotal = BigDecimal.ZERO;

        List<Long> variantIds = request.items().stream()
                .map(PlaceOrderRequest.OrderItemRequest::variantId)
                .toList();
        // Chỉ 1 query lấy tất cả variant + product
        List<ProductVariant> variants = productVariantRepository
                .findAllWithProductByIdIn(variantIds);

        // Tạo Map để tra cứu nhanh theo id (performance tốt)
        Map<Long, ProductVariant> variantMap = variants.stream()
                .collect(Collectors.toMap(ProductVariant::getId, v -> v));
        for (PlaceOrderRequest.OrderItemRequest itemReq : request.items()) {
            ProductVariant variant = variantMap.get(itemReq.variantId());

            if (variant == null) {
                throw new EntityNotFoundException("Không tìm thấy biến thể sản phẩm: " + itemReq.variantId());
            }

            // Kiểm tra tồn kho
            if (variant.getStockQuantity() < itemReq.quantity()) {
                throw new IllegalStateException(
                        String.format("Không đủ hàng cho sản phẩm %s (SKU: %s). Còn: %d, yêu cầu: %d",
                                variant.getProduct().getName(),
                                variant.getSku(),
                                variant.getStockQuantity(),
                                itemReq.quantity()));
            }

            // Tạo OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setProductVariant(variant);
            orderItem.setQuantity(itemReq.quantity());
            orderItem.setPrice(variant.getPrice());   // snapshot giá

            order.addItem(orderItem);

            // Giảm tồn kho
            variant.setStockQuantity(variant.getStockQuantity() - itemReq.quantity());

            // Tích lũy subtotal
            subtotal = subtotal.add(
                    variant.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity()))
            );
        }

          // 5. Xử lý voucher
//        BigDecimal discount = BigDecimal.ZERO;
//        if (request.voucherCode() != null && !request.voucherCode().isBlank()) {
//            discount = voucherService.applyVoucher(request.voucherCode(), subtotal, user);
//        }
//        order.setDiscountAmount(discount);

        // 6. Tính tổng
        order.setOrderCode(generateOrderCode());
        BigDecimal finalTotal = order.calculateTotal(); // subtotal - discount + shippingFee
        order.setTotalPrice(finalTotal);

        return orderRepository.save(order);
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