package com.example.demo.repository.products;

import com.example.demo.models.products.HomeAppliance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HomeApplianceRepository extends JpaRepository<HomeAppliance, Long> {
}
