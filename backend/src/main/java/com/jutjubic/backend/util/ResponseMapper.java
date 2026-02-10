package com.jutjubic.backend.util;

import com.jutjubic.backend.entity.Comment;
import com.jutjubic.backend.entity.User;
import com.jutjubic.backend.entity.Video;
import com.jutjubic.backend.entity.WatchParty;
import com.jutjubic.backend.entity.WatchPartyMember;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ResponseMapper {
  private ResponseMapper() {}

  public static Map<String, Object> mapVideoForList(Video video) {
    Map<String, Object> body = mapVideoCommon(video);
    body.put("user", Map.of(
        "id", video.getUser().getId(),
        "username", video.getUser().getUsername()
    ));
    return body;
  }

  public static Map<String, Object> mapVideoForDetail(Video video) {
    Map<String, Object> body = mapVideoCommon(video);
    body.put("user", Map.of(
        "id", video.getUser().getId(),
        "username", video.getUser().getUsername(),
        "firstName", video.getUser().getFirstName(),
        "lastName", video.getUser().getLastName()
    ));
    return body;
  }

  public static Map<String, Object> mapComment(Comment comment) {
    return Map.of(
        "id", comment.getId(),
        "text", comment.getText(),
        "createdAt", comment.getCreatedAt(),
        "user", Map.of(
            "id", comment.getUser().getId(),
            "username", comment.getUser().getUsername()
        )
    );
  }

  public static Map<String, Object> mapWatchParty(WatchParty party, List<WatchPartyMember> members) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("id", party.getId());
    body.put("roomCode", party.getRoomCode());
    body.put("isActive", party.isActive());
    body.put("createdAt", party.getCreatedAt());
    body.put("creator", Map.of(
        "id", party.getCreator().getId(),
        "username", party.getCreator().getUsername()
    ));
    body.put("members", members.stream().map(ResponseMapper::mapWatchPartyMember).toList());
    body.put("currentVideoId", party.getCurrentVideoId());
    return body;
  }

  public static Map<String, Object> mapWatchPartyMember(WatchPartyMember member) {
    return Map.of(
        "id", member.getId(),
        "joinedAt", member.getJoinedAt(),
        "user", Map.of(
            "id", member.getUser().getId(),
            "username", member.getUser().getUsername()
        )
    );
  }

  public static Map<String, Object> mapPublicUser(User user, long videosCount, long commentsCount) {
    return Map.of(
        "id", user.getId(),
        "username", user.getUsername(),
        "firstName", user.getFirstName(),
        "lastName", user.getLastName(),
        "createdAt", user.getCreatedAt(),
        "_count", Map.of(
            "videos", videosCount,
            "comments", commentsCount
        )
    );
  }

  private static Map<String, Object> mapVideoCommon(Video video) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("id", video.getId());
    body.put("title", video.getTitle());
    body.put("description", video.getDescription());
    body.put("tags", video.getTags());
    body.put("thumbnailPath", video.getThumbnailPath());
    body.put("videoPath", video.getVideoPath());
    body.put("viewCount", video.getViewCount());
    body.put("latitude", video.getLatitude());
    body.put("longitude", video.getLongitude());
    body.put("createdAt", video.getCreatedAt());
    body.put("updatedAt", video.getUpdatedAt());
    return body;
  }
}
