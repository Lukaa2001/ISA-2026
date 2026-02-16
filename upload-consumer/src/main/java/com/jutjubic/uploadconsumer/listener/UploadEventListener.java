package com.jutjubic.uploadconsumer.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jutjubic.uploadconsumer.config.AppProperties;
import com.jutjubic.uploadconsumer.model.UploadEventMapper;
import com.jutjubic.uploadconsumer.model.UploadEventMessage;
import com.jutjubic.uploadconsumer.proto.UploadEventProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class UploadEventListener {
  private static final Logger log = LoggerFactory.getLogger(UploadEventListener.class);

  private final ObjectMapper objectMapper;
  private final AppProperties appProperties;

  public UploadEventListener(ObjectMapper objectMapper, AppProperties appProperties) {
    this.objectMapper = objectMapper;
    this.appProperties = appProperties;
  }

  @RabbitListener(queues = "${app.mq.json-queue}")
  public void onJsonEvent(byte[] payload) {
    try {
      UploadEventMessage event = objectMapper.readValue(payload, UploadEventMessage.class);
      log.info("[JSON] Received upload event: videoId={}, title='{}', author={}, videoSize={}B",
          event.videoId(), event.title(), event.authorUsername(), event.videoSizeBytes());
    } catch (Exception ex) {
      log.error("Failed to deserialize JSON upload event", ex);
    }
  }

  @RabbitListener(queues = "${app.mq.protobuf-queue}")
  public void onProtobufEvent(byte[] payload) {
    try {
      UploadEventProto.UploadEvent proto = UploadEventProto.UploadEvent.parseFrom(payload);
      UploadEventMessage event = UploadEventMapper.fromProto(proto);
      log.info("[PROTOBUF] Received upload event: videoId={}, title='{}', author={}, videoSize={}B",
          event.videoId(), event.title(), event.authorUsername(), event.videoSizeBytes());
    } catch (Exception ex) {
      log.error("Failed to deserialize Protobuf upload event", ex);
    }
  }
}
