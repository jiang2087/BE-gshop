package com.example.demo.controllers;

import com.example.demo.Enums.OrderStatus;
import com.example.demo.config.UserDetailsImpl;
import com.example.demo.dto.request.PlaceOrderRequest;
import com.example.demo.dto.response.OrderAdminResponse;
import com.example.demo.dto.response.OrderUserResponse;
import com.example.demo.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody PlaceOrderRequest order) {
        return ResponseEntity.ok(orderService.placeOrder(order));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long orderId) {

        orderService.DeleteOrder(orderId);
        return ResponseEntity.ok("Successfully deleted order");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<OrderAdminResponse>> getAllOrder(
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable
    ) {
        if (orderCode != null && !orderCode.isBlank()) {
            return ResponseEntity.ok(orderService.getByOrderCode(orderCode, pageable));
        }
        if (status != null) {
            return ResponseEntity.ok(orderService.getByStatus(status, pageable));
        }
        return ResponseEntity.ok(orderService.getAllOrder(pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/profit/month")
    public ResponseEntity<?> getProfitThisMonth() {
        return ResponseEntity.ok(orderService.getProfitThisMonth());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/profit/per-month")
    public ResponseEntity<?> getProfitPerMonth() {
        return ResponseEntity.ok(orderService.getProfitPerMonth());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/profit/per-week")
    public ResponseEntity<?> getProfitPerWeek() {
        return ResponseEntity.ok(orderService.getProfitPerWeek());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/profit/top-purchasers")
    public ResponseEntity<?> getTopPurchasers(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(orderService.getTopPurchasers(pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/profit/per-day")
    public ResponseEntity<List<Map<String, Object>>> getRevenueByDayOfWeekBefore(
            @RequestParam(required = false, defaultValue = "currentWeek") String timeFrame
    ) {
        List<Map<String, Object>> data = orderService.getRevenueByDayOfWeek(timeFrame);
        return ResponseEntity.ok(data);
    }
    @GetMapping("/{orderId}/items")
    public ResponseEntity<?> getOrderItems(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderItemsByOrderId(orderId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrderByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrderByUserId(userId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<OrderUserResponse>> getMyOrders() {
        Long userId = getAuthenticatedUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(orderService.getMyOrders(userId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{orderId}/discount")
    public ResponseEntity<?> getDiscountByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getDiscountByOrderId(orderId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status
    ) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, status));
    }

    @PutMapping("/me/{orderId}/status")
    public ResponseEntity<?> updateMyOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status
    ) {
        Long userId = getAuthenticatedUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(orderService.updateStatusByUser(userId, orderId, status));
    }

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getId();
        }

        return null;
    }

}
