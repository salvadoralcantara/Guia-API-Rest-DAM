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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    // Blacklist en memoria: token - expirationMillis
    private final ConcurrentHashMap<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

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
        dto.setToken(jwt); // solo seteamos el token
        return dto;
    }

    // Invalida (blacklist) un token hasta su fecha de expiración
    // Si no puede parsear la fecha, lo agrega temporalmente (5 min)

    public void invalidateToken(String token) {
        if (token == null || token.isBlank()) return;
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Date exp = claims.getExpiration();
            long expMillis = (exp != null) ? exp.getTime() : (System.currentTimeMillis() + 5 * 60 * 1000L);
            blacklistedTokens.put(token, expMillis);
            log.info("JwtUtil: token invalidado y agregado al blacklist, expira en: {}", new Date(expMillis));
        } catch (JwtException | IllegalArgumentException ex) {
            long tmpExpire = System.currentTimeMillis() + 5 * 60 * 1000L; // 5 minutos temporal
            blacklistedTokens.put(token, tmpExpire);
            log.warn("JwtUtil: token no parseable al invalidar; agregado temporalmente al blacklist.");
        }
    }

    //Comprueba si token está blacklisteado; si su expiración pasó, lo limpia

    public boolean isBlacklisted(String token) {
        if (token == null) return false;
        Long expMillis = blacklistedTokens.get(token);
        if (expMillis == null) return false;
        if (expMillis < System.currentTimeMillis()) {
            blacklistedTokens.remove(token);
            return false;
        }
        return true;
    }

    /**
     * Valida firma y que no esté en blacklist.
     */
    public boolean validatedTokenPermission(String token) {
        try {
            if (isBlacklisted(token)) {
                log.warn("JwtUtil: token rechazado (blacklist).");
                return false;
            }

            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);

            Date exp = claims.getBody().getExpiration();
            if (exp != null && exp.before(new Date())) {
                log.warn("JwtUtil: token expirado (fecha de expiración encontrada).");
                return false;
            }
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

    public Map<String, Long> getBlacklistedTokensSnapshot() {
        return Map.copyOf(blacklistedTokens);
    }
}