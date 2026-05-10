package com.example.demo.controllers;

import com.example.demo.dto.request.VoucherRequest;
import com.example.demo.dto.response.VoucherResponse;
import com.example.demo.services.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    public ResponseEntity<?> getVoucher(
            @RequestParam(required = false) String voucherCode,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        return ResponseEntity.ok(voucherService.getVoucher(voucherCode, active, pageable));
    }

    @GetMapping("/top-5/{userId}")
    public ResponseEntity<List<VoucherResponse>> getTop5Voucher(@PathVariable Long userId) {
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
    public ResponseEntity<VoucherResponse> create(@RequestBody @Valid VoucherRequest request) {
        return ResponseEntity.ok(voucherService.createVoucher(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VoucherResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid VoucherRequest request
    ) {
        return ResponseEntity.ok(voucherService.updateVoucher(id, request));
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
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.noContent().build();
    }
}

