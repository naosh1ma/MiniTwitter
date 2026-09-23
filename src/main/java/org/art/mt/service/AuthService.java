package org.art.mt.service;

import org.art.mt.dto.LoginResponseDTO;
import org.art.mt.entity.User;
import org.art.mt.exception.InvalidCredentialsException;
import org.art.mt.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * An unknown username and a wrong password deliberately produce the same
     * exception and the same message, so the response can't be used to find out
     * which usernames exist.
     */
    public LoginResponseDTO login(String username, String password) {
        User user = userService.getUserByUsername(username)
                .filter(candidate -> passwordEncoder.matches(password, candidate.getPassword()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));
        return new LoginResponseDTO(jwtService.generateToken(user.getUsername()), UserMapper.toDTO(user));
    }

    /**
     * Returns the username the token belongs to. Throws InvalidTokenException
     * (mapped to 401) if the token is unparseable, wrongly signed or expired.
     */
    public String validate(String authorizationHeader) {
        String token = jwtService.resolveBearerToken(authorizationHeader);
        if (token == null) {
            throw new InvalidCredentialsException("Missing bearer token");
        }
        String username = jwtService.extractUsername(token);
        if (!jwtService.isTokenValid(token, username)) {
            throw new InvalidCredentialsException("Invalid token");
        }
        return username;
    }
}
