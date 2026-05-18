package io.alpa.pgnparser.auth;

import java.time.Instant;

/**
 * Snapshot of a user's Lichess OAuth session. Held in the {@link jakarta.servlet.http.HttpSession}
 * under {@link #SESSION_ATTRIBUTE}.
 */
public record LichessSession(String accessToken, String username, Instant expiresAt) {

  public static final String SESSION_ATTRIBUTE = "io.alpa.pgnparser.LICHESS_SESSION";

  public boolean isExpired() {
    return expiresAt != null && Instant.now().isAfter(expiresAt);
  }
}
