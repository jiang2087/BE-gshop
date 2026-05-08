package com.example.demo.services;

import com.example.demo.Enums.CartStatus;
import com.example.demo.dto.response.CartItemResponse;
import com.example.demo.models.Cart;
import com.example.demo.models.Color;
import com.example.demo.models.User;
import com.example.demo.models.products.CartItem;
import com.example.demo.models.products.ProductVariant;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.products.CartItemRepository;
import com.example.demo.repository.products.ProductVariantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductVariantRepository productVariantRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void addToCart_shouldCreateCartAndItem_whenNotExists() {
        String cartKey = "guest-1";
        Long variantId = 10L;
        Integer quantity = 2;

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setCartKey(cartKey);
        cart.setStatus(CartStatus.ACTIVE);

        ProductVariant variant = buildVariant(variantId, "SKU-10", "Red", "#ff0000", new BigDecimal("9.99"), "img.png", 5);

        CartItem savedItem = new CartItem();
        savedItem.setId(100L);
        savedItem.setCart(cart);
        savedItem.setProductVariant(variant);
        savedItem.setQuantity(quantity);
        savedItem.setPrice(variant.getPrice());

        when(cartRepository.findByCartKey(cartKey)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(productVariantRepository.findById(variantId)).thenReturn(Optional.of(variant));
        when(cartItemRepository.increaseQuantity(cart.getId(), variantId, quantity)).thenReturn(0);
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(savedItem);

        CartItemResponse result = cartService.addToCart(cartKey, variantId, quantity);

        assertEquals(1L, result.cartId());
        assertEquals(100L, result.cartItemId());
        assertEquals(variantId, result.productVariantId());
        assertEquals("SKU-10", result.sku());
        assertEquals("#ff0000", result.hexColor());
        assertEquals("Red", result.nameColor());
        assertEquals(quantity, result.quantity());
        verify(cartRepository).save(any(Cart.class));
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addToCart_shouldReturnExistingItem_whenIncreaseQuantitySuccess() {
        String cartKey = "guest-2";
        Long variantId = 20L;
        Integer quantity = 3;

        Cart cart = new Cart();
        cart.setId(2L);
        cart.setCartKey(cartKey);
        cart.setStatus(CartStatus.ACTIVE);

        ProductVariant variant = buildVariant(variantId, "SKU-20", "Blue", "#0000ff", new BigDecimal("15.00"), "img2.png", 10);

        CartItem existingItem = new CartItem();
        existingItem.setId(200L);
        existingItem.setCart(cart);
        existingItem.setProductVariant(variant);
        existingItem.setQuantity(7);
        existingItem.setPrice(variant.getPrice());

        when(cartRepository.findByCartKey(cartKey)).thenReturn(Optional.of(cart));
        when(productVariantRepository.findById(variantId)).thenReturn(Optional.of(variant));
        when(cartItemRepository.increaseQuantity(cart.getId(), variantId, quantity)).thenReturn(1);
        when(cartItemRepository.findByCartIdAndProductVariantId(cart.getId(), variantId)).thenReturn(Optional.of(existingItem));

        CartItemResponse result = cartService.addToCart(cartKey, variantId, quantity);

        assertEquals(200L, result.cartItemId());
        assertEquals(7, result.quantity());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addToCart_shouldThrow_whenQuantityInvalid() {
        assertThrows(IllegalArgumentException.class, () -> cartService.addToCart("k", 1L, 0));
        assertThrows(IllegalArgumentException.class, () -> cartService.addToCart("k", 1L, -1));
        assertThrows(IllegalArgumentException.class, () -> cartService.addToCart("k", 1L, null));
    }

    @Test
    void updateQuantity_shouldDeleteItem_whenQuantityLessOrEqualZero() {
        cartService.updateQuantity(99L, 0);
        verify(cartItemRepository).deleteById(99L);
        verify(cartItemRepository, never()).updateQuantity(anyLong(), anyInt());
    }

    @Test
    void updateQuantity_shouldUpdate_whenQuantityPositive() {
        when(cartItemRepository.updateQuantity(77L, 5)).thenReturn(1);

        cartService.updateQuantity(77L, 5);

        verify(cartItemRepository).updateQuantity(77L, 5);
    }

    @Test
    void updateQuantity_shouldThrow_whenItemNotFound() {
        when(cartItemRepository.updateQuantity(88L, 2)).thenReturn(0);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> cartService.updateQuantity(88L, 2));

        assertEquals("Cart item not found", ex.getMessage());
    }

    @Test
    void mergeCart_shouldAssignGuestCartToUser_whenUserCartNotExists() {
        String cartKey = "guest-3";
        Long userId = 3L;
        Cart guestCart = new Cart();
        guestCart.setId(30L);
        guestCart.setCartKey(cartKey);
        guestCart.setStatus(CartStatus.ACTIVE);
        User user = new User();
        user.setId(userId);

        when(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)).thenReturn(Optional.empty());
        when(cartRepository.findByCartKey(cartKey)).thenReturn(Optional.of(guestCart));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        cartService.mergeCart(cartKey, userId);

        assertEquals(user, guestCart.getUser());
        verify(cartRepository).save(guestCart);
        verify(cartRepository, never()).delete(any(Cart.class));
    }

    @Test
    void mergeCart_shouldMergeAndDeleteGuestCart() {
        String cartKey = "guest-4";
        Long userId = 4L;

        Cart userCart = new Cart();
        userCart.setId(40L);
        userCart.setStatus(CartStatus.ACTIVE);

        Cart guestCart = new Cart();
        guestCart.setId(41L);
        guestCart.setCartKey(cartKey);
        guestCart.setStatus(CartStatus.ACTIVE);

        ProductVariant v1 = buildVariant(101L, "SKU-101", "Black", "#000000", new BigDecimal("100"), "a.png", 5);
        ProductVariant v2 = buildVariant(102L, "SKU-102", "White", "#ffffff", new BigDecimal("50"), "b.png", 8);

        CartItem userItem = new CartItem();
        userItem.setId(501L);
        userItem.setCart(userCart);
        userItem.setProductVariant(v1);
        userItem.setQuantity(4);
        userItem.setPrice(new BigDecimal("100"));
        userCart.getItems().add(userItem);

        CartItem guestSameVariant = new CartItem();
        guestSameVariant.setId(601L);
        guestSameVariant.setCart(guestCart);
        guestSameVariant.setProductVariant(v1);
        guestSameVariant.setQuantity(3);
        guestSameVariant.setPrice(new BigDecimal("100"));
        guestCart.getItems().add(guestSameVariant);

        CartItem guestNewVariant = new CartItem();
        guestNewVariant.setId(602L);
        guestNewVariant.setCart(guestCart);
        guestNewVariant.setProductVariant(v2);
        guestNewVariant.setQuantity(2);
        guestNewVariant.setPrice(new BigDecimal("50"));
        guestCart.getItems().add(guestNewVariant);

        User user = new User();
        user.setId(userId);

        when(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)).thenReturn(Optional.of(userCart));
        when(cartRepository.findByCartKey(cartKey)).thenReturn(Optional.of(guestCart));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        cartService.mergeCart(cartKey, userId);

        assertEquals(5, userItem.getQuantity());
        assertEquals(2, userCart.getItems().size());
        assertTrue(userCart.getItems().stream().anyMatch(i -> i.getProductVariant().getId().equals(102L)));
        verify(cartRepository).save(userCart);
        verify(cartRepository).delete(guestCart);
    }

    @Test
    void mergeCart_shouldDoNothing_whenGuestCartNotFound() {
        when(cartRepository.findByUserIdAndStatus(5L, CartStatus.ACTIVE)).thenReturn(Optional.empty());
        when(cartRepository.findByCartKey("missing")).thenReturn(Optional.empty());

        cartService.mergeCart("missing", 5L);

        verify(userRepository, never()).findById(anyLong());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void getCartItems_shouldDelegateToRepository() {
        CartItemResponse response = new CartItemResponse(1L, 2L, 3L, "img", "SKU", "#fff", "White", BigDecimal.ONE, 1, "k");
        when(cartItemRepository.getCartItemsByUserId(7L)).thenReturn(List.of(response));

        List<CartItemResponse> result = cartService.getCartItems(7L);

        assertEquals(1, result.size());
        assertEquals("SKU", result.get(0).sku());
    }

    @Test
    void calculateTotal_shouldReturnRepositoryValue() {
        when(cartItemRepository.getTotal(9L)).thenReturn(new BigDecimal("123.45"));

        BigDecimal total = cartService.calculateTotal(9L);

        assertEquals(new BigDecimal("123.45"), total);
    }

    @Test
    void removeAndClear_shouldDelegateToRepository() {
        cartService.removeItem(111L);
        cartService.clearCart(222L);

        verify(cartItemRepository).deleteById(111L);
        verify(cartItemRepository).deleteByCartId(222L);
    }

    private ProductVariant buildVariant(Long id, String sku, String colorName, String colorHex, BigDecimal price, String image, int stock) {
        Color color = new Color();
        color.setName(colorName);
        color.setHexCode(colorHex);

        ProductVariant variant = new ProductVariant();
        variant.setId(id);
        variant.setSku(sku);
        variant.setColor(color);
        variant.setPrice(price);
        variant.setImage(image);
        variant.setStockQuantity(stock);
        return variant;
    }
}
