package io.alpa.pgnparser.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Talks to Lichess OAuth + account endpoints. Synchronous facade over {@link WebClient}, mirroring
 * the {@code .block()} style already used by {@link
 * io.alpa.pgnparser.lichess.client.LichessStudiesClient}.
 */
@Service
public class LichessOAuthClient {

  private final WebClient webClient;
  private final LichessOAuthProperties properties;

  public LichessOAuthClient(WebClient.Builder builder, LichessOAuthProperties properties) {
    this.webClient = builder.build();
    this.properties = properties;
  }

  /** Exchanges an authorization code for an access token (PKCE, public client, no secret). */
  public TokenResponse exchangeCode(String code, String codeVerifier) {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("grant_type", "authorization_code");
    form.add("code", code);
    form.add("redirect_uri", properties.redirectUri());
    form.add("client_id", properties.clientId());
    form.add("code_verifier", codeVerifier);

    @SuppressWarnings("unchecked")
    Map<String, Object> body =
        webClient
            .post()
            .uri(properties.tokenUri())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromFormData(form))
            .retrieve()
            .bodyToMono(Map.class)
            .block();

    if (body == null || body.get("access_token") == null) {
      throw new IllegalStateException("Lichess token response did not contain an access_token");
    }

    String accessToken = (String) body.get("access_token");
    Instant expiresAt = null;
    Object expiresIn = body.get("expires_in");
    if (expiresIn instanceof Number n) {
      expiresAt = Instant.now().plus(Duration.ofSeconds(n.longValue()));
    }
    return new TokenResponse(accessToken, expiresAt);
  }

  /** Best-effort token revocation (Lichess: DELETE /api/token). Swallows errors. */
  public void revoke(String accessToken) {
    try {
      webClient
          .delete()
          .uri(properties.revokeUri())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
          .retrieve()
          .toBodilessEntity()
          .block();
    } catch (WebClientResponseException ignored) {
      // Best-effort: token may already be invalid.
    }
  }

  /** Calls {@code GET /api/account} and returns the username, or {@code null} on failure. */
  public String fetchUsername(String accessToken) {
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> account =
          webClient
              .get()
              .uri(properties.accountUri())
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
              .accept(MediaType.APPLICATION_JSON)
              .retrieve()
              .bodyToMono(Map.class)
              .block();
      if (account == null) {
        return null;
      }
      Object username = account.get("username");
      return username != null ? username.toString() : null;
    } catch (WebClientResponseException e) {
      return null;
    }
  }

  /** Successful token exchange result. {@code expiresAt} may be null if Lichess did not send it. */
  public record TokenResponse(String accessToken, Instant expiresAt) {}
}
