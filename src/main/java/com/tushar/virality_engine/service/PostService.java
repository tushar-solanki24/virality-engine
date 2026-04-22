package com.tushar.virality_engine.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.tushar.virality_engine.entity.AuthorType;
import com.tushar.virality_engine.entity.Post;
import com.tushar.virality_engine.repository.BotRepository;
import com.tushar.virality_engine.repository.PostRepository;
import com.tushar.virality_engine.repository.UserRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final BotRepository botRepository;
    private final UserRepository userRepository;

    public Post createPost(@NonNull Post post) {
        // Add this null check first!
        if (post.getAuthorId() == null || post.getAuthorType() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "authorId and authorType are required!");
        }

        if (post.getAuthorType() == AuthorType.USER) {
            if (!userRepository.existsById(post.getAuthorId())) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found!");
            }
        } else if (post.getAuthorType() == AuthorType.BOT) {
            if (!botRepository.existsById(post.getAuthorId())) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Bot not found!");
            }
        }
        return postRepository.save(post);
    }

    public Post getPostById(@NonNull Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Post not found!"));
    }

    public boolean likePost(@NonNull Long postId) {
        // TODO: Phase 2 will add Redis logic for like tracking
        return true;
    }
}
