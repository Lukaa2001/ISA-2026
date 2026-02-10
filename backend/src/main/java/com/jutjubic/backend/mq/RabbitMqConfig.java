package com.jutjubic.backend.mq;

import com.jutjubic.backend.config.AppProperties;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
  @Bean
  public DirectExchange uploadEventsExchange(AppProperties appProperties) {
    return new DirectExchange(appProperties.getMq().getExchange(), true, false);
  }

  @Bean
  public Queue uploadEventsJsonQueue(AppProperties appProperties) {
    return QueueBuilder.durable(appProperties.getMq().getJsonQueue()).build();
  }

  @Bean
  public Queue uploadEventsProtobufQueue(AppProperties appProperties) {
    return QueueBuilder.durable(appProperties.getMq().getProtobufQueue()).build();
  }

  @Bean
  public Binding uploadEventsJsonBinding(
      DirectExchange uploadEventsExchange,
      Queue uploadEventsJsonQueue,
      AppProperties appProperties
  ) {
    return BindingBuilder.bind(uploadEventsJsonQueue)
        .to(uploadEventsExchange)
        .with(appProperties.getMq().getJsonRoutingKey());
  }

  @Bean
  public Binding uploadEventsProtobufBinding(
      DirectExchange uploadEventsExchange,
      Queue uploadEventsProtobufQueue,
      AppProperties appProperties
  ) {
    return BindingBuilder.bind(uploadEventsProtobufQueue)
        .to(uploadEventsExchange)
        .with(appProperties.getMq().getProtobufRoutingKey());
  }
}
