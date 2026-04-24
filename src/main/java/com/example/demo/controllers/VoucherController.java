package com.example.demo.controllers;

import com.example.demo.dto.request.VoucherRequest;
import com.example.demo.services.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid VoucherRequest request) {
        return ResponseEntity.ok(voucherService.createVoucher(request));
    }

    @PostMapping("/collect")
    public ResponseEntity<?> collect(
            @RequestParam Long userId,
            @RequestParam String code
    ) {
        voucherService.collectVoucher(userId, code);
        return ResponseEntity.ok("Collected");
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyVoucher(
            @RequestParam Long userId,
            @RequestParam String code,
            @RequestParam Double orderTotal,
            @RequestParam Double shippingFee
    ) {
        Double discount = voucherService.applyVoucher(code, userId, orderTotal, shippingFee);
        return ResponseEntity.ok(discount);
    }
}