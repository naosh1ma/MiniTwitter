package org.art.mt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.art.mt.service.UserService;
import org.art.mt.entity.User;
import java.util.Optional;

import org.art.mt.dto.ApiResponse;
import org.art.mt.dto.LoginResponseDTO;
import org.art.mt.dto.UserDTO;
import org.art.mt.entity.AuthRequest;
import org.art.mt.service.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@Valid @RequestBody AuthRequest authRequest) {
    try {
        Optional<User> user = userService.getUserByUsername(authRequest.getUsername());
        if (user.isPresent()) {
            if (passwordEncoder.matches(authRequest.getPassword(), user.get().getPassword())) {
                String token = jwtService.generateToken(user.get().getUsername());
                UserDTO userDTO = convertToDTO(user.get());
                LoginResponseDTO response = new LoginResponseDTO(token, userDTO);
                return ResponseEntity.ok(ApiResponse.ok(response, "Login successful"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid password"));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid username or password"));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Login failed"));
    }
}

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            String username = jwtService.extractUsername(token);
            if (jwtService.isTokenValid(token, username)) {
                return ResponseEntity.ok(ApiResponse.ok("Token is valid for user: " + username, "Token is valid"));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid token"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid token"));
        }
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getCreatedAt());
    }

}
