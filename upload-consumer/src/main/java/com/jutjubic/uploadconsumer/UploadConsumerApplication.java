package com.jutjubic.uploadconsumer;

import com.jutjubic.uploadconsumer.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class UploadConsumerApplication {
  public static void main(String[] args) {
    SpringApplication.run(UploadConsumerApplication.class, args);
  }
}
