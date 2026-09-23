package org.art.mt.dto;

/**
 * Result of toggling a like: whether the post is now liked by the caller, and
 * the post's total like count after the toggle.
 */
public record LikeStatusDTO(boolean liked, long likeCount) {
}
