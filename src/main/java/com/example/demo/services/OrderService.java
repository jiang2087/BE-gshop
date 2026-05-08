package com.example.demo.services;

import com.example.demo.Enums.OrderStatus;
import com.example.demo.dto.request.PlaceOrderRequest;
import com.example.demo.dto.response.OrderAdminResponse;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.temporal.TemporalAdjusters;
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
    private final DateTimeService dateTimeService;

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

            String note = request.note() != null ? request.note() : "";
            order.setNote(note);
            order.setShippingFee(BigDecimal.valueOf(1.2));

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

    public BigDecimal getProfitThisMonth() {
        LocalDateTime startOfMonth = dateTimeService.getStartOfMonth();
        LocalDateTime startOfNextMonth = dateTimeService.getStartOfNextMonth();
        return orderRepository.getTotalProfitThisMonth(startOfMonth, startOfNextMonth);
    }


    public Map<Integer, BigDecimal> getProfitPerMonth() {
        int year = LocalDate.now().getYear();
        List<Object[]> rows = orderRepository.getProfitPerMonth(year);

        Map<Integer, BigDecimal> profitByMonth = new LinkedHashMap<>();
        for (int month = 1; month <= 12; month++) {
            profitByMonth.put(month, BigDecimal.ZERO);
        }

        for (Object[] row : rows) {
            Integer month = (Integer) row[0];
            BigDecimal total = (BigDecimal) row[1];
            profitByMonth.put(month, total);
        }

        return profitByMonth;
    }

    public Map<Integer, BigDecimal> getProfitPerWeek() {
        int year = LocalDate.now().getYear();
        List<Object[]> rows = orderRepository.getProfitPerWeek(year);

        Map<Integer, BigDecimal> profitByWeek = new LinkedHashMap<>();
        for (int week = 1; week <= 53; week++) {
            profitByWeek.put(week, BigDecimal.ZERO);
        }

        for (Object[] row : rows) {
            Integer week = (Integer) row[0];
            BigDecimal total = (BigDecimal) row[1];
            profitByWeek.put(week, total);
        }

        return profitByWeek;
    }


    public List<Map<String, Object>> getRevenueByDayOfWeek(String timeFrame) {
        LocalDate week;
        if("last week".equalsIgnoreCase(timeFrame)){
            week = LocalDate.now().minusWeeks(1);
        }else{
            week = LocalDate.now();
        }

        List<Object[]> rawData = orderRepository.getRevenueByDayOfWeek(week);

        Map<Integer, Double> map = new HashMap<>();
        for (Object[] row : rawData) {
            Integer day = ((Number) row[0]).intValue();
            Double total = ((Number) row[1]).doubleValue();
            map.put(day, total);
        }

        List<Integer> order = List.of(2,3,4,5,6,7,1);

        Map<Integer, String> dayMap = Map.of(
                1, "Sun",
                2, "Mon",
                3, "Tue",
                4, "Wed",
                5, "Thu",
                6, "Fri",
                7, "Sat"
        );

        List<Map<String, Object>> result = new ArrayList<>();

        for (Integer d : order) {
            Map<String, Object> item = new HashMap<>();
            item.put("x", dayMap.get(d)); // cho chart
            item.put("y", map.getOrDefault(d, 0.0));

            result.add(item);
        }

        return result;
    }

    public List<OrderAdminResponse> getAllOrder() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(order -> new OrderAdminResponse(
                        order.getId(),
                        order.getOrderCode(),
                        order.getShippingAddress() != null ? order.getShippingAddress().getRecipientName() : null,
                        order.getShippingAddress() != null ? order.getShippingAddress().getPhone() : null,
                        order.getTotalPrice(),
                        order.getPaymentMethod(),
                        order.getStatus(),
                        order.getCreatedAt()
                ))
                .toList();
    }

}
