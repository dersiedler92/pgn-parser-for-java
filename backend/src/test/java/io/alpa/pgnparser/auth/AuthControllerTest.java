package io.alpa.pgnparser.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(AuthControllerTest.TestBeans.class)
@TestPropertySource(
    properties = {
      "app.frontend-url=http://localhost:5173",
    })
@WithMockUser
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private LichessOAuthClient oauthClient;
  @MockBean private LichessTokenProvider tokenProvider;

  @TestConfiguration
  static class TestBeans {
    @Bean
    LichessOAuthProperties lichessOAuthProperties() {
      return new LichessOAuthProperties(
          "test-client",
          "https://lichess.org/oauth",
          "https://lichess.org/api/token",
          "https://lichess.org/api/token",
          "https://lichess.org/api/account",
          "http://localhost:8080/api/auth/lichess/callback",
          List.of("study:read", "study:write"));
    }
  }

  @Test
  void login_redirectsToLichessWithPkceAndStoresVerifierInSession() throws Exception {
    var result =
        mockMvc
            .perform(get("/api/auth/lichess/login"))
            .andExpect(status().isFound())
            .andExpect(
                header()
                    .string(
                        "Location", org.hamcrest.Matchers.startsWith("https://lichess.org/oauth?")))
            .andExpect(
                header()
                    .string(
                        "Location",
                        org.hamcrest.Matchers.containsString("code_challenge_method=S256")))
            .andExpect(
                header()
                    .string(
                        "Location", org.hamcrest.Matchers.containsString("client_id=test-client")))
            .andReturn();

    HttpSession session = result.getRequest().getSession(false);
    assertThat(session).isNotNull();
    assertThat(session.getAttribute(AuthController.PKCE_VERIFIER_ATTRIBUTE)).isNotNull();
    assertThat(session.getAttribute(AuthController.OAUTH_STATE_ATTRIBUTE)).isNotNull();
  }

  @Test
  void callback_rejectsRequestWithMismatchedState() throws Exception {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(AuthController.OAUTH_STATE_ATTRIBUTE, "expected-state");
    session.setAttribute(AuthController.PKCE_VERIFIER_ATTRIBUTE, "the-verifier");

    mockMvc
        .perform(
            get("/api/auth/lichess/callback")
                .param("code", "abc")
                .param("state", "wrong")
                .session(session))
        .andExpect(status().isBadRequest());
  }

  @Test
  void callback_withMatchingStateExchangesCodeAndStoresSession() throws Exception {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(AuthController.OAUTH_STATE_ATTRIBUTE, "state-1");
    session.setAttribute(AuthController.PKCE_VERIFIER_ATTRIBUTE, "verifier-1");

    when(oauthClient.exchangeCode(anyString(), anyString()))
        .thenReturn(new LichessOAuthClient.TokenResponse("token-xyz", null));
    when(oauthClient.fetchUsername("token-xyz")).thenReturn("alice");

    mockMvc
        .perform(
            get("/api/auth/lichess/callback")
                .param("code", "thecode")
                .param("state", "state-1")
                .session(session))
        .andExpect(status().isFound())
        .andExpect(
            header().string("Location", org.hamcrest.Matchers.containsString("login=success")));

    LichessSession stored = (LichessSession) session.getAttribute(LichessSession.SESSION_ATTRIBUTE);
    assertThat(stored).isNotNull();
    assertThat(stored.accessToken()).isEqualTo("token-xyz");
    assertThat(stored.username()).isEqualTo("alice");
    assertThat(session.getAttribute(AuthController.PKCE_VERIFIER_ATTRIBUTE)).isNull();
    assertThat(session.getAttribute(AuthController.OAUTH_STATE_ATTRIBUTE)).isNull();
  }

  @Test
  void callback_withErrorParameterRedirectsToFrontendWithErrorFlag() throws Exception {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(AuthController.OAUTH_STATE_ATTRIBUTE, "s");
    session.setAttribute(AuthController.PKCE_VERIFIER_ATTRIBUTE, "v");

    mockMvc
        .perform(get("/api/auth/lichess/callback").param("error", "access_denied").session(session))
        .andExpect(status().isFound())
        .andExpect(
            header().string("Location", org.hamcrest.Matchers.containsString("login=error")));
  }

  @Test
  void me_returnsUnauthenticatedWhenNoSession() throws Exception {
    when(tokenProvider.currentSession()).thenReturn(null);

    mockMvc
        .perform(get("/api/auth/me"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(jsonPath("$.authenticated").value(false));
  }

  @Test
  void me_returnsUsernameWhenAuthenticated() throws Exception {
    when(tokenProvider.currentSession()).thenReturn(new LichessSession("tok", "alice", null));

    mockMvc
        .perform(get("/api/auth/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.authenticated").value(true))
        .andExpect(jsonPath("$.username").value("alice"));
  }

  @Test
  void logout_revokesTokenAndInvalidatesSession() throws Exception {
    when(tokenProvider.currentSession()).thenReturn(new LichessSession("tok", "alice", null));

    mockMvc.perform(post("/api/auth/logout").with(csrf())).andExpect(status().isNoContent());

    verify(oauthClient).revoke("tok");
  }
}
