package com.sravan.moneymanager.jwtUtilPackage;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


@Service
public class JwtUtil {



    @Value("${app.jwt.key:aajsdhflkjasdhfjkhasdklfjhaskdjdfhkjasdhfjkhasdfhasdkjfashdfaskdfjh}")
    private String secretKey;
    @PostConstruct
    public void init() {
        System.out.println(secretKey);
        System.out.println("JWT Secret Key loaded: " + (secretKey != null && !secretKey.isEmpty()));
    }

    public Key getKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }


    public String generateToken(UserDetails userDetails) {

        Map<String, Object> claims = new HashMap<>();

        if (userDetails != null) {

            claims.put("email", userDetails.getUsername());

            List<String> roles = userDetails.getAuthorities()
                                            .stream()
                                            .map(role -> role.getAuthority())
                                            .toList();
            claims.put("roles", roles);

        }

        return Jwts.builder()
                   .claims(claims)
                   .subject(userDetails.getUsername() != null ? userDetails.getUsername() : "")
                   .issuedAt(new Date(System.currentTimeMillis()))
                   .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 10))
                   .signWith(getKey())
                   .compact();
    }


    public Claims extractAllClaims(String token) {
        Key key = getKey();
        return Jwts.parser()
                   .verifyWith((SecretKey) key)
                   .build()
                   .parseSignedClaims(token)
                   .getPayload();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    public List<String> extractRoles(String token) {

        List<String> roles = extractClaim(token, claims -> claims.get("roles", List.class));
        return roles;
    }
    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration)
                   .before(new Date());
    }
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

}
