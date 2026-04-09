package com.example.demo.repository.products;

import com.example.demo.models.products.Watches;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WatchRepository extends JpaRepository<Watches, Long> {
}
