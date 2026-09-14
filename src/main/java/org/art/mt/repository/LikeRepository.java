package org.art.mt.repository;

import java.util.List;

import org.art.mt.entity.Like;
import org.art.mt.entity.Post;
import org.art.mt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByPostAndUser(Post post, User user);
    long countByPost(Post post);
    void deleteByPostAndUser(Post post, User user);

    @Query("select l.post.id from Like l where l.user = :user and l.post.id in :postIds")
    List<Long> findLikedPostIds(@Param("user") User user, @Param("postIds") List<Long> postIds);
}
