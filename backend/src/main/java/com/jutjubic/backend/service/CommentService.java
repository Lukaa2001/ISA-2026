package com.jutjubic.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jutjubic.backend.entity.Comment;
import com.jutjubic.backend.entity.User;
import com.jutjubic.backend.entity.Video;
import com.jutjubic.backend.exception.ApiException;
import com.jutjubic.backend.repository.CommentRepository;
import com.jutjubic.backend.repository.UserRepository;
import com.jutjubic.backend.repository.VideoRepository;
import com.jutjubic.backend.util.ResponseMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {
  private final CommentRepository commentRepository;
  private final VideoRepository videoRepository;
  private final UserRepository userRepository;
  private final CacheService cacheService;

  public CommentService(
      CommentRepository commentRepository,
      VideoRepository videoRepository,
      UserRepository userRepository,
      CacheService cacheService
  ) {
    this.commentRepository = commentRepository;
    this.videoRepository = videoRepository;
    this.userRepository = userRepository;
    this.cacheService = cacheService;
  }

  @Transactional
  public Map<String, Object> create(long videoId, long userId, String text) {
    Video video = videoRepository.findById(videoId)
        .orElseThrow(() -> new ApiException(404, "Video not found"));
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ApiException(404, "User not found"));

    Comment comment = new Comment();
    comment.setText(text);
    comment.setUser(user);
    comment.setVideo(video);

    Comment saved = commentRepository.save(comment);

    cacheService.delPattern("comments:" + videoId + ":*");

    return ResponseMapper.mapComment(saved);
  }

  @Transactional(readOnly = true)
  public Map<String, Object> findByVideoId(long videoId, int page, int limit) {
    String cacheKey = "comments:%d:page:%d:limit:%d".formatted(videoId, page, limit);
    Map<String, Object> cached = cacheService.get(cacheKey, new TypeReference<>() {});
    if (cached != null) {
      return cached;
    }

    PageRequest request = PageRequest.of(page - 1, limit);
    Page<Comment> commentsPage = commentRepository.findByVideoIdOrderByCreatedAtDesc(videoId, request);

    Map<String, Object> pagination = new LinkedHashMap<>();
    pagination.put("page", page);
    pagination.put("limit", limit);
    pagination.put("total", commentsPage.getTotalElements());
    pagination.put("totalPages", commentsPage.getTotalPages());

    Map<String, Object> result = Map.of(
        "comments", commentsPage.getContent().stream().map(ResponseMapper::mapComment).toList(),
        "pagination", pagination
    );

    cacheService.set(cacheKey, result, 5 * 60);
    return result;
  }
}
