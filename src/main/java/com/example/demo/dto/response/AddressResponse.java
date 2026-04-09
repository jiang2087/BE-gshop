package com.example.demo.dto.response;

public record AddressResponse (
        Long id,
        String phone,
        String recipientName,
        String address,
        boolean isDefault){
}
