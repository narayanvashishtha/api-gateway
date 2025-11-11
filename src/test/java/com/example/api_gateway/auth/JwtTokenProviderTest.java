package com.example.api_gateway.auth;

import com.example.api_gateway.auth.model.User;
import com.example.api_gateway.auth.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private User user;
    String secretKey = "my-trading-key";
    long accessTokenExpirationTime = 60_000;

    @BeforeEach
    void setup() {
        user = new User();
        user.setUsername("testUser");
        user.setEmail("testUser@gmail.com");
        user.setRole(User.Role.USER);

        String secretKey = "my-trading-key";
        long accessTokenExpirationTime = 60_000;
        long refreshTokenExpirationTime = 120_000;

        jwtTokenProvider = new JwtTokenProvider(secretKey, accessTokenExpirationTime, refreshTokenExpirationTime);
    }

    @Test
    void testGenerateAccessToken_ValidUser_ShouldReturnValidToken() {
        String token = jwtTokenProvider.generateAccessToken(user);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("testUser", jwtTokenProvider.extractUsername(token));
    }

    @Test
    void testValidateToken_ValidToken_ShouldReturnTrue() {
        String token = jwtTokenProvider.generateAccessToken(user);

        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void testValidateToken_ExpiredToken_ShouldReturnFalse() throws InterruptedException {
        String token = jwtTokenProvider.generateAccessToken(user);
        Thread.sleep(60);
        assertFalse(jwtTokenProvider.validateToken(token));
    }

    @Test
    void testExtractRole_ValidToken_ShouldReturnRole() {
        String token = jwtTokenProvider.generateAccessToken(user);
        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey.getBytes()).build().parseClaimsJws(token).getBody();
        String role = claims.get("role", String.class);
        String username = claims.get("username", String.class);
        String email = claims.get("email", String.class);

        assertEquals("USER", role);
        assertEquals("testUser", username);
        assertEquals("testUser@gmail.com", email);
    }
    @Test
    void testExtractUsername_ValidToken_ShouldReturnUsername() {
        String token = jwtTokenProvider.generateAccessToken(user);
        String username = jwtTokenProvider.extractUsername(token);
        assertEquals("testUser", username);
    }
}
