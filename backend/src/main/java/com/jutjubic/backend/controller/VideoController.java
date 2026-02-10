package com.jutjubic.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jutjubic.backend.entity.Video;
import com.jutjubic.backend.exception.ApiException;
import com.jutjubic.backend.security.JwtPrincipal;
import com.jutjubic.backend.security.SecurityUtils;
import com.jutjubic.backend.service.VideoService;
import com.jutjubic.backend.service.ViewCounterService;
import com.jutjubic.backend.util.ResponseMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/videos")
public class VideoController {
  private static final Set<String> VIDEO_EXTENSIONS = Set.of(".mp4", ".webm", ".avi", ".mov", ".mkv");
  private static final Set<String> THUMB_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");

  private final VideoService videoService;
  private final ViewCounterService viewCounterService;
  private final ObjectMapper objectMapper;

  public VideoController(VideoService videoService, ViewCounterService viewCounterService, ObjectMapper objectMapper) {
    this.videoService = videoService;
    this.viewCounterService = viewCounterService;
    this.objectMapper = objectMapper;
  }

  @GetMapping
  public Map<String, Object> list(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "12") int limit
  ) {
    validatePagination(page, limit);
    return videoService.findAll(page, limit);
  }

  @GetMapping("/{id}")
  public Map<String, Object> getById(@PathVariable long id) {
    Video video = videoService.findById(id);
    int viewCount = viewCounterService.increment(id);

    Map<String, Object> body = ResponseMapper.mapVideoForDetail(video);
    body.put("viewCount", viewCount);
    return body;
  }

  @GetMapping("/{id}/thumbnail")
  public ResponseEntity<byte[]> getThumbnail(@PathVariable long id) {
    VideoService.ThumbnailResult thumbnail = videoService.getThumbnail(id);
    return ResponseEntity.ok()
        .header(HttpHeaders.CACHE_CONTROL, CacheControl.maxAge(86400, java.util.concurrent.TimeUnit.SECONDS)
            .cachePublic().getHeaderValue())
        .contentType(MediaType.parseMediaType(thumbnail.contentType()))
        .body(thumbnail.buffer());
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> create(
      @RequestParam String title,
      @RequestParam String description,
      @RequestParam(required = false) String tags,
      @RequestParam("video") MultipartFile video,
      @RequestParam("thumbnail") MultipartFile thumbnail,
      @RequestParam(required = false) Double latitude,
      @RequestParam(required = false) Double longitude
  ) {
    if (video == null || video.isEmpty() || thumbnail == null || thumbnail.isEmpty()) {
      throw new ApiException(400, "Video and thumbnail files are required");
    }

    validateFileExtension(video.getOriginalFilename(), VIDEO_EXTENSIONS,
        "Invalid video format. Allowed: mp4, webm, avi, mov, mkv");
    validateFileExtension(thumbnail.getOriginalFilename(), THUMB_EXTENSIONS,
        "Invalid image format. Allowed: jpg, jpeg, png, webp");

    List<String> tagsList = parseTags(tags);
    JwtPrincipal principal = SecurityUtils.getCurrentPrincipal();

    VideoService.CreateVideoInput input = new VideoService.CreateVideoInput(
        title,
        description,
        tagsList,
        latitude,
        longitude,
        principal.userId(),
        video,
        thumbnail
    );

    return videoService.create(input);
  }

  private List<String> parseTags(String rawTags) {
    if (rawTags == null || rawTags.isBlank()) {
      return Collections.emptyList();
    }

    try {
      return objectMapper.readValue(rawTags, new TypeReference<>() {});
    } catch (Exception ex) {
      throw new ApiException(400, "Invalid tags payload");
    }
  }

  private void validatePagination(int page, int limit) {
    if (page <= 0 || limit <= 0 || limit > 50) {
      throw new ApiException(400, "Invalid pagination parameters");
    }
  }

  private void validateFileExtension(String filename, Set<String> allowed, String errorMessage) {
    String ext = "";
    if (filename != null) {
      int idx = filename.lastIndexOf('.');
      if (idx >= 0) {
        ext = filename.substring(idx).toLowerCase();
      }
    }

    if (!allowed.contains(ext)) {
      throw new ApiException(400, errorMessage);
    }
  }
}
