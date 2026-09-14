package org.art.mt.repository;

import java.util.List;

import org.art.mt.entity.Comment;
import org.art.mt.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostOrderByCreatedAtAsc(Post post);
    long countByPost(Post post);
}
