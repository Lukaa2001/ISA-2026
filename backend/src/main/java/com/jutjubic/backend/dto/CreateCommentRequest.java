package com.jutjubic.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCommentRequest {
  @NotBlank(message = "Comment text is required")
  @Size(max = 1000, message = "Comment must be at most 1000 characters")
  private String text;

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }
}
