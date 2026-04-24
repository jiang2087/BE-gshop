package com.example.demo.models;

import com.example.demo.Enums.DiscountType;
import com.example.demo.Enums.VoucherType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "vouchers")
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @Enumerated(EnumType.STRING)
    private VoucherType type;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    private Double value;

    private Double minOrderValue;

    private Double maxDiscount;

    private Integer quantity;

    private Integer usedCount;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Boolean active;
}