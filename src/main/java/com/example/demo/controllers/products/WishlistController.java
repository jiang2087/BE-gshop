package com.example.demo.controllers.products;

import com.example.demo.services.products.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getWishlist(@PathVariable Long userId) {
       return  ResponseEntity.ok(wishlistService.getWishlist(userId));
    }

    @PostMapping
    public ResponseEntity<?> addWishlist(@RequestParam Long userId,
                                         @RequestParam Long productVariantId){
        return ResponseEntity.ok(wishlistService.addWishlistItem(userId,productVariantId));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteWishlist(@RequestParam Long userId, @RequestParam Long productVariantId) {
        wishlistService.deleteWishlistItem(userId,productVariantId);
        return ResponseEntity.ok().build();
    }
}
