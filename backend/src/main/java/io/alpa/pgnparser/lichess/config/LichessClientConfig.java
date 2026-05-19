package io.alpa.pgnparser.lichess.config;

import io.alpa.pgnparser.lichess.api.StudiesApi;
import io.alpa.pgnparser.lichess.client.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Configures a single shared {@link WebClient}/{@link ApiClient}/{@link StudiesApi} for all Lichess
 * calls. Avoids the prior pattern of constructing a fresh {@link WebClient} (and its underlying
 * Netty connection pool / TLS context) on every request.
 *
 * <p>Per-request bearer tokens are injected via Reactor {@link reactor.util.context.Context}: each
 * call site supplies the token with {@code .contextWrite(Context.of(TOKEN_CONTEXT_KEY, token))} and
 * the filter below reads it back when the request is built.
 */
@Configuration
public class LichessClientConfig {

  public static final String TOKEN_CONTEXT_KEY = "lichessAccessToken";
  private static final String BASE_PATH = "https://lichess.org";

  @Bean
  public WebClient lichessWebClient() {
    return ApiClient.buildWebClientBuilder()
        .baseUrl(BASE_PATH)
        .filter(bearerTokenFilter())
        .build();
  }

  @Bean
  public ApiClient lichessApiClient(WebClient lichessWebClient) {
    ApiClient apiClient = new ApiClient(lichessWebClient);
    apiClient.setBasePath(BASE_PATH);
    return apiClient;
  }

  @Bean
  public StudiesApi lichessStudiesApi(ApiClient lichessApiClient) {
    return new StudiesApi(lichessApiClient);
  }

  /**
   * Reads a bearer token from the Reactor Context (key {@link #TOKEN_CONTEXT_KEY}) and adds it as
   * an {@code Authorization} header. If no token is present, the request goes out unauthenticated
   * (Lichess will respond 401 — callers should ensure they wrote the token to context first).
   */
  private static ExchangeFilterFunction bearerTokenFilter() {
    return (request, next) ->
        Mono.deferContextual(
            ctx -> {
              if (!ctx.hasKey(TOKEN_CONTEXT_KEY)) {
                return next.exchange(request);
              }
              String token = ctx.get(TOKEN_CONTEXT_KEY);
              ClientRequest authorized =
                  ClientRequest.from(request)
                      .headers(h -> h.set(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                      .build();
              return next.exchange(authorized);
            });
  }
}
