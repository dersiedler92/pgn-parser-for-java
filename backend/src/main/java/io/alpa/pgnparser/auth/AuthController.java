package io.alpa.pgnparser.auth;

import io.alpa.pgnparser.api.AuthApi;
import io.alpa.pgnparser.api.model.AuthStatusResponse;
import jakarta.servlet.http.HttpSession;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Lichess OAuth (Authorization Code + PKCE) endpoints.
 *
 * <p>The flow is fully stateful via {@link HttpSession}: no database, no client-side token. The
 * browser carries {@code JSESSIONID} on subsequent requests (CORS allows credentials).
 */
@RestController
@RequestMapping("/api")
public class AuthController implements AuthApi {

  static final String PKCE_VERIFIER_ATTRIBUTE = "io.alpa.pgnparser.PKCE_VERIFIER";
  static final String OAUTH_STATE_ATTRIBUTE = "io.alpa.pgnparser.OAUTH_STATE";

  private final LichessOAuthProperties properties;
  private final LichessOAuthClient oauthClient;
  private final LichessTokenProvider tokenProvider;
  private final HttpSession httpSession;
  private final String frontendUrl;

  public AuthController(
      LichessOAuthProperties properties,
      LichessOAuthClient oauthClient,
      LichessTokenProvider tokenProvider,
      HttpSession httpSession,
      @Value("${app.frontend-url}") String frontendUrl) {
    this.properties = properties;
    this.oauthClient = oauthClient;
    this.tokenProvider = tokenProvider;
    this.httpSession = httpSession;
    this.frontendUrl = frontendUrl;
  }

  @Override
  public ResponseEntity<Void> lichessLogin() {
    String verifier = PkceUtil.generateCodeVerifier();
    String challenge = PkceUtil.codeChallengeS256(verifier);
    String state = PkceUtil.generateState();

    httpSession.setAttribute(PKCE_VERIFIER_ATTRIBUTE, verifier);
    httpSession.setAttribute(OAUTH_STATE_ATTRIBUTE, state);

    URI authorize =
        UriComponentsBuilder.fromUriString(properties.authorizeUri())
            .queryParam("response_type", "code")
            .queryParam("client_id", properties.clientId())
            .queryParam("redirect_uri", properties.redirectUri())
            .queryParam("scope", String.join(" ", properties.scopes()))
            .queryParam("code_challenge", challenge)
            .queryParam("code_challenge_method", "S256")
            .queryParam("state", state)
            .encode(StandardCharsets.UTF_8)
            .build()
            .toUri();

    return ResponseEntity.status(HttpStatus.FOUND).location(authorize).build();
  }

  @Override
  public ResponseEntity<Void> lichessCallback(String code, String state, String error) {
    URI redirectBack = URI.create(frontendUrl);

    if (error != null && !error.isBlank()) {
      clearOauthFlowAttributes();
      return ResponseEntity.status(HttpStatus.FOUND)
          .location(appendQuery(redirectBack, "login", "error"))
          .build();
    }

    String expectedState = (String) httpSession.getAttribute(OAUTH_STATE_ATTRIBUTE);
    String verifier = (String) httpSession.getAttribute(PKCE_VERIFIER_ATTRIBUTE);
    clearOauthFlowAttributes();

    if (code == null || state == null || expectedState == null || verifier == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    if (!Objects.equals(state, expectedState)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    LichessOAuthClient.TokenResponse token = oauthClient.exchangeCode(code, verifier);
    String username = oauthClient.fetchUsername(token.accessToken());
    Instant expiresAt = token.expiresAt();

    httpSession.setAttribute(
        LichessSession.SESSION_ATTRIBUTE,
        new LichessSession(token.accessToken(), username, expiresAt));

    return ResponseEntity.status(HttpStatus.FOUND)
        .location(appendQuery(redirectBack, "login", "success"))
        .build();
  }

  @Override
  public ResponseEntity<Void> logout() {
    LichessSession current = tokenProvider.currentSession();
    if (current != null) {
      oauthClient.revoke(current.accessToken());
    }
    httpSession.invalidate();
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<AuthStatusResponse> getCurrentUser() {
    LichessSession current = tokenProvider.currentSession();
    AuthStatusResponse body = new AuthStatusResponse();
    if (current == null || current.isExpired()) {
      body.setAuthenticated(false);
      return ResponseEntity.ok(body);
    }
    body.setAuthenticated(true);
    if (current.username() != null) {
      body.setUsername(current.username());
    }
    return ResponseEntity.ok(body);
  }

  private void clearOauthFlowAttributes() {
    httpSession.removeAttribute(PKCE_VERIFIER_ATTRIBUTE);
    httpSession.removeAttribute(OAUTH_STATE_ATTRIBUTE);
  }

  private static URI appendQuery(URI base, String name, String value) {
    return UriComponentsBuilder.fromUri(base).queryParam(name, value).build().toUri();
  }
}
