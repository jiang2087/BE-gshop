package com.example.demo.models.junction;

import com.example.demo.models.Address;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Embeddable
@Getter
@AllArgsConstructor
public class AddressSnapShot {
    private String recipientName;
    private String phone;
    private String address;

    public AddressSnapShot(Address address) {
    }

    AddressSnapShot() {}
    // Tạo snapshot từ Address entity
    public static AddressSnapShot from(Address address) {
        AddressSnapShot addressSnapShot = new AddressSnapShot();
        addressSnapShot.address = address.getAddress();
        addressSnapShot.recipientName = address.getRecipientName();
        addressSnapShot.phone = address.getPhone();
        return addressSnapShot;
    }
}
