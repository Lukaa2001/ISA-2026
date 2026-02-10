package com.jutjubic.backend.service;

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
public class UserService {
  private final UserRepository userRepository;
  private final VideoRepository videoRepository;
  private final CommentRepository commentRepository;

  public UserService(UserRepository userRepository, VideoRepository videoRepository, CommentRepository commentRepository) {
    this.userRepository = userRepository;
    this.videoRepository = videoRepository;
    this.commentRepository = commentRepository;
  }

  @Transactional(readOnly = true)
  public Map<String, Object> getProfile(long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ApiException(404, "User not found"));

    long videosCount = videoRepository.countByUserId(userId);
    long commentsCount = commentRepository.countByUserId(userId);

    return ResponseMapper.mapPublicUser(user, videosCount, commentsCount);
  }

  @Transactional(readOnly = true)
  public Map<String, Object> getUserVideos(long userId, int page, int limit) {
    PageRequest request = PageRequest.of(page - 1, limit);
    Page<Video> videosPage = videoRepository.findByUserIdOrderByCreatedAtDesc(userId, request);

    Map<String, Object> pagination = new LinkedHashMap<>();
    pagination.put("page", page);
    pagination.put("limit", limit);
    pagination.put("total", videosPage.getTotalElements());
    pagination.put("totalPages", videosPage.getTotalPages());

    return Map.of(
        "videos", videosPage.getContent().stream().map(ResponseMapper::mapVideoForList).toList(),
        "pagination", pagination
    );
  }
}
