package com.example.demo.controllers;

import com.example.demo.Enums.UserRole;
import com.example.demo.Enums.UserStatus;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.request.UserChangeRequest;
import com.example.demo.dto.response.JwtResponse;
import com.example.demo.dto.response.MessageResponse;
import com.example.demo.models.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.services.OrderService;
import com.example.demo.services.auth.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrderService orderService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/total")
    public ResponseEntity<?> getTotalUser(){
        return ResponseEntity.ok(userDetailsService.countTotalUser());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        Page<UserDTO> users = userRepository.findNonAdminUsers(UserRole.ROLE_ADMIN, keyword, status, pageable)
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getImageUrl(),
                        user.getStatus() != null ? user.getStatus().name() : null
                ));
        return ResponseEntity.ok(users);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/top-purchasers")
    public ResponseEntity<?> getTopPurchasers(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(orderService.getTopPurchasers(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserChangeRequest userChangeRequest){
        User user = userRepository.findById(id)
                .orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: User not found!"));
        }

        if (userChangeRequest.username() != null && !userChangeRequest.username().isBlank()) {
            user.setUsername(userChangeRequest.username());
        }

        String email = userChangeRequest.email();
        if (email != null && !email.isBlank()) {
            user.setEmail(email);
        }

        String imageUrl = userChangeRequest.imageUrl();
        if (imageUrl != null) {
            user.setImageUrl(imageUrl);
        }
        String currentPass = userChangeRequest.currentPassword();
        String newPass = userChangeRequest.newPassword();
        if ((currentPass != null && !currentPass.isBlank()) || (newPass != null && !newPass.isBlank())) {
            if (currentPass == null || currentPass.isBlank() || newPass == null || newPass.isBlank()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Current password and new password are required!"));
            }
            if (!passwordEncoder.matches(currentPass, user.getPassword())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Current password is incorrect!"));
            }
            user.setPassword(passwordEncoder.encode(newPass));
        }

        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(new JwtResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getImageUrl(),
                null
        ));
    }
}
