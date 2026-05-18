package io.alpa.pgnparser.lichess.config;

import io.alpa.pgnparser.lichess.api.StudiesApi;
import io.alpa.pgnparser.lichess.client.ApiClient;
import org.springframework.context.annotation.Configuration;

/**
 * Factory for the generated Lichess WebClient. Since access tokens are now per-user (sourced from
 * the active OAuth session) the {@link StudiesApi} is no longer a singleton bean — callers obtain a
 * properly authenticated instance via {@link #studiesApiFor(String)}.
 */
@Configuration
public class LichessClientConfig {

  private static final String BASE_PATH = "https://lichess.org";

  /** Builds a {@link StudiesApi} authenticated with the given Lichess access token. */
  public StudiesApi studiesApiFor(String accessToken) {
    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(BASE_PATH);
    apiClient.addDefaultHeader("Authorization", "Bearer " + accessToken);
    return new StudiesApi(apiClient);
  }
}
