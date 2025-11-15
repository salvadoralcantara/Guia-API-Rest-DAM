package edu.ues.dam.demo_api_rest.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.stream.Collectors;

import edu.ues.dam.demo_api_rest.dtos.TokenDTO;

@Slf4j
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms:1800000}")
    private long jwtExpirationMs;

    private SecretKey signingKey;

    @PostConstruct
    private void init() {
        signingKey = createSigningKey(jwtSecret);
        log.info("JwtUtil: signing key inicializada (algoritmo HS256)");
    }

    private SecretKey createSigningKey(String secret) {
        if (secret == null) {
            throw new IllegalArgumentException("JWT secret no puede ser null");
        }

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        try {
            if (secret.matches("^[A-Za-z0-9+/=\\r\\n]+$")) {
                try {
                    byte[] decoded = Decoders.BASE64.decode(secret);
                    if (decoded.length >= 32) {
                        return Keys.hmacShaKeyFor(decoded);
                    } else {
                        keyBytes = decoded;
                    }
                } catch (Exception ex) {
                    // no era base64 válido -> continuar
                }
            }
        } catch (Exception ignored) { }

        if (keyBytes.length < 32) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                keyBytes = md.digest(keyBytes);
                log.warn("JwtUtil: secreto corto; se usó SHA-256 del secreto para crear una clave de 256 bits.");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (WeakKeyException wke) {
            log.warn("JwtUtil: llave aun débil, generando key segura nueva (no determinista).");
            return Keys.secretKeyFor(SignatureAlgorithm.HS256);
        }
    }

    public TokenDTO generateToken(String username, UserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        String roles = userDetails.getAuthorities().stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));

        String jwt = Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        TokenDTO dto = new TokenDTO();
        dto.setToken(jwt); // solo seteamos el token para evitar método inexistente en TokenDTO
        return dto;
    }

    public boolean validatedTokenPermission(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JwtUtil: token inválido -> {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
