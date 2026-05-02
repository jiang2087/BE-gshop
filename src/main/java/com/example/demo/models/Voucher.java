package com.example.demo.models;

import com.example.demo.Enums.DiscountType;
import com.example.demo.Enums.VoucherType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "vouchers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_voucher_code", columnNames = "code")
        }
)
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    private VoucherType type;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    @Column(precision = 10, scale = 2)
    private BigDecimal value;

    @Column(precision = 10, scale = 2)
    private BigDecimal minOrderValue;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxDiscount;

    private Integer quantity;

    private Integer usedCount;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Boolean active;
}