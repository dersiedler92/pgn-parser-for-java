package io.alpa.pgnparser.auth;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Resolves the current Lichess access token from the active {@link HttpSession}.
 *
 * <p>This replaces the previous static {@code lichess.token} property: every outbound call to
 * Lichess now uses the token belonging to the currently authenticated user.
 */
@Component
public class LichessTokenProvider {

  /**
   * @return the access token bound to the current HTTP session
   * @throws LichessNotAuthenticatedException if there is no active request, no session, or no
   *     stored Lichess session
   */
  public String requireToken() {
    LichessSession session = currentSession();
    if (session == null) {
      throw new LichessNotAuthenticatedException(
          "No Lichess session. Start the OAuth flow at /api/auth/lichess/login.");
    }
    if (session.isExpired()) {
      throw new LichessNotAuthenticatedException("Lichess session expired; please log in again.");
    }
    return session.accessToken();
  }

  /**
   * @return the current Lichess session, or {@code null} if none. Never throws.
   */
  public LichessSession currentSession() {
    ServletRequestAttributes attrs =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attrs == null) {
      return null;
    }
    HttpSession httpSession = attrs.getRequest().getSession(false);
    if (httpSession == null) {
      return null;
    }
    Object stored = httpSession.getAttribute(LichessSession.SESSION_ATTRIBUTE);
    return stored instanceof LichessSession ls ? ls : null;
  }
}
