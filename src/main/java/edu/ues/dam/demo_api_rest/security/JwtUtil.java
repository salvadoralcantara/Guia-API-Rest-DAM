package edu.ues.dam.demo_api_rest.security;

import edu.ues.dam.demo_api_rest.dtos.TokenDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secretKey;

    private final long EXPIRATION = 1800000L; // 30 minutos

    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    public <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extactAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extactAllClaims(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public TokenDTO generateToken(String userName, UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        claims.put("authorities", authorities);
        return createToken(claims, userName);
    }

    private TokenDTO createToken(Map<String, Object> claims, String userName) {
        TokenDTO newToken = new TokenDTO();
        newToken.setExpireIn(String.valueOf(EXPIRATION));

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, secretKey).compact();

        newToken.setToken(token);
        newToken.setMsj("ok");
        return newToken;
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String userName = extractUsername(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Validates token and, if valid, sets a basic Authentication in SecurityContextHolder.
     * This method mirrors the guide's validatedTokenPermission.
     */
    public boolean validatedTokenPermission(String token) {
        try {
            String email = extractUsername(token);
            // Create a basic authentication token with no authorities
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication =
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(email, null, new ArrayList<>());
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
            return true;
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }
}