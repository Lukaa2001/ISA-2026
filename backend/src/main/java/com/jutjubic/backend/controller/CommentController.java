package com.jutjubic.backend.controller;

import com.jutjubic.backend.dto.CreateCommentRequest;
import com.jutjubic.backend.exception.ApiException;
import com.jutjubic.backend.security.JwtPrincipal;
import com.jutjubic.backend.security.SecurityUtils;
import com.jutjubic.backend.service.CommentService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/videos/{videoId}/comments")
public class CommentController {
  private final CommentService commentService;

  public CommentController(CommentService commentService) {
    this.commentService = commentService;
  }

  @GetMapping
  public Map<String, Object> list(
      @PathVariable long videoId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int limit
  ) {
    validatePagination(page, limit);
    return commentService.findByVideoId(videoId, page, limit);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> create(@PathVariable long videoId, @Valid @RequestBody CreateCommentRequest request) {
    JwtPrincipal principal = SecurityUtils.getCurrentPrincipal();
    return commentService.create(videoId, principal.userId(), request.getText());
  }

  private void validatePagination(int page, int limit) {
    if (page <= 0 || limit <= 0 || limit > 50) {
      throw new ApiException(400, "Invalid pagination parameters");
    }
  }
}
