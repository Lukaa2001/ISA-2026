package com.jutjubic.uploadconsumer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
  private MqProperties mq = new MqProperties();

  public MqProperties getMq() {
    return mq;
  }

  public void setMq(MqProperties mq) {
    this.mq = mq;
  }

  public static class MqProperties {
    private String exchange;
    private String jsonQueue;
    private String protobufQueue;
    private String jsonRoutingKey;
    private String protobufRoutingKey;

    public String getExchange() {
      return exchange;
    }

    public void setExchange(String exchange) {
      this.exchange = exchange;
    }

    public String getJsonQueue() {
      return jsonQueue;
    }

    public void setJsonQueue(String jsonQueue) {
      this.jsonQueue = jsonQueue;
    }

    public String getProtobufQueue() {
      return protobufQueue;
    }

    public void setProtobufQueue(String protobufQueue) {
      this.protobufQueue = protobufQueue;
    }

    public String getJsonRoutingKey() {
      return jsonRoutingKey;
    }

    public void setJsonRoutingKey(String jsonRoutingKey) {
      this.jsonRoutingKey = jsonRoutingKey;
    }

    public String getProtobufRoutingKey() {
      return protobufRoutingKey;
    }

    public void setProtobufRoutingKey(String protobufRoutingKey) {
      this.protobufRoutingKey = protobufRoutingKey;
    }
  }
}
