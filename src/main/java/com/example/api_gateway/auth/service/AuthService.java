package com.example.api_gateway.auth.service;

import com.example.api_gateway.auth.dto.LoginRequest;
import com.example.api_gateway.auth.dto.RegisterRequest;
import com.example.api_gateway.auth.model.User;
import com.example.api_gateway.auth.repository.UserRepository;
import com.example.api_gateway.auth.security.jwt.JwtTokenProvider;
import com.example.api_gateway.exceptions.InvalidPasswordOrUsernameException;
import com.example.api_gateway.exceptions.UserAlreadyExistException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(PasswordEncoder passwordEncoder, UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    //Register the user
    public void registerUser(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent() || userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistException("This username/email has already registered, Login");
        }
        String encoded = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(encoded);
        userRepository.save(user);
    }

    //login the user
    public Map<String, String> loginUser(LoginRequest request) {
        User user = authenticate(request);
        //now we create a access/refersh jwt token and return to the user
        String accesstoken = jwtTokenProvider.generateAccessToken(user);
        String refreshtoken = jwtTokenProvider.generateRefreshToken(user);

        return Map.of("accessToken", accesstoken,
                "refreshToken", refreshtoken);
    }

    public User authenticate(LoginRequest request) {
        User decoder = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new UsernameNotFoundException("User doesn't exist"));

        boolean isValid = passwordEncoder.matches(request.getPassword(), decoder.getPassword());
        if (!isValid) throw new InvalidPasswordOrUsernameException("Invalid Credentials");

        return decoder;
    }

    public boolean validateRefreshToken(String refreshToken) {
        return jwtTokenProvider.validateToken(refreshToken);
    }

    public void revokeTokensForUser() {
    }
}
