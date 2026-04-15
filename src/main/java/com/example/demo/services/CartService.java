package com.example.demo.services;

import com.example.demo.dto.response.CartItemResponse;
import com.example.demo.models.Cart;
import com.example.demo.models.products.CartItem;
import com.example.demo.models.products.ProductVariant;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.products.CartItemRepository;
import com.example.demo.repository.products.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public CartItemResponse addToCart(String cartKey, Long variantId, Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }

        Cart cart = cartRepository.findByCartKey(cartKey)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setCartKey(cartKey);
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
}
