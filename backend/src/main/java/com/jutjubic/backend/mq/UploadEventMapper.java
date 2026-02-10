package com.jutjubic.backend.mq;

import com.jutjubic.backend.proto.UploadEventProto;
import java.time.OffsetDateTime;

public final class UploadEventMapper {
  private UploadEventMapper() {}

  public static UploadEventProto.UploadEvent toProto(UploadEventMessage event) {
    UploadEventProto.UploadEvent.Builder builder = UploadEventProto.UploadEvent.newBuilder()
        .setVideoId(event.videoId())
        .setTitle(nullToEmpty(event.title()))
        .setVideoSizeBytes(event.videoSizeBytes())
        .setThumbnailSizeBytes(event.thumbnailSizeBytes())
        .setAuthorId(event.authorId())
        .setAuthorUsername(nullToEmpty(event.authorUsername()))
        .setDescription(nullToEmpty(event.description()))
        .setVideoPath(nullToEmpty(event.videoPath()))
        .setThumbnailPath(nullToEmpty(event.thumbnailPath()))
        .setUploadedAt(event.uploadedAt() == null ? "" : event.uploadedAt().toString());

    if (event.tags() != null) {
      builder.addAllTags(event.tags());
    }

    return builder.build();
  }

  public static UploadEventMessage fromProto(UploadEventProto.UploadEvent proto) {
    OffsetDateTime uploadedAt = null;
    if (!proto.getUploadedAt().isBlank()) {
      uploadedAt = OffsetDateTime.parse(proto.getUploadedAt());
    }

    return new UploadEventMessage(
        proto.getVideoId(),
        proto.getTitle(),
        proto.getVideoSizeBytes(),
        proto.getThumbnailSizeBytes(),
        proto.getAuthorId(),
        proto.getAuthorUsername(),
        proto.getDescription(),
        proto.getTagsList(),
        proto.getVideoPath(),
        proto.getThumbnailPath(),
        uploadedAt
    );
  }

  private static String nullToEmpty(String value) {
    return value == null ? "" : value;
  }
}
