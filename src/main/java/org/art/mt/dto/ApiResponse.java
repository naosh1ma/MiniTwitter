package org.art.mt.dto;
import java.time.LocalDateTime;

public class ApiResponse<T> {
  private boolean success;
  private String message;
  private T data;
  private LocalDateTime timestamp = LocalDateTime.now();

  public static <T> ApiResponse<T> ok(T data, String message) {
    ApiResponse<T> r = new ApiResponse<>();
    r.success = true; r.message = message; r.data = data; 
    return r;
  }
  public static <T> ApiResponse<T> error(String message) {
    ApiResponse<T> r = new ApiResponse<>();
    r.success = false; r.message = message; r.data = null; 
    return r;
  }
  public boolean isSuccess() { return success; }
  public String getMessage() { return message; }
  public T getData() { return data; }
  public LocalDateTime getTimestamp() { return timestamp; }
}
