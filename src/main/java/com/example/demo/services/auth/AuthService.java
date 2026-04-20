package com.example.demo.services.auth;

import com.example.demo.config.UserDetailsImpl;
import com.example.demo.config.jwt.JwtUtils;
import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.response.JwtResponse;
import com.example.demo.dto.response.RefreshTokenPair;
import com.example.demo.models.RefreshToken;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.services.CartService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CookieValue;

import java.time.Duration;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtils jwtUtils;
    private final CartService cartService;
    private final UserDetailsServiceImpl userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;
    @Value("${hg.app.jwtExpirationSecond}")
    private Long refreshTokenTtlSecond;


    public JwtResponse authenticateUser(LoginRequest loginRequest,
                                        HttpServletResponse response,
                                        String cartKey){

        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username(), loginRequest.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        var userDetails = (UserDetailsImpl) authentication.getPrincipal();
        if (userDetails == null) {
            throw new RuntimeException("User not found");
        }
        var accessToken = jwtUtils.generateJwtAccessToken((userDetails));
        var refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername(), refreshTokenTtlSecond);
        // save refreshToken and accessToken into Cookie
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofMinutes(15))
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken.plainToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        if (cartKey != null && !cartKey.isBlank()) {
            cartService.mergeCart(cartKey, userDetails.getId());
        }
        return new JwtResponse(
                userDetails.getId(), userDetails.getUsername(),
                userDetails.getEmail(), userDetails.getImageUrl(), roles);
    }

    @Transactional
    public void refreshToken(String receivedRefreshPlain, HttpServletResponse response){
        var used = refreshTokenService.useRefreshToken(receivedRefreshPlain);
        if(used.isEmpty()){
            throw new RuntimeException("Invalid reused refreshToken");
        }
        RefreshToken old = used.get();
        String username = old.getUsername();
        var userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);

        // create a new refresh token and access token
        RefreshTokenPair newRefreshToken = refreshTokenService.createRefreshToken(username, refreshTokenTtlSecond);
        String newAccessToken = jwtUtils.generateJwtAccessToken(userDetails);
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofMinutes(15))
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newRefreshToken.plainToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        old.setReplacedBy(newRefreshToken.jti());
        refreshTokenRepository.save(old);
    }

    @Transactional
    public void revokeAllForUser(String username){
        refreshTokenRepository.findAllByUsername(username)
                .ifPresent(tokens -> {
                    tokens.forEach(token -> token.setRevoked(true));
                    refreshTokenRepository.saveAll(tokens);
                });
    }

}
