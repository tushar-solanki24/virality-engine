package com.tushar.virality_engine.repository;

import com.tushar.virality_engine.entity.Post;
import com.tushar.virality_engine.entity.AuthorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // Find all posts by a specific author
    List<Post> findByAuthorId(Long authorId);
    
    // Find all posts by author type (USER or BOT)
    List<Post> findByAuthorType(AuthorType authorType);
    
    // Find posts by specific author and author type
    List<Post> findByAuthorIdAndAuthorType(Long authorId, AuthorType authorType);
}