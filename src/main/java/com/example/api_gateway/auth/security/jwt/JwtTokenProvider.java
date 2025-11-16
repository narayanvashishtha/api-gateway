package com.example.api_gateway.auth.security.jwt;

import com.example.api_gateway.auth.model.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.ott.InvalidOneTimeTokenException;
import org.springframework.stereotype.Component;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.accessTokenExpirationTime}")
    private long accessTokenExpirationTime;

    @Value("${app.jwt.refreshTokenExpirationTime}")
    private long refreshTokenExpirationTime;


    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());
        claims.put("email", user.getEmail());

        return Jwts.builder().signWith(SignatureAlgorithm.HS256, secretKey)
                .setSubject(user.getUsername()).setClaims(claims)
                .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationTime)).compact();
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());

        return Jwts.builder().signWith(SignatureAlgorithm.HS256, secretKey)
                .setSubject(user.getUsername()).setClaims(claims)
                .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationTime)).compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey.getBytes()).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Invalid Token");
        } catch (MalformedJwtException e) {
            System.out.println("Invalid JWT token");
        } catch (Exception e) {
            System.out.println("Token validation error");
        }
        return false;
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey.getBytes()).build().parseClaimsJws(token).getBody().getSubject();
    }

    public String extractTokenFromHeader(HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            return authHeader.substring(7);
        }
        return null;
    }
}
