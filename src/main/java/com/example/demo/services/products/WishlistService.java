package com.example.demo.services.products;

import com.example.demo.dto.response.WishlistResponse;
import com.example.demo.models.products.WishlistItem;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.products.ProductRepository;
import com.example.demo.repository.products.ProductVariantRepository;
import com.example.demo.repository.products.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    public List<WishlistResponse> getWishlist(Long userId) {
        return  wishlistRepository.findByUserId(userId);
    }

    @Transactional
    public String addWishlistItem(Long userId, Long productId) {

        boolean exists = wishlistRepository.existsByUserIdAndProductVariantId(userId, productId);
        if (exists) {
            throw new RuntimeException("Product already in wishlist");
        }

        WishlistItem item = new WishlistItem();
        item.setUser(userRepository.getReferenceById(userId));
        item.setProductVariant(productVariantRepository.getReferenceById(productId));

        wishlistRepository.save(item);
        return "Wishlist added";
    }

    @Transactional
    public void deleteWishlistItem(Long userId, Long productId) {
        wishlistRepository.deleteByUserIdAndProductVariantId(userId, productId);
    }
}
