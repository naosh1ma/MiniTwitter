package org.art.mt;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the full stack for real: real HTTP requests through DispatcherServlet,
 * the real SecurityFilterChain (including JwtAuthenticationFilter), the real
 * GlobalExceptionHandler, and a real (H2) database - no mocks anywhere.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class PostFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String registerAndLogin(String username) throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s@example.com","password":"password123"}
                                """.formatted(username, username)))
                .andExpect(status().isOk());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"password123"}
                                """.formatted(username)))
                .andExpect(status().isOk())
                .andReturn();

        String body = loginResult.getResponse().getContentAsString();
        return JsonPath.read(body, "$.data.token");
    }

    @Test
    void register_thenLogin_thenCreatePost_isVisibleInPublicFeed() throws Exception {
        String token = registerAndLogin("alice_flow");

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello from the integration test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("hello from the integration test"))
                .andExpect(jsonPath("$.data.author.username").value("alice_flow"));

        // The feed is public - no Authorization header here at all.
        mockMvc.perform(get("/api/posts/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].content").value("hello from the integration test"));
    }

    @Test
    void createPost_withoutAToken_isRejected() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"should not be allowed\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createPost_withGarbageToken_isRejected() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer this.is.not.a.valid.jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"should not be allowed\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void register_withInvalidFields_returns400WithFieldMessages() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"email\":\"not-an-email\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("Invalid email address")));
    }

    @Test
    void register_withDuplicateUsername_returns400() throws Exception {
        registerAndLogin("dup_flow");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"dup_flow\",\"email\":\"other@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        registerAndLogin("wrongpw_flow");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"wrongpw_flow\",\"password\":\"totally-wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createPost_withContentOver280Chars_returns400() throws Exception {
        String token = registerAndLogin("longpost_flow");
        String tooLong = "a".repeat(300);

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"" + tooLong + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletePost_byNonOwner_isForbidden() throws Exception {
        String ownerToken = registerAndLogin("owner_flow");
        String otherToken = registerAndLogin("intruder_flow");

        MvcResult createResult = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"owner's post\"}"))
                .andExpect(status().isOk())
                .andReturn();
        long postId = ((Number) JsonPath.read(createResult.getResponse().getContentAsString(), "$.data.id")).longValue();

        mockMvc.perform(delete("/api/posts/" + postId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deletePost_byOwner_succeedsAndRemovesItFromTheFeed() throws Exception {
        String token = registerAndLogin("selfdelete_flow");

        MvcResult createResult = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"delete me\"}"))
                .andExpect(status().isOk())
                .andReturn();
        long postId = ((Number) JsonPath.read(createResult.getResponse().getContentAsString(), "$.data.id")).longValue();

        mockMvc.perform(delete("/api/posts/" + postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/feed"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String body = result.getResponse().getContentAsString();
                    assertThat(body).doesNotContain("\"content\":\"delete me\"");
                });
    }

    @Test
    void likeToggle_flipsLikedStateAndCount() throws Exception {
        String authorToken = registerAndLogin("liked_author_flow");
        String likerToken = registerAndLogin("liker_flow");

        MvcResult createResult = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"like me\"}"))
                .andExpect(status().isOk())
                .andReturn();
        long postId = ((Number) JsonPath.read(createResult.getResponse().getContentAsString(), "$.data.id")).longValue();

        mockMvc.perform(post("/api/posts/" + postId + "/like")
                        .header("Authorization", "Bearer " + likerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked").value(true))
                .andExpect(jsonPath("$.data.likeCount").value(1));

        mockMvc.perform(post("/api/posts/" + postId + "/like")
                        .header("Authorization", "Bearer " + likerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked").value(false))
                .andExpect(jsonPath("$.data.likeCount").value(0));
    }

    @Test
    void followUnfollow_changesWhatShowsInTheFollowingFeed() throws Exception {
        String followerToken = registerAndLogin("follower_flow");
        String followeeToken = registerAndLogin("followee_flow");

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + followeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"from someone you might follow\"}"))
                .andExpect(status().isOk());

        // Before following: following feed is empty.
        mockMvc.perform(get("/api/posts/feed/following")
                        .header("Authorization", "Bearer " + followerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts").isEmpty());

        mockMvc.perform(post("/api/users/followee_flow/follow")
                        .header("Authorization", "Bearer " + followerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/feed/following")
                        .header("Authorization", "Bearer " + followerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].content").value("from someone you might follow"));

        mockMvc.perform(delete("/api/users/followee_flow/follow")
                        .header("Authorization", "Bearer " + followerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/feed/following")
                        .header("Authorization", "Bearer " + followerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts").isEmpty());
    }

    @Test
    void notificationsEndpoints_areAuthRequiredAndReachable() throws Exception {
        // No Kafka broker runs in this test environment, so a like/follow's
        // NotificationEvent is never actually consumed here - this test only
        // proves the notification read-side endpoints are wired up correctly
        // (auth-protected, return a well-formed empty result for a user with
        // no notifications). Real end-to-end delivery through Kafka is verified
        // manually against the docker-compose stack, which does run a broker.
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isForbidden());

        String token = registerAndLogin("notif_flow");

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(0));
    }

    @Test
    void followingYourself_isRejected() throws Exception {
        String token = registerAndLogin("selffollow_flow");

        mockMvc.perform(post("/api/users/selffollow_flow/follow")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestToUnmappedRoute_returns404NotAGeneric500() throws Exception {
        String token = registerAndLogin("notfound_flow");

        mockMvc.perform(get("/api/this/route/does/not/exist")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void comment_canBeAddedAndReadPublicly_andCommentCountShowsInTheFeed() throws Exception {
        String authorToken = registerAndLogin("comment_author_flow");
        String commenterToken = registerAndLogin("commenter_flow");

        MvcResult postResult = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"comment on me\"}"))
                .andExpect(status().isOk())
                .andReturn();
        long postId = ((Number) JsonPath.read(postResult.getResponse().getContentAsString(), "$.data.id")).longValue();

        mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .header("Authorization", "Bearer " + commenterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"nice post!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("nice post!"))
                .andExpect(jsonPath("$.data.author.username").value("commenter_flow"));

        // Comments are readable with no Authorization header at all.
        mockMvc.perform(get("/api/posts/" + postId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].content").value("nice post!"));

        mockMvc.perform(get("/api/posts/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].commentCount").value(1));
    }

    @Test
    void addingAComment_withoutAToken_isRejected() throws Exception {
        String token = registerAndLogin("noauth_comment_flow");
        MvcResult postResult = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"a post\"}"))
                .andExpect(status().isOk())
                .andReturn();
        long postId = ((Number) JsonPath.read(postResult.getResponse().getContentAsString(), "$.data.id")).longValue();

        mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"no token here\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteComment_byNonAuthor_isForbidden() throws Exception {
        String authorToken = registerAndLogin("post_owner_for_comment_flow");
        String commenterToken = registerAndLogin("comment_owner_flow");
        String intruderToken = registerAndLogin("comment_intruder_flow");

        MvcResult postResult = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"a post\"}"))
                .andExpect(status().isOk())
                .andReturn();
        long postId = ((Number) JsonPath.read(postResult.getResponse().getContentAsString(), "$.data.id")).longValue();

        MvcResult commentResult = mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .header("Authorization", "Bearer " + commenterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"my comment\"}"))
                .andExpect(status().isOk())
                .andReturn();
        long commentId = ((Number) JsonPath.read(commentResult.getResponse().getContentAsString(), "$.data.id")).longValue();

        mockMvc.perform(delete("/api/posts/" + postId + "/comments/" + commentId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/posts/" + postId + "/comments/" + commentId)
                        .header("Authorization", "Bearer " + commenterToken))
                .andExpect(status().isOk());
    }
}
