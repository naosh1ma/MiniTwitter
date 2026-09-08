package org.art.mt.service;

import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.art.mt.dto.UserDTO;
import org.art.mt.entity.Follow;
import org.art.mt.exception.UserRegistrationException;
import org.art.mt.repository.FollowRepository;
import org.art.mt.repository.UserRepository;
import org.art.mt.entity.User;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtil securityUtil;
    private final FileStorageService fileStorageService;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, FollowRepository followRepository,
                        PasswordEncoder passwordEncoder, SecurityUtil securityUtil,
                        FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityUtil = securityUtil;
        this.fileStorageService = fileStorageService;
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

    @Transactional
    public User updateProfile(String username, String bio, String avatarUrl) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setBio(bio);
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return user;
    }

    @Transactional
    public User updateAvatar(String username, MultipartFile file) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String avatarUrl = fileStorageService.storeImage(file, "avatar-" + user.getId(), user.getAvatarUrl());
        user.setAvatarUrl(avatarUrl);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return user;
    }

    @Transactional
    public void followUser(String followerUsername, String followingUsername) {
        if (followerUsername.equals(followingUsername)) {
            throw new IllegalArgumentException("You cannot follow yourself");
        }
        User follower = userRepository.findByUsername(followerUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        User following = userRepository.findByUsername(followingUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!followRepository.existsByFollowerAndFollowing(follower, following)) {
            Follow follow = new Follow();
            follow.setFollower(follower);
            follow.setFollowing(following);
            followRepository.save(follow);
        }
    }

    @Transactional
    public void unfollowUser(String followerUsername, String followingUsername) {
        User follower = userRepository.findByUsername(followerUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        User following = userRepository.findByUsername(followingUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        followRepository.deleteByFollowerAndFollowing(follower, following);
    }

    public UserDTO convertToDTOWithFollowInfo(User user) {
        UserDTO dto = new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getCreatedAt());
        dto.setFollowerCount(followRepository.countByFollowing(user));
        dto.setFollowingCount(followRepository.countByFollower(user));
        String currentUsername = securityUtil.getCurrentUsernameOrNull();
        if (currentUsername != null) {
            userRepository.findByUsername(currentUsername).ifPresent(currentUser ->
                    dto.setFollowedByCurrentUser(followRepository.existsByFollowerAndFollowing(currentUser, user)));
        }
        return dto;
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
