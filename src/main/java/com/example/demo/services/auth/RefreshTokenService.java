package com.example.demo.services.auth;

import com.example.demo.dto.response.RefreshTokenPair;
import com.example.demo.models.RefreshToken;
import com.example.demo.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenService {

    private final UserDetailsServiceImpl userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private static final long RETENTION_DAYS = 30;

    public static String sha256(String input){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    private String generateRandomTokenPlain(){
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    @Transactional
    public RefreshTokenPair createRefreshToken(String username, Long ttlMs){
        String plain = generateRandomTokenPlain();
        String hash = sha256(plain);
        String jti = UUID.randomUUID().toString();
        var now = Instant.now();
        var exp = now.plusSeconds(ttlMs);
        RefreshToken token = RefreshToken.builder()
                .revoked(false)
                .jti(jti)
                .username(username)
                .tokenHash(hash)
                .createdAt(now)
                .expiresAt(exp)
                .build();
        refreshTokenRepository.save(token);
        return new RefreshTokenPair(plain, jti);
    }
    @Transactional
    public Optional<RefreshToken> useRefreshToken(String receivedPlain){
        String hash = sha256(receivedPlain);
        Optional<RefreshToken> opt = refreshTokenRepository.findByTokenHash(hash);
        if(opt.isEmpty()){
            return Optional.empty();
        }

        RefreshToken token = opt.get();

        if(token.isRevoked() || token.getExpiresAt().isBefore(Instant.now())){
            return Optional.empty();
        }
        log.info("found token hash {}", token);
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        return Optional.of(token);
    }

}
