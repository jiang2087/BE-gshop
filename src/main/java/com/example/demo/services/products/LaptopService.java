package com.example.demo.services.products;

import com.example.demo.dto.request.LaptopRequest;
import com.example.demo.dto.response.LaptopResponse;
import com.example.demo.models.Color;
import com.example.demo.models.Product;
import com.example.demo.models.products.Laptop;
import com.example.demo.models.products.ProductVariant;
import com.example.demo.repository.ColorRepository;
import com.example.demo.repository.products.LaptopRepository;
import com.example.demo.repository.products.ProductVariantRepository;
import com.example.demo.utils.SkuGenerator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LaptopService {

    private final LaptopRepository laptopRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ColorRepository colorRepository;
    private final SkuGenerator skuGenerator;

    public List<?> getAllLaptop() {
        return laptopRepository.getAllLaptop();
    }

    public List<LaptopResponse> getLaptopVariants(Long id) {
        return laptopRepository.findLaptopWithVariants(id);
    }

    @Transactional
    public Laptop updateLaptop(String sku, LaptopRequest laptopRequest) {

        var variant = productVariantRepository.findBySku(sku)
                .orElseThrow(() -> new EntityNotFoundException("sku not found" + sku));
        Long laptopId = variant.getProduct().getId();
        var laptop = laptopRepository.findById(laptopId)
                .orElseThrow(() -> new EntityNotFoundException("laptop not found" + laptopId));

        // Update Product fields
        laptop.setBrand(laptopRequest.brand());
        laptop.setName(laptopRequest.name());
        laptop.setDescription(laptopRequest.description());

        // Update Laptop fields
        laptop.setCpu(laptopRequest.cpu());
        laptop.setRam(laptopRequest.ram());
        laptop.setStorage(laptopRequest.storage());
        laptop.setGpu(laptopRequest.gpu());
        laptop.setResolution(laptopRequest.resolution());
        laptop.setScreenSize(laptopRequest.screenSize());
        laptop.setDimension(laptopRequest.dimension());

        // Update Variant fields
        variant.setPrice(laptopRequest.price());
        variant.setImage(laptopRequest.image());
        // Update color
        if (laptopRequest.colorName() != null || laptopRequest.hexCode() != null) {
            Color color = variant.getColor();
            color.setName(laptopRequest.colorName());
            color.setHexCode(laptopRequest.hexCode());
        }
        // dirty checking tự update khi commit
        return laptop;
    }

    @Transactional
    public ProductVariant createLaptop(LaptopRequest laptopRequest) {
        Laptop laptop = getLaptop(laptopRequest);

        // check color
        var color = colorRepository
                .findByHexCode(laptopRequest.hexCode())
                .orElseGet(() -> {
                    Color c = new Color();
                    c.setName(laptopRequest.colorName());
                    c.setHexCode(laptopRequest.hexCode());
                    return colorRepository.save(c);
                });

        laptopRepository.save(laptop);
        String sku = skuGenerator.generate(laptop.getName(), color.getName());
        ProductVariant productVariant = new ProductVariant();
        productVariant.setColor(color);
        productVariant.setPrice(laptopRequest.price());
        productVariant.setProduct(laptop);
        productVariant.setImage(laptopRequest.image());
        productVariant.setSku(sku);
        productVariant.setStockQuantity(0);

        return productVariantRepository.save(productVariant);
    }

    @Transactional
    public void deleteLaptop(String sku) {
        ProductVariant variant = productVariantRepository
                .findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Not found product by sku"));

        // 1. Remove
        Product product = variant.getProduct();
        if (product != null) {
            product.getProductVariants().remove(variant);
        }

        // 3. Xóa variant
        productVariantRepository.delete(variant);
    }

    private static @NonNull Laptop getLaptop(LaptopRequest laptopRequest) {
        Laptop laptop = new Laptop();
        laptop.setName(laptopRequest.name());
        laptop.setDescription(laptopRequest.description());
        laptop.setCpu(laptopRequest.cpu());
        laptop.setRam(laptopRequest.ram());
        laptop.setStorage(laptopRequest.storage());
        laptop.setGpu(laptopRequest.gpu());
        laptop.setResolution(laptopRequest.resolution());
        laptop.setScreenSize(laptopRequest.screenSize());
        laptop.setDimension(laptopRequest.dimension());
        laptop.setBrand(laptopRequest.brand());
        return laptop;
    }

}
