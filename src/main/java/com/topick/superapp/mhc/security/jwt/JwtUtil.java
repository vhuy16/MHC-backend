package com.topick.superapp.mhc.security.jwt;

import com.topick.superapp.mhc.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;


import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
@Component
public class JwtUtil {
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.secret}")
    private String secret;
    public String generateAccessToken(User user){
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        String token = Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
        return  token;
    }
    public String generateRefreshToken(User user){
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        String token = Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
        return  token;
    }
    public Claims parseAccessToken(String token){
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims;

    }
    public boolean validateToken(String token){

        try {
            Claims claims = parseAccessToken(token);
        }
        catch (Exception e){
            return false;
        }
        return true;
    }
    public boolean isTokenExpired(String token){
       Date expiration = (Date) parseAccessToken(token).getExpiration();
       Date now = new Date();
       if(expiration.before(now)){
           return true;
       }
       return false;
    }

    public  UUID extractUserId(HttpServletRequest httpServletRequest){
        var header =  httpServletRequest.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException ("Invalid token");
        }
        var token = header.substring(7);
        var userId = parseAccessToken(token).getSubject();
        return  UUID.fromString(userId);
    }
}
