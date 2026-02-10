package com.jutjubic.backend.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jutjubic.backend.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class UploadEventPublisher {
  private static final Logger log = LoggerFactory.getLogger(UploadEventPublisher.class);

  private final RabbitTemplate rabbitTemplate;
  private final ObjectMapper objectMapper;
  private final AppProperties appProperties;

  public UploadEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper, AppProperties appProperties) {
    this.rabbitTemplate = rabbitTemplate;
    this.objectMapper = objectMapper;
    this.appProperties = appProperties;
  }

  public void publish(UploadEventMessage event) {
    try {
      byte[] jsonBody = objectMapper.writeValueAsBytes(event);
      Message jsonMessage = MessageBuilder.withBody(jsonBody)
          .setContentType(MessageProperties.CONTENT_TYPE_JSON)
          .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
          .build();

      rabbitTemplate.send(
          appProperties.getMq().getExchange(),
          appProperties.getMq().getJsonRoutingKey(),
          jsonMessage
      );

      byte[] protobufBody = UploadEventMapper.toProto(event).toByteArray();
      Message protobufMessage = MessageBuilder.withBody(protobufBody)
          .setContentType("application/x-protobuf")
          .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
          .build();

      rabbitTemplate.send(
          appProperties.getMq().getExchange(),
          appProperties.getMq().getProtobufRoutingKey(),
          protobufMessage
      );
    } catch (Exception ex) {
      log.error("Failed to publish upload event for videoId={}", event.videoId(), ex);
    }
  }
}
