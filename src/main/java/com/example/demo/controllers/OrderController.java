package com.example.demo.controllers;

import com.example.demo.dto.request.PlaceOrderRequest;
import com.example.demo.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    public ResponseEntity<?> getAllOrder() {
        return ResponseEntity.ok(orderService.getAllOrder());
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
    @GetMapping("/profit/per-day")
    public ResponseEntity<List<Map<String, Object>>> getRevenueByDayOfWeekBefore(
            @RequestParam(required = false, defaultValue = "currentWeek") String timeFrame
    ) {
        List<Map<String, Object>> data = orderService.getRevenueByDayOfWeek(timeFrame);
        return ResponseEntity.ok(data);
    }

}
