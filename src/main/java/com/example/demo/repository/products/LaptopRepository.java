package com.example.demo.repository.products;

import com.example.demo.dto.response.LaptopResponse;
import com.example.demo.models.products.Laptop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LaptopRepository extends JpaRepository<Laptop, Long> {
    @Query("""
         SELECT new com.example.demo.dto.response.LaptopResponse(l.cpu, l.ram, l.storage, l.gpu, l.resolution,
                l.screenSize, l.dimension, c.name, c.hexCode, pv.image, p.brand, p.name, p.description, pv.sku, pv.price
        )
        FROM Product p
        JOIN Laptop l ON p.id = l.id
        LEFT JOIN ProductVariant pv ON pv.product = p
        LEFT JOIN pv.color c
        WHERE l.id = :id
    """)

    List<LaptopResponse> findLaptopWithVariants(@Param("id") Long id);
    @Query("""
       SELECT new com.example.demo.dto.response.LaptopResponse(l.cpu, l.ram, l.storage, l.gpu, l.resolution,
                l.screenSize, l.dimension, c.name, c.hexCode, pv.image, p.brand, p.name, p.description, pv.sku, pv.price
        )
        FROM Product p
        JOIN Laptop l ON p.id = l.id
        LEFT JOIN ProductVariant pv ON pv.product = p
        LEFT JOIN pv.color c
       """)
    List<LaptopResponse> getAllLaptop();

    @Query("""
         SELECT new com.example.demo.dto.response.LaptopResponse(l.cpu, l.ram, l.storage, l.gpu, l.resolution,
                l.screenSize, l.dimension, c.name, c.hexCode, pv.image, p.brand, p.name, p.description, pv.sku, pv.price
        )
        FROM Product p
        JOIN Laptop l ON p.id = l.id
        LEFT JOIN ProductVariant pv ON pv.product = p
        LEFT JOIN pv.color c
        WHERE pv.sku = :sku
    """)
    Optional<LaptopResponse> findBySku(@Param("sku") String sku);

}