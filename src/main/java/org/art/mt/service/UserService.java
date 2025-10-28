package org.art.mt.service;

import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.art.mt.repository.UserRepository;
import org.art.mt.entity.User;
import org.art.mt.exception.UserRegistrationException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Transactional
    public boolean registerUser(String username, String email, String password) {
        try {
            logger.info("Registering user: {}", username);
            if (userRepository.existsByUsername(username)) {
                logger.error("Username already exists: {}", username);
                throw new IllegalArgumentException("Username already exists");
            }
            if (email != null && userRepository.existsByEmail(email)) {
                logger.error("Email already exists: {}", email);
                throw new IllegalArgumentException("Email already exists");
            }
            User user = new User(username, email, passwordEncoder.encode(password));
            userRepository.save(user);
            return true;
        } catch (DataAccessException e) {
            throw new UserRegistrationException("Username already exists");
        }
    }


    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean updateUser(User user) {
        if (userRepository.existsById(user.getId())) {
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
