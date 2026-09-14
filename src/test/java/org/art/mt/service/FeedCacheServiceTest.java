package org.art.mt.service;

import java.util.List;

import org.art.mt.dto.PagedResponse;
import org.art.mt.dto.PostDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedCacheServiceTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    private FeedCacheService feedCacheService;

    @BeforeEach
    void setUp() {
        feedCacheService = new FeedCacheService(redisTemplate);
    }

    private PagedResponse<PostDTO> samplePage() {
        PostDTO dto = new PostDTO();
        dto.setId(1L);
        dto.setContent("hi");
        return new PagedResponse<>(List.of(dto), 0, 20, 1, 1, true);
    }

    @Test
    void getPageZero_returnsNullOnACacheMiss() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("feed:page0:size20")).thenReturn(null);

        assertThat(feedCacheService.getPageZero()).isNull();
    }

    @Test
    void putThenGet_roundTripsThePagedResponse() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String[] stored = new String[1];
        org.mockito.Mockito.doAnswer(invocation -> {
            stored[0] = invocation.getArgument(1);
            return null;
        }).when(valueOperations).set(org.mockito.ArgumentMatchers.eq("feed:page0:size20"),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any(java.time.Duration.class));
        when(valueOperations.get("feed:page0:size20")).thenAnswer(invocation -> stored[0]);

        feedCacheService.putPageZero(samplePage());
        PagedResponse<PostDTO> result = feedCacheService.getPageZero();

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("hi");
    }

    @Test
    void getPageZero_isACacheMissNotAnExceptionWhenRedisIsDown() {
        when(redisTemplate.opsForValue()).thenThrow(new RedisConnectionFailureException("connection refused"));

        assertThatCode(() -> assertThat(feedCacheService.getPageZero()).isNull())
                .doesNotThrowAnyException();
    }

    @Test
    void putPageZero_doesNotThrowWhenRedisIsDown() {
        when(redisTemplate.opsForValue()).thenThrow(new RedisConnectionFailureException("connection refused"));

        assertThatCode(() -> feedCacheService.putPageZero(samplePage())).doesNotThrowAnyException();
    }

    @Test
    void evictPageZero_doesNotThrowWhenRedisIsDown() {
        when(redisTemplate.delete("feed:page0:size20")).thenThrow(new RedisConnectionFailureException("connection refused"));

        assertThatCode(() -> feedCacheService.evictPageZero()).doesNotThrowAnyException();
    }
}
