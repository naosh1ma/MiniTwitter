package org.art.mt.dto;
import java.time.LocalDateTime;

public class ApiResponse<T> {
  private boolean success;
  private String message;
  private T data;
  private LocalDateTime timestamp = LocalDateTime.now();

  public static <T> ApiResponse<T> ok(T data, String message) {
    ApiResponse<T> response = new ApiResponse<>();
    response.success = true;
    response.message = message;
    response.data = data;
    return response;
  }

  public static <T> ApiResponse<T> error(String message) {
    ApiResponse<T> response = new ApiResponse<>();
    response.success = false;
    response.message = message;
    response.data = null;
    return response;
  }
  public boolean isSuccess() { return success; }
  public String getMessage() { return message; }
  public T getData() { return data; }
  public LocalDateTime getTimestamp() { return timestamp; }
}
