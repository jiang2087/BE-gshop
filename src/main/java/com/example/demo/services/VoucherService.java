package com.example.demo.services;

import com.example.demo.Enums.DiscountType;
import com.example.demo.dto.request.VoucherRequest;
import com.example.demo.models.User;
import com.example.demo.models.Voucher;
import com.example.demo.models.junction.UserVoucher;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserVoucherRepository;
import com.example.demo.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final UserRepository userRepository;

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
    public Double applyVoucher(String code, Long userId, Double orderTotal, Double shippingFee) {

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

        if (orderTotal < voucher.getMinOrderValue()) {
            throw new RuntimeException("Not enough order value");
        }

        UserVoucher uv = userVoucherRepository
                .findByUserIdAndVoucherCode(userId, code)
                .orElseThrow(() -> new RuntimeException("User does not own voucher"));

        if (uv.getUsed()) {
            throw new RuntimeException("Voucher already used");
        }

        double discountAmount = getDiscountAmount(orderTotal, shippingFee, voucher);

        // update usage
        uv.setUsed(true);
        uv.setUsedAt(now);

        voucher.setUsedCount(voucher.getUsedCount() + 1);

        return discountAmount;
    }

    private static double getDiscountAmount(Double orderTotal, Double shippingFee, Voucher voucher) {
        double discountAmount = 0;

        switch (voucher.getType()) {
            case FREE_SHIP:
                discountAmount = shippingFee;
                break;

            case ORDER_DISCOUNT:
                if (voucher.getDiscountType() == DiscountType.PERCENTAGE) {
                    discountAmount = orderTotal * voucher.getValue() / 100;
                } else {
                    discountAmount = voucher.getValue();
                }
                break;

            case PRODUCT_DISCOUNT:
                discountAmount = voucher.getValue();
                break;
        }

        if (voucher.getMaxDiscount() != null) {
            discountAmount = Math.min(discountAmount, voucher.getMaxDiscount());
        }
        return discountAmount;
    }
}