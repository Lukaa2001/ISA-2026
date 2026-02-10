package com.jutjubic.backend.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("error", ex.getMessage());
    return ResponseEntity.status(ex.getStatusCode()).body(body);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
    List<Map<String, String>> details = ex.getBindingResult().getFieldErrors().stream()
        .map(this::mapFieldError)
        .toList();

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("error", "Validation error");
    body.put("details", details);
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String, Object>> handleConstraintValidation(ConstraintViolationException ex) {
    List<Map<String, String>> details = ex.getConstraintViolations().stream()
        .map(v -> {
          Map<String, String> item = new LinkedHashMap<>();
          item.put("field", v.getPropertyPath().toString());
          item.put("message", v.getMessage());
          return item;
        })
        .toList();

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("error", "Validation error");
    body.put("details", details);
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler({MultipartException.class, MaxUploadSizeExceededException.class})
  public ResponseEntity<Map<String, Object>> handleMultipart(Exception ex) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("error", ex.getMessage());
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
  public ResponseEntity<Map<String, Object>> handleBadRequest(Exception ex) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("error", "Validation error");
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleUnknown(Exception ex) {
    ex.printStackTrace();
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("error", "Internal server error");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }

  private Map<String, String> mapFieldError(FieldError error) {
    Map<String, String> item = new LinkedHashMap<>();
    item.put("field", error.getField());
    item.put("message", error.getDefaultMessage());
    return item;
  }
}
