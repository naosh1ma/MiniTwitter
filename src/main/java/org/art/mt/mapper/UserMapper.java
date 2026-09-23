package org.art.mt.mapper;

import org.art.mt.dto.UserDTO;
import org.art.mt.entity.User;

/**
 * The single place a User entity becomes a UserDTO. Deliberately static: the
 * mapping is pure, so callers don't need it injected, and the counts and
 * per-viewer flags that some callers add on top stay where they belong - in the
 * service that can query for them (see UserService.convertToDTOWithFollowInfo).
 */
public final class UserMapper {

    private UserMapper() {
    }

    public static UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getCreatedAt());
    }
}
