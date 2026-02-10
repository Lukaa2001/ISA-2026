package com.jutjubic.backend.websocket;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.jutjubic.backend.config.AppProperties;
import com.jutjubic.backend.security.JwtPrincipal;
import com.jutjubic.backend.security.JwtService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WatchPartySocketServer {
  private static final Logger log = LoggerFactory.getLogger(WatchPartySocketServer.class);
  private static final String USER_ATTR = "user";

  private final AppProperties appProperties;
  private final JwtService jwtService;
  private SocketIOServer server;

  public WatchPartySocketServer(AppProperties appProperties, JwtService jwtService) {
    this.appProperties = appProperties;
    this.jwtService = jwtService;
  }

  @PostConstruct
  public void start() {
    Configuration config = new Configuration();
    config.setHostname(appProperties.getSocket().getHost());
    config.setPort(appProperties.getSocket().getPort());
    config.setOrigin(appProperties.getFrontendUrl());

    server = new SocketIOServer(config);

    registerListeners();
    server.start();

    log.info("Socket.IO server started on {}:{}", appProperties.getSocket().getHost(), appProperties.getSocket().getPort());
  }

  @PreDestroy
  public void stop() {
    if (server != null) {
      server.stop();
      log.info("Socket.IO server stopped");
    }
  }

  private void registerListeners() {
    server.addConnectListener(client -> {
      JwtPrincipal principal = authenticate(client);
      if (principal == null) {
        client.disconnect();
        return;
      }

      client.set(USER_ATTR, principal);
      log.info("Socket connected: {}", principal.userId());
    });

    server.addDisconnectListener(client -> {
      JwtPrincipal principal = client.get(USER_ATTR);
      if (principal != null) {
        notifyDisconnectingRooms(client, principal);
        log.info("Socket disconnected: {}", principal.userId());
      }
    });

    server.addEventListener("time-sync", Object.class, (client, data, ackRequest) -> {
      if (ackRequest.isAckRequested()) {
        ackRequest.sendAckData(System.currentTimeMillis());
      }
    });

    server.addEventListener("join-room", String.class, (client, roomCode, ackRequest) -> {
      JwtPrincipal principal = client.get(USER_ATTR);
      if (principal == null || roomCode == null || roomCode.isBlank()) {
        return;
      }

      client.joinRoom(roomCode);
      emitToRoomExceptSender(roomCode, client, "user-joined", Map.of(
          "userId", principal.userId(),
          "email", principal.email()
      ));

      log.info("User {} joined room {}", principal.userId(), roomCode);
    });

    server.addEventListener("play-video", Map.class, (client, data, ackRequest) -> {
      Map<String, Object> payloadData = castMap(data);
      JwtPrincipal principal = client.get(USER_ATTR);
      if (principal == null) {
        return;
      }

      String roomCode = asString(payloadData.get("roomCode"));
      Number videoId = asNumber(payloadData.get("videoId"));
      String videoTitle = asString(payloadData.get("videoTitle"));
      if (roomCode == null || videoId == null) {
        return;
      }

      long serverTime = System.currentTimeMillis();
      long startAt = serverTime + 5000;

      Map<String, Object> payload = new HashMap<>();
      payload.put("videoId", videoId.longValue());
      payload.put("videoTitle", videoTitle);
      payload.put("startedBy", principal.userId());
      payload.put("serverTime", serverTime);
      payload.put("startAt", startAt);

      server.getRoomOperations(roomCode).sendEvent("play-video", payload);
      log.info("User {} played video {} in room {}, startAt={}", principal.userId(), videoId.longValue(), roomCode, startAt);
    });

    server.addEventListener("sync-video", Map.class, (client, data, ackRequest) -> {
      Map<String, Object> payloadData = castMap(data);
      JwtPrincipal principal = client.get(USER_ATTR);
      if (principal == null) {
        return;
      }

      String roomCode = asString(payloadData.get("roomCode"));
      String action = asString(payloadData.get("action"));
      Number currentTime = asNumber(payloadData.get("currentTime"));
      if (roomCode == null || action == null) {
        return;
      }

      long serverTime = System.currentTimeMillis();
      Map<String, Object> payload = new HashMap<>();
      payload.put("action", action);
      if (currentTime != null) {
        payload.put("currentTime", currentTime.doubleValue());
      }
      payload.put("serverTime", serverTime);
      payload.put("triggeredBy", principal.userId());

      emitToRoomExceptSender(roomCode, client, "sync-video", payload);
    });

    server.addEventListener("leave-room", String.class, (client, roomCode, ackRequest) -> {
      JwtPrincipal principal = client.get(USER_ATTR);
      if (principal == null || roomCode == null || roomCode.isBlank()) {
        return;
      }

      client.leaveRoom(roomCode);
      emitToRoomExceptSender(roomCode, client, "user-left", Map.of(
          "userId", principal.userId(),
          "email", principal.email()
      ));

      log.info("User {} left room {}", principal.userId(), roomCode);
    });
  }

  private JwtPrincipal authenticate(SocketIOClient client) {
    String token = client.getHandshakeData().getSingleUrlParam("token");

    if (token == null || token.isBlank()) {
      return null;
    }

    try {
      return jwtService.verifyToken(token);
    } catch (Exception ex) {
      return null;
    }
  }

  private void emitToRoomExceptSender(String roomCode, SocketIOClient sender, String event, Object payload) {
    for (SocketIOClient client : server.getRoomOperations(roomCode).getClients()) {
      if (!client.getSessionId().equals(sender.getSessionId())) {
        client.sendEvent(event, payload);
      }
    }
  }

  private void notifyDisconnectingRooms(SocketIOClient client, JwtPrincipal principal) {
    Set<String> rooms = client.getAllRooms();
    for (String room : rooms) {
      emitToRoomExceptSender(room, client, "user-left", Map.of(
          "userId", principal.userId(),
          "email", principal.email()
      ));
    }
  }

  private String asString(Object value) {
    if (value == null) {
      return null;
    }
    return String.valueOf(value);
  }

  private Number asNumber(Object value) {
    if (value instanceof Number number) {
      return number;
    }
    if (value == null) {
      return null;
    }

    try {
      return Double.parseDouble(String.valueOf(value));
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> castMap(Map data) {
    if (data == null) {
      return Map.of();
    }
    return (Map<String, Object>) data;
  }
}
