package com.example.demo.services;

import com.example.demo.Enums.CartStatus;
import com.example.demo.dto.response.CartItemResponse;
import com.example.demo.models.Cart;
import com.example.demo.models.User;
import com.example.demo.models.products.CartItem;
import com.example.demo.models.products.ProductVariant;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.products.CartItemRepository;
import com.example.demo.repository.products.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    @Transactional
    public CartItemResponse addToCart(String cartKey, Long variantId, Integer quantity) {
        return addToCart(cartKey, null, variantId, quantity);
    }

    @Transactional
    public CartItemResponse addToCart(String cartKey, Long userId, Long variantId, Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }

        Cart cart = resolveCart(cartKey, userId);

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Product variant not found"));

        int updated = cartItemRepository.increaseQuantity(cart.getId(), variantId, quantity);

        CartItem item;

        if (updated > 0) {
            item = cartItemRepository
                    .findByCartIdAndProductVariantId(cart.getId(), variantId)
                    .orElseThrow();
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProductVariant(variant);
            newItem.setQuantity(quantity);
            newItem.setPrice(variant.getPrice());
            newItem.setCreatedAt(LocalDateTime.now());

            item = cartItemRepository.save(newItem);
        }

        return new CartItemResponse(
                cart.getId(),
                item.getId(),
                variant.getId(),
                variant.getImage(),
                variant.getSku(),
                variant.getColor().getHexCode(),
                variant.getColor().getName(),
                item.getPrice(),
                item.getQuantity(),
                cart.getCartKey()
        );
    }

    private Cart resolveCart(String cartKey, Long userId) {
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                    .orElseGet(() -> {
                        Cart c = new Cart();
                        c.setUser(user);
                        c.setStatus(CartStatus.ACTIVE);
                        c.setCartKey(buildUserCartKey(userId));
                        return cartRepository.save(c);
                    });
        }

        if (cartKey == null || cartKey.isBlank()) {
            throw new IllegalArgumentException("cartKey is required for guest cart");
        }

        return cartRepository.findByCartKey(cartKey)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setCartKey(cartKey);
                    c.setStatus(CartStatus.ACTIVE);
                    return cartRepository.save(c);
                });
    }

    private String buildUserCartKey(Long userId) {
        return "user_" + userId;
    }

    public List<CartItemResponse> getCartItems(Long userId) {
        return cartItemRepository.getCartItemsByUserId(userId);
    }

    @Transactional
    public void updateQuantity(Long cartItemId, Integer quantity) {


        if (quantity <= 0) {
            cartItemRepository.deleteById(cartItemId);
            return;
        }

        int updated = cartItemRepository.updateQuantity(cartItemId, quantity);

        if (updated == 0) {
            throw new RuntimeException("Cart item not found");
        }
    }

    @Transactional
    public void mergeCart(String cartKey, Long userId) {

        Cart userCart = cartRepository
                .findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElse(null);

        Cart guestCart = cartRepository.findByCartKey(cartKey)
                .orElse(null);

        if (guestCart == null) return;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (userCart == null) {
            guestCart.setUser(user);
            cartRepository.save(guestCart);
            return;
        }

        if (guestCart.getId().equals(userCart.getId())) {
            return;
        }

        Map<Long, CartItem> userItemMap = new HashMap<>();
        for (CartItem item : userCart.getItems()) {
            userItemMap.put(item.getProductVariant().getId(), item);
        }

        for (CartItem guestItem : guestCart.getItems()) {
            Long variantId = guestItem.getProductVariant().getId();
            CartItem existingItem = userItemMap.get(variantId);

            if (existingItem != null) {
                int newQuantity = existingItem.getQuantity() + guestItem.getQuantity();
                int stock = existingItem.getProductVariant().getStockQuantity();

                if (newQuantity > stock) {
                    newQuantity = stock;
                }
                existingItem.setQuantity(newQuantity);
            } else {
                CartItem newItem = new CartItem();
                newItem.setCart(userCart);
                newItem.setProductVariant(guestItem.getProductVariant());
                newItem.setQuantity(guestItem.getQuantity());
                newItem.setPrice(guestItem.getPrice());
                newItem.setCreatedAt(LocalDateTime.now());
                userCart.getItems().add(newItem);
            }
        }

        cartRepository.save(userCart);
        cartRepository.delete(guestCart);
    }
    @Transactional
    public void removeItem(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Transactional
    public void clearCart(Long cartId) {
        cartItemRepository.deleteByCartId(cartId);
    }

    public BigDecimal calculateTotal(Long cartId) {
        return cartItemRepository.getTotal(cartId);
    }
    private CartItem cloneItem(CartItem item) {
        CartItem newItem = new CartItem();
        newItem.setCart(item.getCart());
        newItem.setProductVariant(item.getProductVariant());
        newItem.setQuantity(item.getQuantity());
        newItem.setPrice(item.getPrice());
        return newItem;
    }
}
