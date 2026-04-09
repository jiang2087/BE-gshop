package com.example.demo.models;

import com.example.demo.Enums.OrderStatus;
import com.example.demo.models.junction.AddressSnapShot;
import com.example.demo.models.products.OrderItem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@Entity
@Table(name = "orders")
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @Embedded
    private AddressSnapShot shippingAddress;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(precision = 19, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(unique = true, nullable = false, length = 20)
    private String orderCode;

    @Column(nullable = false)
    private String paymentMethod; // "COD", "MOMO", "BANK_CARD", ...

    private String paymentTransactionId; // mã giao dịch từ cổng thanh toán

    private String note; // ghi chú

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL,  orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<OrderItem> items = new ArrayList<>();

    private BigDecimal totalPrice;

    @Version
    private Long version;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public void addItem(OrderItem item) {
        if (item != null) {
            this.items.add(item);
            item.setOrder(this);
        }
    }

    public void removeItem(OrderItem item) {
        if (item != null) {
            this.items.remove(item);
            item.setOrder(null);
        }
    }

    public BigDecimal calculateTotal() {
        BigDecimal subtotal = items.stream()
                .map(item -> item.getProductVariant().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return subtotal
                .subtract(discountAmount)
                .add(shippingFee);
    }

}

