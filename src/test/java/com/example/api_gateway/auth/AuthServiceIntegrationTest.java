package com.example.api_gateway.auth;

import com.example.api_gateway.auth.dto.LoginRequest;
import com.example.api_gateway.auth.dto.RegisterRequest;
import com.example.api_gateway.auth.model.User;
import com.example.api_gateway.auth.repository.UserRepository;
import com.example.api_gateway.auth.service.AuthService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
public class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testRegisterUser_ShouldSaveToDatabase() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setEmail("john@gmail.com");
        request.setPassword("pass123");

        authService.registerUser(request);

        User user = userRepository.findByEmail("john@gmail.com").orElse(null);
        assertNotNull(user);
        assertEquals("john", user.getUsername());
    }

    @Test
    void testLoginUser_ShouldReturnValidJwt() {
        // Register first
        RegisterRequest request = new RegisterRequest();
        request.setUsername("mike");
        request.setEmail("mike@gmail.com");
        request.setPassword("secret");
        authService.registerUser(request);

        // Login
        LoginRequest login = new LoginRequest();
        login.setUsername("mike");
        login.setPassword("secret");

        Map<String, String> response = authService.loginUser(login);
        assertNotNull(response.get("accessToken"));
        assertTrue(response.get("accessToken").startsWith("ey"));
    }

}
