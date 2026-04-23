package com.tushar.virality_engine.repository;

import com.tushar.virality_engine.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // find all comments by postId
    List<Comment> findByPostId(Long postId);
    
    // count comments by postId
    long countByPostId(Long postId);
}