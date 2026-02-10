package com.jutjubic.backend.service;

import com.jutjubic.backend.config.AppProperties;
import com.jutjubic.backend.entity.User;
import com.jutjubic.backend.entity.Video;
import com.jutjubic.backend.exception.ApiException;
import com.jutjubic.backend.mq.UploadEventMessage;
import com.jutjubic.backend.mq.UploadEventPublisher;
import com.jutjubic.backend.repository.UserRepository;
import com.jutjubic.backend.repository.VideoRepository;
import com.jutjubic.backend.util.ResponseMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VideoService {
  private static final Logger log = LoggerFactory.getLogger(VideoService.class);

  private final VideoRepository videoRepository;
  private final UserRepository userRepository;
  private final CacheService cacheService;
  private final UploadEventPublisher uploadEventPublisher;
  private final Path uploadRoot;
  private final Path videosDir;
  private final Path thumbnailsDir;
  private final Path tmpDir;

  public VideoService(
      VideoRepository videoRepository,
      UserRepository userRepository,
      CacheService cacheService,
      UploadEventPublisher uploadEventPublisher,
      AppProperties appProperties
  ) {
    this.videoRepository = videoRepository;
    this.userRepository = userRepository;
    this.cacheService = cacheService;
    this.uploadEventPublisher = uploadEventPublisher;

    this.uploadRoot = Path.of(appProperties.getUploadDir()).toAbsolutePath().normalize();
    this.videosDir = uploadRoot.resolve("videos");
    this.thumbnailsDir = uploadRoot.resolve("thumbnails");
    this.tmpDir = uploadRoot.resolve("tmp");

    ensureDirectories();
  }

  @Transactional
  public Map<String, Object> create(CreateVideoInput input) {
    User user = userRepository.findById(input.userId())
        .orElseThrow(() -> new ApiException(404, "User not found"));

    String videoFilename = UUID.randomUUID() + getExtension(input.videoFile().getOriginalFilename());
    String thumbnailFilename = UUID.randomUUID() + getExtension(input.thumbnailFile().getOriginalFilename());

    Path tmpVideoPath = tmpDir.resolve(videoFilename);
    Path tmpThumbPath = tmpDir.resolve(thumbnailFilename);
    Path finalVideoPath = videosDir.resolve(videoFilename);
    Path finalThumbPath = thumbnailsDir.resolve(thumbnailFilename);

    try {
      input.videoFile().transferTo(tmpVideoPath);
      input.thumbnailFile().transferTo(tmpThumbPath);

      Files.move(tmpVideoPath, finalVideoPath, StandardCopyOption.REPLACE_EXISTING);
      Files.move(tmpThumbPath, finalThumbPath, StandardCopyOption.REPLACE_EXISTING);

      Video video = new Video();
      video.setTitle(input.title());
      video.setDescription(input.description());
      video.setTags(input.tags().toArray(String[]::new));
      video.setVideoPath("videos/" + videoFilename);
      video.setThumbnailPath("thumbnails/" + thumbnailFilename);
      video.setLatitude(input.latitude());
      video.setLongitude(input.longitude());
      video.setUser(user);

      Video saved = videoRepository.save(video);

      try {
        byte[] thumbnailBytes = Files.readAllBytes(finalThumbPath);
        cacheService.setBuffer("thumb:" + saved.getId(), thumbnailBytes, 24 * 60 * 60);
      } catch (Exception ignored) {
        // Non-critical cache write.
      }

      publishUploadEvent(saved, user, input, finalVideoPath, finalThumbPath);

      return ResponseMapper.mapVideoForList(saved);
    } catch (IOException ex) {
      cleanupFile(finalVideoPath);
      cleanupFile(finalThumbPath);
      cleanupFile(tmpVideoPath);
      cleanupFile(tmpThumbPath);
      throw new ApiException(500, "Failed to store uploaded files");
    }
  }

  @Transactional(readOnly = true)
  public Map<String, Object> findAll(int page, int limit) {
    PageRequest request = PageRequest.of(page - 1, limit);
    Page<Video> videosPage = videoRepository.findAllByOrderByCreatedAtDesc(request);

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

  @Transactional(readOnly = true)
  public Video findById(long id) {
    return videoRepository.findDetailsById(id)
        .orElseThrow(() -> new ApiException(404, "Video not found"));
  }

  @Transactional(readOnly = true)
  public ThumbnailResult getThumbnail(long id) {
    byte[] cached = cacheService.getBuffer("thumb:" + id);
    if (cached != null) {
      return new ThumbnailResult(cached, "image/jpeg");
    }

    Video video = videoRepository.findById(id)
        .orElseThrow(() -> new ApiException(404, "Video not found"));

    Path thumbnailPath = uploadRoot.resolve(video.getThumbnailPath()).normalize();
    if (!thumbnailPath.startsWith(uploadRoot)) {
      throw new ApiException(404, "Thumbnail not found");
    }

    try {
      byte[] bytes = Files.readAllBytes(thumbnailPath);
      cacheService.setBuffer("thumb:" + id, bytes, 24 * 60 * 60);
      return new ThumbnailResult(bytes, detectContentType(video.getThumbnailPath()));
    } catch (IOException ex) {
      throw new ApiException(404, "Thumbnail not found");
    }
  }

  public record CreateVideoInput(
      String title,
      String description,
      List<String> tags,
      Double latitude,
      Double longitude,
      Long userId,
      MultipartFile videoFile,
      MultipartFile thumbnailFile
  ) {}

  public record ThumbnailResult(byte[] buffer, String contentType) {}

  private String getExtension(String originalName) {
    if (originalName == null) {
      return "";
    }

    int idx = originalName.lastIndexOf('.');
    return idx >= 0 ? originalName.substring(idx) : "";
  }

  private String detectContentType(String filePath) {
    String lower = filePath.toLowerCase();
    if (lower.endsWith(".png")) {
      return "image/png";
    }
    if (lower.endsWith(".webp")) {
      return "image/webp";
    }
    return "image/jpeg";
  }

  private void ensureDirectories() {
    try {
      Files.createDirectories(videosDir);
      Files.createDirectories(thumbnailsDir);
      Files.createDirectories(tmpDir);
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to initialize upload directories", ex);
    }
  }

  private void cleanupFile(Path path) {
    try {
      Files.deleteIfExists(path);
    } catch (IOException ignored) {
      // Best-effort cleanup.
    }
  }

  private void publishUploadEvent(
      Video video,
      User author,
      CreateVideoInput input,
      Path videoPath,
      Path thumbnailPath
  ) {
    try {
      long videoSize = Files.size(videoPath);
      long thumbnailSize = Files.size(thumbnailPath);

      UploadEventMessage event = new UploadEventMessage(
          video.getId(),
          video.getTitle(),
          videoSize,
          thumbnailSize,
          author.getId(),
          author.getUsername(),
          input.description(),
          input.tags(),
          video.getVideoPath(),
          video.getThumbnailPath(),
          OffsetDateTime.now()
      );

      uploadEventPublisher.publish(event);
    } catch (Exception ex) {
      log.warn("Upload event dispatch failed for videoId={}", video.getId(), ex);
    }
  }
}
