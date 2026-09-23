package org.art.mt.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.art.mt.dto.UserDTO;
import org.art.mt.entity.Follow;
import org.art.mt.event.UserFollowedEvent;
import org.art.mt.exception.UserRegistrationException;
import org.art.mt.mapper.UserMapper;
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
    private final ApplicationEventPublisher eventPublisher;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, FollowRepository followRepository,
                        PasswordEncoder passwordEncoder, SecurityUtil securityUtil,
                        FileStorageService fileStorageService, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityUtil = securityUtil;
        this.fileStorageService = fileStorageService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void registerUser(String username, String email, String password) {
        logger.info("Registering user: {}", username);
        if (userRepository.existsByUsername(username)) {
            logger.info("Rejected registration, username already exists: {}", username);
            throw new IllegalArgumentException("Username already exists");
        }
        if (email != null && userRepository.existsByEmail(email)) {
            logger.info("Rejected registration, email already exists: {}", email);
            throw new IllegalArgumentException("Email already exists");
        }
        try {
            userRepository.save(new User(username, email, passwordEncoder.encode(password)));
        } catch (DataIntegrityViolationException e) {
            // The existsBy checks above lost a race with a concurrent registration
            // and the unique constraint rejected this insert.
            throw new UserRegistrationException("Username or email already exists", e);
        }
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User updateProfile(String username, String bio, String avatarUrl) {
        User user = requireUser(username);
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
        User user = requireUser(username);
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
        User follower = requireUser(followerUsername);
        User following = requireUser(followingUsername);
        if (!followRepository.existsByFollowerAndFollowing(follower, following)) {
            Follow follow = new Follow();
            follow.setFollower(follower);
            follow.setFollowing(following);
            followRepository.save(follow);
            eventPublisher.publishEvent(new UserFollowedEvent(followingUsername, followerUsername));
        }
    }

    @Transactional
    public void unfollowUser(String followerUsername, String followingUsername) {
        User follower = requireUser(followerUsername);
        User following = requireUser(followingUsername);
        followRepository.deleteByFollowerAndFollowing(follower, following);
    }

    public UserDTO convertToDTOWithFollowInfo(User user) {
        UserDTO dto = UserMapper.toDTO(user);
        dto.setFollowerCount(followRepository.countByFollowing(user));
        dto.setFollowingCount(followRepository.countByFollower(user));
        String currentUsername = securityUtil.getCurrentUsernameOrNull();
        if (currentUsername != null) {
            userRepository.findByUsername(currentUsername).ifPresent(currentUser ->
                    dto.setFollowedByCurrentUser(followRepository.existsByFollowerAndFollowing(currentUser, user)));
        }
        return dto;
    }

    private User requireUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
