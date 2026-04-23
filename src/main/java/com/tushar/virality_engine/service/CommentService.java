package com.tushar.virality_engine.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.tushar.virality_engine.entity.AuthorType;
import com.tushar.virality_engine.entity.Comment;
import com.tushar.virality_engine.repository.BotRepository;
import com.tushar.virality_engine.repository.CommentRepository;
import com.tushar.virality_engine.repository.PostRepository;
import com.tushar.virality_engine.repository.UserRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final BotRepository botRepository;
    private final ViralityService viralityService;
    private final NotificationService notificationService;

    public Comment addComment(@NonNull Long postId, @NonNull Comment comment) {

        // Step 1: Check post exists
        if (!postRepository.existsById(postId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Post not found!");
        }

        // Step 2: Check depthLevel <= 20
        if (comment.getDepthLevel() != null && comment.getDepthLevel() > 20) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Comment depth level cannot exceed 20!");
        }

        // Step 3: Set postId on comment
        comment.setPostId(postId);

        // Step 4: Validate authorId
        if (comment.getAuthorId() == null || comment.getAuthorType() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "authorId and authorType are required!");
        }

        if (comment.getAuthorType() == AuthorType.USER) {
            if (!userRepository.existsById(comment.getAuthorId())) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found!");
            }
        } else if (comment.getAuthorType() == AuthorType.BOT) {
            if (!botRepository.existsById(comment.getAuthorId())) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Bot not found!");
            }
        }

        // Virality Logic
        if (comment.getAuthorType() == AuthorType.BOT) {
            // Check horizontal cap
            if (!viralityService.checkAndIncrementBotCount(postId)) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Bot reply limit reached for this post");
            }

            // Check cooldown (pass botId and post authorId)
            var post = postRepository.findById(postId).get();
            if (!viralityService.checkCooldown(comment.getAuthorId(), post.getAuthorId())) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Bot cooldown active");
            }

            // +1 virality for bot reply
            viralityService.addViralityScore(postId, 1L);
        } else {
            // +50 virality for human comment
            viralityService.addViralityScore(postId, 50L);
        }
        Comment savedComment = commentRepository.save(comment);

        // Send notification to post author if comment is from a bot
        if (comment.getAuthorType() == AuthorType.BOT) {
            var post = postRepository.findById(postId).get();
            var bot = botRepository.findById(comment.getAuthorId()).get();
            notificationService.handleBotNotification(
                    post.getAuthorId(),
                    bot.getName());
        }

        return savedComment;
    }
}