package org.art.mt.dto;
import java.util.List;

public class PagedResponse<T> {
  private List<T> content;
  private int page;
  private int size;
  private long totalElements;
  private int totalPages;
  private boolean last;

  public PagedResponse() {}

  public PagedResponse(List<T> content, int page, int size, long totalElements, int totalPages, boolean last) {
    this.content = content;
    this.page = page;
    this.size = size;
    this.totalElements = totalElements;
    this.totalPages = totalPages;
    this.last = last;
  }
  public void setContent(List<T> content) { this.content = content; }
  public void setPage(int page) { this.page = page; }
  public void setSize(int size) { this.size = size; }
  public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
  public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
  public void setLast(boolean last) { this.last = last; }
  public List<T> getContent() { return content; }
  public int getPage() { return page; }
  public int getSize() { return size; }
  public long getTotalElements() { return totalElements; }
  public int getTotalPages() { return totalPages; }
  public boolean isLast() { return last; }
}