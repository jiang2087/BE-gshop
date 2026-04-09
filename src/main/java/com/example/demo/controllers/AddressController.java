package com.example.demo.controllers;

import com.example.demo.dto.request.AddressRequest;

import com.example.demo.dto.response.AddressResponse;
import com.example.demo.services.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AddressResponse>> getAddressByUserId(
            @PathVariable Long userId) {
        List<AddressResponse> response = addressService.getAddressByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAddressById(
            @PathVariable Long id) {
        AddressResponse response = addressService.getAddressById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<AddressResponse> updateAddress(
            @RequestParam Long userId,
            @RequestParam Long addressId,
            @RequestBody AddressRequest request) {

        AddressResponse response = addressService.editAddress(userId, addressId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<AddressResponse> createAddress(
            @PathVariable Long userId,
            @RequestBody AddressRequest request) {
        AddressResponse response = addressService.addAddress(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAddress(
            @RequestParam Long userId,
            @RequestParam Long addressId
    ) {
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.ok().build();
    }
}
