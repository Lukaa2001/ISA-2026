package com.jutjubic.uploadconsumer.model;

import java.time.OffsetDateTime;
import java.util.List;

public record UploadEventMessage(
    long videoId,
    String title,
    long videoSizeBytes,
    long thumbnailSizeBytes,
    long authorId,
    String authorUsername,
    String description,
    List<String> tags,
    String videoPath,
    String thumbnailPath,
    OffsetDateTime uploadedAt
) {}
