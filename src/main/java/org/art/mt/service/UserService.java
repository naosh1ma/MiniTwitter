package org.art.mt.service;

import org.springframework.stereotype.Service;
import org.art.mt.repository.UserRepository;
import org.art.mt.entity.User;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public boolean registerUser(String username, String email, String password) {
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            return false;
        }
        User user = new User(username, email, password);
        userRepository.save(user);
        return true;
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
