package com.example.demo.controllers;

import com.example.demo.dto.request.VoucherRequest;
import com.example.demo.models.Voucher;
import com.example.demo.services.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import okhttp3.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping
    public ResponseEntity<List<Voucher>> getVoucher() {
        return ResponseEntity.ok(voucherService.getVoucher());
    }

    @GetMapping("/top-5/{userId}")
    public ResponseEntity<List<Voucher>> getTop5Voucher(@PathVariable Long userId) {
        return ResponseEntity.ok(voucherService.getTop5VoucherByUser(userId));
    }

    @GetMapping("/preview")
    public ResponseEntity<?> getPreviewVoucher(
            @RequestParam String code,
            @RequestParam Long userId,
            @RequestParam BigDecimal orderTotal
            ) {
        return ResponseEntity.ok(voucherService.previewVoucher(code, userId, orderTotal));
    }

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
            @RequestParam BigDecimal orderTotal
    ) {
        var discount = voucherService.applyVoucher(code, userId, orderTotal);
        return ResponseEntity.ok(discount);
    }


}