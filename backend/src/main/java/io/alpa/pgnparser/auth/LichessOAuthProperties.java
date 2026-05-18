package io.alpa.pgnparser.auth;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the Lichess OAuth 2.0 (Authorization Code + PKCE) flow.
 *
 * <p>Lichess is a public OAuth client: there is no client secret. The {@code clientId} is a
 * free-form string shown to the user on the Lichess consent page.
 */
@ConfigurationProperties(prefix = "lichess.oauth")
public record LichessOAuthProperties(
    String clientId,
    String authorizeUri,
    String tokenUri,
    String revokeUri,
    String accountUri,
    String redirectUri,
    List<String> scopes) {}
