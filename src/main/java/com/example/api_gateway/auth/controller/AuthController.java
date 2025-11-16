package com.example.api_gateway.auth.controller;

import com.example.api_gateway.auth.dto.LoginRequest;
import com.example.api_gateway.auth.dto.RegisterRequest;
import com.example.api_gateway.auth.model.User;
import com.example.api_gateway.auth.repository.UserRepository;
import com.example.api_gateway.auth.security.jwt.JwtTokenProvider;
import com.example.api_gateway.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@RestController
public class AuthController {


    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthService authService, UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        authService.registerUser(request);
        return ResponseEntity.ok("You are registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        Map<String, String> tokens = authService.loginUser(request);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokens.get("refreshToken")).httpOnly(true)
                .secure(true).path("/").maxAge(Duration.ofDays(1))
                .sameSite("Strict").build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "accessToken", tokens.get("accessToken")
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response){
        try {
            String refreshToken = jwtTokenProvider.extractTokenFromHeader(request);
            if (authService.validateRefreshToken(refreshToken)) {
                String username = jwtTokenProvider.extractUsername(refreshToken);
                User userDetails = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Login again"));
                String token = jwtTokenProvider.generateAccessToken(userDetails);

                return ResponseEntity.ok(Map.of("accessToken", token));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (RuntimeException e) {
            throw new RuntimeException("Trying logging in again");
        }
    }
}
