package com.example.demo.controllers;

import com.example.demo.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/{cartId}")
    public ResponseEntity<?> getCart(@PathVariable Long cartId) {
        return ResponseEntity.ok(cartService.getCartItems(cartId));
    }

    @GetMapping("/total/{cartId}")
    public ResponseEntity<?> getTotal(@PathVariable Long cartId) {
        return ResponseEntity.ok(cartService.calculateTotal(cartId));
    }

    @PostMapping
    public ResponseEntity<?> addToCart(@RequestParam String cartKey,
                                       @RequestParam Long productVariantId,
                                       @RequestParam Integer quantity) {
       return ResponseEntity.ok(cartService.addToCart(cartKey, productVariantId, quantity));
    }

    @PutMapping("/cart-item/{cartItemId}")
    public ResponseEntity<?> updateCart(@PathVariable Long cartItemId,@RequestParam Integer quantity) {
        cartService.updateQuantity(cartItemId, quantity);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cart-item")
    public ResponseEntity<?> deleteCart(@RequestParam Long cartItemId) {
        cartService.removeItem(cartItemId);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("{cartId}")
    public ResponseEntity<?> deleteCartItem(@PathVariable Long cartId) {
        cartService.clearCart(cartId);
        return ResponseEntity.noContent().build();
    }
}
