package com.jutjubic.uploadconsumer.model;

import com.jutjubic.uploadconsumer.proto.UploadEventProto;
import java.time.OffsetDateTime;

public final class UploadEventMapper {
  private UploadEventMapper() {}

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
}
