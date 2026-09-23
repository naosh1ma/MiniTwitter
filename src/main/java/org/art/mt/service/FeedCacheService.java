package org.art.mt.service;

import java.io.IOException;
import java.time.Duration;

import org.art.mt.dto.PagedResponse;
import org.art.mt.dto.PostDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Caches only the most-requested feed page (page 0, the frontend's default page
 * size) - and only the impersonal parts of it (no likedByCurrentUser, which is
 * per-viewer and always computed fresh by the caller on top of what's returned
 * here). Every Redis operation is deliberately fail-soft: if Redis is down or
 * unreachable, every method degrades to "no cache" rather than breaking the feed.
 */
@Service
public class FeedCacheService {

    private static final Logger logger = LoggerFactory.getLogger(FeedCacheService.class);
    /**
     * The only page this cache holds: page 0 at the frontend's default page
     * size. The key embeds the size, so the two must never be set independently
     * - isCacheable() is what decides, and the key is derived from the same
     * constant.
     */
    private static final int CACHED_PAGE = 0;
    private static final int CACHED_PAGE_SIZE = 20;
    private static final String PAGE_ZERO_KEY = "feed:page" + CACHED_PAGE + ":size" + CACHED_PAGE_SIZE;
    private static final Duration TTL = Duration.ofSeconds(30);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final JavaType pagedPostResponseType;

    public FeedCacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        // This is a Jackson 2.x mapper dedicated to this service's own Redis
        // serialization, deliberately not Spring's auto-configured ObjectMapper
        // bean - Spring Boot 4 defaults to Jackson 3.x (tools.jackson), a
        // different type entirely, and this cache format is an internal
        // implementation detail never exposed over HTTP.
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.pagedPostResponseType = objectMapper.getTypeFactory()
                .constructParametricType(PagedResponse.class, PostDTO.class);
    }

    /** Whether a feed request for this page/size is the one page this cache holds. */
    public boolean isCacheable(int page, int size) {
        return page == CACHED_PAGE && size == CACHED_PAGE_SIZE;
    }

    public PagedResponse<PostDTO> getPageZero() {
        try {
            String json = redisTemplate.opsForValue().get(PAGE_ZERO_KEY);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, pagedPostResponseType);
        } catch (DataAccessException | IOException e) {
            logger.warn("Feed cache read failed, falling back to a cache miss", e);
            return null;
        }
    }

    public void putPageZero(PagedResponse<PostDTO> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(PAGE_ZERO_KEY, json, TTL);
        } catch (DataAccessException | JsonProcessingException e) {
            logger.warn("Feed cache write failed, continuing without caching this response", e);
        }
    }

    public void evictPageZero() {
        try {
            redisTemplate.delete(PAGE_ZERO_KEY);
        } catch (DataAccessException e) {
            logger.warn("Feed cache eviction failed, cached page may be briefly stale", e);
        }
    }
}
