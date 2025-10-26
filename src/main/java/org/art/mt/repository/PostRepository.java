package org.art.mt.repository;

import java.util.List;
import org.art.mt.entity.Post;
import org.art.mt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthorOrderByCreatedAtDesc(User author);
    List<Post> findByAuthorInOrderByCreatedAtDesc(List<User> authors);
}
