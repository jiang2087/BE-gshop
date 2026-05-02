package com.example.demo.services;

import com.example.demo.Enums.DiscountType;
import com.example.demo.Enums.VoucherErrorCode;
import com.example.demo.dto.request.VoucherRequest;
import com.example.demo.exceptions.VoucherException;
import com.example.demo.models.User;
import com.example.demo.models.Voucher;
import com.example.demo.models.junction.UserVoucher;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserVoucherRepository;
import com.example.demo.repository.VoucherRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.ErrorResponseException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final UserRepository userRepository;
    private static  final BigDecimal shippingFee = BigDecimal.valueOf(1.2);

    @Transactional
    public Voucher createVoucher(VoucherRequest request) {
        Voucher v = new Voucher();
        v.setCode(request.code());
        v.setType(request.type());
        v.setDiscountType(request.discountType());
        v.setValue(request.value());
        v.setMinOrderValue(request.minOrderValue());
        v.setMaxDiscount(request.maxDiscount());
        v.setQuantity(request.quantity());
        v.setUsedCount(0);
        v.setStartDate(request.startDate());
        v.setEndDate(request.endDate());
        v.setActive(true);

        return voucherRepository.save(v);
    }

    public List<Voucher> getVoucher() {
        return voucherRepository.findAll();
    }

    public List<Voucher> getTop5VoucherByUser(Long userId){
        Pageable pageable = PageRequest.of(0, 5);
        return voucherRepository.findTopAvailableVouchers(userId, pageable);
    }


    @Transactional
    public void collectVoucher(Long userId, String code) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean exists = userVoucherRepository.existsByUserAndVoucher(user, voucher);
        if (exists) {
            throw new RuntimeException("Already collected");
        }

        UserVoucher uv = new UserVoucher();
        uv.setUser(user);
        uv.setVoucher(voucher);
        uv.setUsed(false);

        userVoucherRepository.save(uv);
    }

    @Transactional
    public BigDecimal applyVoucher(String code, Long userId, BigDecimal orderTotal) {

        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));

        if (!voucher.getActive()) throw new RuntimeException("Voucher inactive");

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(voucher.getStartDate()) || now.isAfter(voucher.getEndDate())) {
            throw new RuntimeException("Voucher expired");
        }

        if (voucher.getUsedCount() >= voucher.getQuantity()) {
            throw new RuntimeException("Voucher out of stock");
        }

        if (orderTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new VoucherException(VoucherErrorCode.MIN_NOT_MET);
        }

        UserVoucher uv = userVoucherRepository
                .findByUserIdAndVoucherCode(userId, code)
                .orElseThrow(() -> new RuntimeException("User does not own voucher"));

        if (uv.getUsed()) {
            throw new RuntimeException("Voucher already used");
        }
        int updated = voucherRepository.incrementUsage(code);

        if (updated == 0) {
            throw new RuntimeException("Voucher out of stock");
        }

        BigDecimal discountAmount = getDiscountAmount(orderTotal, voucher);

        // update usage
        uv.setUsed(true);
        uv.setUsedAt(now);

        return discountAmount;
    }

    public BigDecimal previewVoucher(String code, Long userId, BigDecimal orderTotal) {

        Voucher voucher1 = validateVoucher(code, userId, orderTotal);

        return getDiscountAmount(orderTotal, voucher1);
    }

    private Voucher validateVoucher(String code, Long userId, BigDecimal orderTotal) {

        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new VoucherException(VoucherErrorCode.NOT_FOUND));

        if (!Boolean.TRUE.equals(voucher.getActive())) {
            throw new VoucherException(VoucherErrorCode.INACTIVE);
        }

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(voucher.getStartDate()) || now.isAfter(voucher.getEndDate())) {
            throw new VoucherException(VoucherErrorCode.EXPIRED);
        }

        if (voucher.getUsedCount() >= voucher.getQuantity()) {
            throw new VoucherException(VoucherErrorCode.OUT_OF_STOCK);
        }

        if (orderTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new VoucherException(VoucherErrorCode.MIN_NOT_MET);
        }

        UserVoucher uv = userVoucherRepository
                .findByUserIdAndVoucherCode(userId, code)
                .orElseThrow(() -> new VoucherException(VoucherErrorCode.NOT_OWNED));

        if (Boolean.TRUE.equals(uv.getUsed())) {
            throw new VoucherException(VoucherErrorCode.ALREADY_USED);
        }

        return voucher;
    }

    private static BigDecimal getDiscountAmount(
            BigDecimal orderTotal,
            Voucher voucher
    ) {

        BigDecimal discount = BigDecimal.ZERO;

        if (voucher == null) return discount;

        switch (voucher.getType()) {

            case FREE_SHIP:
                discount = shippingFee;
                break;

            case ORDER_DISCOUNT:
                if (voucher.getDiscountType() == DiscountType.PERCENTAGE) {
                    discount = orderTotal
                            .multiply(safe(voucher.getValue()))
                            .divide(BigDecimal.valueOf(100));
                } else {
                    discount = safe(voucher.getValue());
                }
                break;

            case PRODUCT_DISCOUNT:
                discount = safe(voucher.getValue());
                break;

            default:
                throw new VoucherException(VoucherErrorCode.INVALID_TYPE);
        }

        // apply max discount
        if (voucher.getMaxDiscount() != null) {
            discount = discount.min((voucher.getMaxDiscount()));
        }

        // ensure discount does not exceed total order amount (order + shipping)
        BigDecimal maxAllowed = orderTotal.add(safe(shippingFee));
        discount = discount.min(maxAllowed);

        // prevent negative discount
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            discount = BigDecimal.ZERO;
        }

        return discount;
    }
    private static BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}