package com.example.demo.services;

import com.example.demo.dto.request.AddressRequest;
import com.example.demo.dto.response.AddressResponse;
import com.example.demo.models.Address;
import com.example.demo.models.User;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional
    public AddressResponse addAddress(Long userId, @Valid AddressRequest addressRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("user not found"));


        Address address = Address.builder()
                .address(addressRequest.address())
                .user(user)
                .phone(addressRequest.phone())
                .recipientName(addressRequest.recipientName())
                .build();
        // Nếu chưa có địa chỉ thì sẽ set địa chỉ default
        boolean shouldBeDefault = addressRepository.findByUserId(userId).isEmpty() ;
        if(shouldBeDefault) {
            address.setDefault(true);
        }
        addressRepository.save(address);
        return new AddressResponse(
          address.getId(),
          address.getRecipientName(),
          address.getPhone() ,
          address.getAddress(), address.isDefault()
        );
    }

    public List<AddressResponse> getAddressByUserId(Long userId) {
        List<Address> addresses = addressRepository.findByUserId(userId)
                .stream().toList();
        return addresses.stream()
                .map(address -> new AddressResponse(
                        address.getId(), address.getPhone(),
                        address.getRecipientName(),
                        address.getAddress(),
                        address.isDefault()
                ))
                .toList();
    }

    public  AddressResponse getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Address not found"));
        return new AddressResponse(address.getId(),
                address.getPhone(),
                address.getRecipientName(),
                address.getAddress(),
                address.isDefault());
    }

    @Transactional
    public AddressResponse editAddress(Long userId, Long addressId, @Valid AddressRequest addressRequest) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new EntityNotFoundException("address not found"));
        if(address != null) {
            address.setAddress(addressRequest.address());
            address.setPhone(addressRequest.phone());
            address.setRecipientName(addressRequest.recipientName());
            address.setDefault(address.isDefault());
            addressRepository.save(address);
        }
        return new AddressResponse(address.getId(),
                address.getPhone(),
                address.getRecipientName(),
                address.getAddress(),
                address.isDefault());
    }
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Address not found"));

        // Nếu xóa địa chỉ default thì tự set default cho địa chỉ khác
        if (address.isDefault()) {
            addressRepository.findByUserId(userId).stream()
                    .filter(a -> !a.getId().equals(addressId))
                    .findFirst()
                    .ifPresent(a -> {
                        a.setDefault(true);
                        addressRepository.save(a);
                    });
        }
        addressRepository.delete(address);
    }

}
