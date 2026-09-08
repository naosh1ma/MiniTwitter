package org.art.mt.repository;

import java.util.List;

import org.art.mt.entity.Follow;
import org.art.mt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerAndFollowing(User follower, User following);
    void deleteByFollowerAndFollowing(User follower, User following);
    long countByFollower(User follower);
    long countByFollowing(User following);

    @Query("select f.following from Follow f where f.follower = :follower")
    List<User> findFollowingUsers(@Param("follower") User follower);
}
