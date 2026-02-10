package com.jutjubic.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
  private String backendUrl;
  private String frontendUrl;
  private String uploadDir;
  private SocketProperties socket = new SocketProperties();
  private MqProperties mq = new MqProperties();

  public String getBackendUrl() {
    return backendUrl;
  }

  public void setBackendUrl(String backendUrl) {
    this.backendUrl = backendUrl;
  }

  public String getFrontendUrl() {
    return frontendUrl;
  }

  public void setFrontendUrl(String frontendUrl) {
    this.frontendUrl = frontendUrl;
  }

  public String getUploadDir() {
    return uploadDir;
  }

  public void setUploadDir(String uploadDir) {
    this.uploadDir = uploadDir;
  }

  public SocketProperties getSocket() {
    return socket;
  }

  public void setSocket(SocketProperties socket) {
    this.socket = socket;
  }

  public MqProperties getMq() {
    return mq;
  }

  public void setMq(MqProperties mq) {
    this.mq = mq;
  }

  public static class SocketProperties {
    private String host;
    private int port;

    public String getHost() {
      return host;
    }

    public void setHost(String host) {
      this.host = host;
    }

    public int getPort() {
      return port;
    }

    public void setPort(int port) {
      this.port = port;
    }
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
