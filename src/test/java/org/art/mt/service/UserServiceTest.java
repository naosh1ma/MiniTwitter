package org.art.mt.service;

import java.util.List;
import java.util.Optional;

import org.art.mt.dto.UserDTO;
import org.art.mt.entity.User;
import org.art.mt.event.UserFollowedEvent;
import org.art.mt.exception.UserRegistrationException;
import org.art.mt.repository.FollowRepository;
import org.art.mt.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private FollowRepository followRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ApplicationEventPublisher eventPublisher;

    private UserService userService;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, followRepository, passwordEncoder,
                new SecurityUtil(), mock(FileStorageService.class), eventPublisher);

        alice = new User("alice", "alice@example.com", "hashed-pw");
        alice.setId(1L);
        bob = new User("bob", "bob@example.com", "hashed-pw");
        bob.setId(2L);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registerUser_succeedsWithUniqueUsernameAndEmail() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-pw");

        userService.registerUser("alice", "alice@example.com", "password123");

        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_rejectsDuplicateUsername() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser("alice", "new@example.com", "password123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already exists");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_rejectsDuplicateEmail() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser("newuser", "alice@example.com", "password123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_translatesDbRaceIntoUserRegistrationException() {
        // Two concurrent registrations for the same username: both pass the existsBy
        // check, then the DB's unique constraint rejects the second save.
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-pw");
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> userService.registerUser("alice", "alice2@example.com", "password123"))
                .isInstanceOf(UserRegistrationException.class);
    }

    @Test
    void followUser_rejectsFollowingYourself() {
        assertThatThrownBy(() -> userService.followUser("alice", "alice"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot follow yourself");
        verify(followRepository, never()).save(any());
    }

    @Test
    void followUser_createsFollowWhenNotAlreadyFollowing() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(followRepository.existsByFollowerAndFollowing(bob, alice)).thenReturn(false);

        userService.followUser("bob", "alice");

        verify(followRepository).save(any());
        verify(eventPublisher).publishEvent(new UserFollowedEvent("alice", "bob"));
    }

    @Test
    void followUser_isIdempotentWhenAlreadyFollowing() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(followRepository.existsByFollowerAndFollowing(bob, alice)).thenReturn(true);

        userService.followUser("bob", "alice");

        verify(followRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void unfollowUser_deletesTheFollowRelationship() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));

        userService.unfollowUser("bob", "alice");

        verify(followRepository).deleteByFollowerAndFollowing(bob, alice);
    }

    @Test
    void convertToDTOWithFollowInfo_anonymousViewer_neverMarksFollowedByCurrentUser() {
        // No authentication in the SecurityContext.
        when(followRepository.countByFollowing(alice)).thenReturn(5L);
        when(followRepository.countByFollower(alice)).thenReturn(2L);

        UserDTO dto = userService.convertToDTOWithFollowInfo(alice);

        assertThat(dto.getFollowerCount()).isEqualTo(5L);
        assertThat(dto.getFollowingCount()).isEqualTo(2L);
        assertThat(dto.isFollowedByCurrentUser()).isFalse();
        verify(followRepository, never()).existsByFollowerAndFollowing(any(), any());
    }

    @Test
    void convertToDTOWithFollowInfo_loggedInViewer_reflectsWhetherTheyFollow() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("bob", null, List.of()));
        when(followRepository.countByFollowing(alice)).thenReturn(5L);
        when(followRepository.countByFollower(alice)).thenReturn(2L);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));
        when(followRepository.existsByFollowerAndFollowing(bob, alice)).thenReturn(true);

        UserDTO dto = userService.convertToDTOWithFollowInfo(alice);

        assertThat(dto.isFollowedByCurrentUser()).isTrue();
    }
}
