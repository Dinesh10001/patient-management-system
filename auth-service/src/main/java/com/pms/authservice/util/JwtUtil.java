package com.pms.authservice.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final Key secretKey;

//    public JwtUtil(@Value("${jwt.secret}") String secret) {
////        byte[] keyBytes = Base64.getDecoder()
////                .decode(secret.getBytes(StandardCharsets.UTF_8));
//        byte[] keyBytes = Base64.getDecoder().decode(secret);
//        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
//    }
public JwtUtil(@Value("${jwt.secret}") String secret) {
    System.out.println("Base64 secret = " + secret);
    byte[] keyBytes = Base64.getDecoder().decode(secret);
    System.out.println("Decoded bytes = " + keyBytes.length);
    this.secretKey = Keys.hmacShaKeyFor(keyBytes);
}


    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 *10)) // 10 hours
                .signWith(secretKey)
                .compact();
    }

    public void validateToken(String token) {
        try {
            Jwts.parser().verifyWith((SecretKey) secretKey)
                    .build()
                    .parseSignedClaims(token);
        } catch (SignatureException e) {
            throw new JwtException("Invalid JWT signature");
        } catch (JwtException e) {
            throw new JwtException("Invalid JWT");
        }
    }

}