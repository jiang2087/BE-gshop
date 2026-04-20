package com.example.demo.controllers;

import com.example.demo.Enums.UserRole;
import com.example.demo.config.UserDetailsImpl;
import com.example.demo.models.Role;
import com.example.demo.models.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.SignupRequest;
import com.example.demo.dto.response.JwtResponse;
import com.example.demo.dto.response.MessageResponse;
import com.example.demo.services.auth.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
                                              HttpServletResponse responseHttp,
                                              @CookieValue(value = "cartKey", required = false) String cartKey){
        JwtResponse response = authService.authenticateUser(loginRequest, responseHttp, cartKey);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        authService.refreshToken(refreshToken, response);

        return ResponseEntity.ok().build();
    }


    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest){
        if(userRepository.existsByEmail(signupRequest.email()))
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        if(userRepository.existsByUsername(signupRequest.username()))
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Username is already in use!"));

        User user = User.builder()
                .username(signupRequest.username())
                .email(signupRequest.email())
                .password(passwordEncoder.encode(signupRequest.password()))
                .build();
        Role userRole = roleRepository.findByName(UserRole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        if(Objects.isNull(userRole)){
           userRole = new Role(UserRole.ROLE_USER);
        }
        user.setRole(Set.of(userRole));
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User register successfully!"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null){
            String username = authentication.getName();
            authService.revokeAllForUser(username);
        }
        deleteCookie(response, "accessToken");
        deleteCookie(response, "refreshToken");
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new MessageResponse("Logout successfully!"));
    }
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        UserDetailsImpl userContext = (UserDetailsImpl) authentication.getPrincipal();
        assert userContext != null;
        User user = userRepository.findByUsername(userContext.getUsername())
                .orElseThrow(() -> new RuntimeException("no user in context"));
        List<String> roles = userContext.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return ResponseEntity.ok(new JwtResponse(user.getId(), user.getUsername(),
                user.getEmail(), user.getImageUrl(), roles));
    }
    private void deleteCookie(HttpServletResponse response, String name){
        Cookie cookie = new Cookie(name, null);
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
