package com.example.demo.config.jwt;

import com.example.demo.config.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class JwtUtils {
    private static final Logger logger =  LoggerFactory.getLogger(JwtUtils.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Value("${hg.app.jwtSecret}")
    private String jwtSecret;

    @Value("${hg.app.jwtRefreshable-duration}")
    private int jwtExpirationSecond;

    private Key key(){
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateJwtAccessToken( UserDetailsImpl userPrincipal){
        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .claim("type", "access")
                .claim("jti", UUID.randomUUID().toString())
                .claim("roles", userPrincipal.getAuthorities())
                .setExpiration(Date.from(Instant.now().plusSeconds(jwtExpirationSecond)))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }
    public String getUsernameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken){
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        }catch (MalformedJwtException e){
            logger.error("Invalid token: {}", e.getMessage());
        }catch (ExpiredJwtException e){
            logger.error("JWT Token is expired: {}",e.getMessage());
        }catch (UnsupportedJwtException e){
            logger.error("JWT token is unsupported: {}", e.getMessage());
        }catch (IllegalArgumentException e){
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
