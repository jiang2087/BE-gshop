package com.example.demo.services;

import com.example.demo.Enums.DiscountType;
import com.example.demo.Enums.VoucherErrorCode;
import com.example.demo.dto.request.VoucherRequest;
import com.example.demo.dto.response.UserVoucherResponse;
import com.example.demo.dto.response.VoucherResponse;
import com.example.demo.exceptions.VoucherException;
import com.example.demo.models.User;
import com.example.demo.models.Voucher;
import com.example.demo.models.junction.UserVoucher;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserVoucherRepository;
import com.example.demo.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final UserRepository userRepository;
    private static  final BigDecimal shippingFee = BigDecimal.valueOf(1.2);

    @Transactional
    public VoucherResponse createVoucher(VoucherRequest request) {
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
        v.setActive(request.active());

        return toResponse(voucherRepository.save(v));
    }

    @Transactional
    public VoucherResponse updateVoucher(Long voucherId, VoucherRequest request) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));

        voucher.setCode(request.code());
        voucher.setType(request.type());
        voucher.setDiscountType(request.discountType());
        voucher.setValue(request.value());
        voucher.setMinOrderValue(request.minOrderValue());
        voucher.setMaxDiscount(request.maxDiscount());
        voucher.setQuantity(request.quantity());
        voucher.setStartDate(request.startDate());
        voucher.setEndDate(request.endDate());
        voucher.setActive(request.active());

        return toResponse(voucherRepository.save(voucher));
    }

    public Page<VoucherResponse> getVoucher(String voucherCode, Boolean active, Pageable pageable) {
        return voucherRepository.searchVouchers(voucherCode, active, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public void deleteVoucher(Long voucherId) {
        if (!voucherRepository.existsById(voucherId)) {
            throw new RuntimeException("Voucher not found");
        }
        voucherRepository.deleteById(voucherId);
    }

    public List<VoucherResponse> getTop5VoucherByUser(Long userId){
        Pageable pageable = PageRequest.of(0, 5);
        return voucherRepository.findTopAvailableVouchers(userId, pageable).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<UserVoucherResponse> getUserUsableVouchers(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserVoucher> userVouchers = userVoucherRepository.findAllUnusedByUserId(userId);
        List<UserVoucherResponse> responses = new ArrayList<>();

        for (UserVoucher uv : userVouchers) {
            Voucher voucher = uv.getVoucher();

            responses.add(new UserVoucherResponse(
                    voucher.getId(),
                    voucher.getCode(),
                    voucher.getType(),
                    voucher.getDiscountType(),
                    voucher.getValue(),
                    voucher.getMinOrderValue(),
                    voucher.getMaxDiscount(),
                    voucher.getStartDate(),
                    voucher.getEndDate(),
                    voucher.getActive()
            ));
        }

        return responses;
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

    private VoucherResponse toResponse(Voucher voucher) {
        return new VoucherResponse(
                voucher.getId(),
                voucher.getCode(),
                voucher.getType(),
                voucher.getDiscountType(),
                voucher.getValue(),
                voucher.getMinOrderValue(),
                voucher.getMaxDiscount(),
                voucher.getQuantity(),
                voucher.getUsedCount(),
                voucher.getStartDate(),
                voucher.getEndDate(),
                voucher.getActive()
        );
    }
}
