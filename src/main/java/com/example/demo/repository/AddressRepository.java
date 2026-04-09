package com.example.demo.repository;

import com.example.demo.models.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    @Query("SELECT a FROM Address a JOIN FETCH a.user b WHERE a.user.id = :userId")
    List<Address> findByUserId(Long userId);

    Optional<Address> findByUserIdAndIsDefaultTrue(Long id);

    @Query("SELECT a from Address a WHERE a.user.id = :userId AND a.id = :addressId")
    Optional<Address> findByIdAndUserId(@Param("addressId") Long id, @Param("userId") Long userId);
}
