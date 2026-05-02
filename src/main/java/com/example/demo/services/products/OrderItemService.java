package com.example.demo.services.products;

import com.example.demo.models.Order;
import com.example.demo.models.products.OrderItem;
import com.example.demo.models.products.ProductVariant;
import com.example.demo.repository.OrderItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;


    public OrderItem getById(Long id) {
        return orderItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("OrderItem not found with id: " + id));
    }

    public List<OrderItem> getByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    public List<OrderItem> getByUserId(Long userId) {
        return orderItemRepository.findByUserId(userId);
    }

    public List<OrderItem> getByVariantId(Long variantId) {
        return orderItemRepository.findByProductVariantId(variantId);
    }

    public List<OrderItem> getByProductId(Long productId) {
        return orderItemRepository.findByProductId(productId);
    }

    public List<OrderItem> getAll() {
        return orderItemRepository.findAll();
    }

    @Transactional
    public OrderItem createOrderItem(Order order, ProductVariant productVariant,
                                     int quantity, BigDecimal price) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProductVariant(productVariant);
        item.setQuantity(quantity);
        item.setPrice(price);
        return orderItemRepository.save(item);
    }


    @Transactional
    public OrderItem updateQuantity(Long id, int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        OrderItem item = getById(id);
        item.setQuantity(newQuantity);
        return orderItemRepository.save(item);
    }


    @Transactional
    public OrderItem updatePrice(Long id, BigDecimal newPrice) {
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }
        OrderItem item = getById(id);
        item.setPrice(newPrice);
        return orderItemRepository.save(item);
    }


    @Transactional
    public void deleteById(Long id) {
        if (!orderItemRepository.existsById(id)) {
            throw new EntityNotFoundException("OrderItem not found with id: " + id);
        }
        orderItemRepository.deleteById(id);
    }


    @Transactional
    public void deleteByOrderId(Long orderId) {
        orderItemRepository.deleteByOrderId(orderId);
    }

    public BigDecimal calculateOrderSubtotal(Long orderId) {
        return getByOrderId(orderId).stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
