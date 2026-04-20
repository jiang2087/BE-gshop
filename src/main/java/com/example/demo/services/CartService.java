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

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }

        Cart cart = cartRepository.findByCartKey(cartKey)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setCartKey(cartKey);
                    c.setStatus(CartStatus.ACTIVE);
                    return cartRepository.save(c);
                });

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

    public List<CartItemResponse> getCartItems(Long cartId) {
        return cartItemRepository.getCartItemsByCartId(cartId);
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

        Cart guestCart = cartRepository.findByCartKey(cartKey).orElse(null);
        if (guestCart == null) return;

        Cart userCart = cartRepository
                .findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElse(null);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Case: user don't have cart set userId
        if (userCart == null) {
            guestCart.setUser(user);
            cartRepository.save(guestCart);
            return;
        }

        // Case: avoid merge itself => if duplicate id then return
        if (guestCart.getId().equals(userCart.getId())) {
            return;
        }

        // Case: merge 2 cart(guest and user) when user login

        Map<Long, CartItem> map = new HashMap<>();
        // create map from guest cart
        for (CartItem item : guestCart.getItems()) {
            map.put(
                    item.getProductVariant().getId(),
                    cloneItem(item)
            );
        }
        // merge user cart into map
        for (CartItem item : userCart.getItems()) {

            Long key = item.getProductVariant().getId();

            if (map.containsKey(key)) {

                CartItem existing = map.get(key);
                // valid stock for product Variant
                int mergedQty = existing.getQuantity() + item.getQuantity();

                int stock = item.getProductVariant().getStockQuantity();

                if (mergedQty > stock) {
                    throw new RuntimeException(
                            "Not enough stock for product variant: "
                                    + item.getProductVariant().getId()
                    );
                }
                existing.setQuantity(mergedQty);

            } else {

                map.put(key, cloneItem(item));
            }
        }
        List<CartItem> mergedList = new ArrayList<>(map.values());
        userCart.setItems(mergedList);
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
