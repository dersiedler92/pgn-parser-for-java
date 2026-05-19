package io.alpa.pgnparser.lichess.client;

import io.alpa.pgnparser.api.model.CreateLichessStudyRequest;
import io.alpa.pgnparser.api.model.CreateLichessStudyResponse;
import io.alpa.pgnparser.auth.LichessTokenProvider;
import io.alpa.pgnparser.lichess.api.StudiesApi;
import io.alpa.pgnparser.lichess.config.LichessClientConfig;
import io.alpa.pgnparser.lichess.model.StudyUserSelection;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.retry.Retry;

@Service
public class LichessStudiesClient {

  private static final Pattern CHAPTER_ID_PATTERN =
      Pattern.compile("lichess\\.org/study/[^/\\s\"]+/([^\\s\"]+)");

  // Polling parameters used while waiting for Lichess to materialize the default chapter
  // after a new study is created. Replaces the old fixed Thread.sleep(3000).
  private static final int CHAPTER_POLL_MAX_ATTEMPTS = 10;
  private static final Duration CHAPTER_POLL_INITIAL_BACKOFF = Duration.ofMillis(100);
  private static final Duration CHAPTER_POLL_MAX_BACKOFF = Duration.ofMillis(800);

  private final StudiesApi studiesApi;
  private final LichessTokenProvider tokenProvider;

  public LichessStudiesClient(StudiesApi studiesApi, LichessTokenProvider tokenProvider) {
    this.studiesApi = studiesApi;
    this.tokenProvider = tokenProvider;
  }

  public CreateLichessStudyResponse createStudyWithPgn(CreateLichessStudyRequest request) {
    // Resolve the per-request token once on the caller (servlet) thread, then propagate it
    // to all WebClient calls below via the Reactor Context (read by the auth filter in
    // LichessClientConfig).
    String token = tokenProvider.requireToken();

    String studyName = request.getStudyName();
    String pgn = request.getPgn();
    String color = "white";
    if (request.getColor() != null) {
      color = request.getColor().toString();
    }

    String name = studyName == null || studyName.isBlank() ? "Imported PGN" : studyName;

    var createResponse =
        studiesApi
            .apiStudyPost(
                name,
                "unlisted",
                StudyUserSelection.EVERYONE,
                StudyUserSelection.EVERYONE,
                StudyUserSelection.NOBODY,
                StudyUserSelection.EVERYONE,
                StudyUserSelection.NOBODY,
                "true")
            .contextWrite(withToken(token))
            .block();

    String studyId = createResponse.getId();

    // Poll for the default chapter with exponential backoff (replaces the prior 3s Thread.sleep).
    String chapterId = pollForChapterId(studyId, token);

    // Import the provided PGN to the newly created study
    studiesApi
        .apiStudyImportPGN(studyId, pgn, name, color, null, "gamebook")
        .contextWrite(withToken(token))
        .block();

    // Delete the default Chapter that is created whenever a new study is created
    if (chapterId != null) {
      studiesApi
          .apiStudyStudyIdChapterIdDelete(studyId, chapterId)
          .contextWrite(withToken(token))
          .block();
    }

    CreateLichessStudyResponse response = new CreateLichessStudyResponse();
    response.setStudyId(studyId);
    response.setUrl("https://lichess.org/study/" + studyId);
    return response;
  }

  /**
   * Polls {@code studyAllChaptersPgn} until a chapter id can be extracted from the response, or
   * the maximum number of attempts is exhausted. Uses exponential backoff between attempts.
   */
  private String pollForChapterId(String studyId, String token) {
    return Mono.defer(
            () ->
                studiesApi
                    .studyAllChaptersPgn(studyId, null, null, null, null)
                    .map(this::getChapterId)
                    .flatMap(id -> id == null ? Mono.error(new ChapterNotReadyException()) : Mono.just(id)))
        .retryWhen(
            Retry.backoff(CHAPTER_POLL_MAX_ATTEMPTS, CHAPTER_POLL_INITIAL_BACKOFF)
                .maxBackoff(CHAPTER_POLL_MAX_BACKOFF)
                .filter(t -> t instanceof ChapterNotReadyException))
        .onErrorReturn((String) null)
        .contextWrite(withToken(token))
        .block();
  }

  private static java.util.function.Function<Context, Context> withToken(String token) {
    return ctx -> ctx.put(LichessClientConfig.TOKEN_CONTEXT_KEY, token);
  }

  private String getChapterId(String pgn) {
    if (pgn == null) {
      return null;
    }
    Matcher matcher = CHAPTER_ID_PATTERN.matcher(pgn);
    return matcher.find() ? matcher.group(1) : null;
  }

  /** Signals that polling should retry because the default chapter is not yet present. */
  private static final class ChapterNotReadyException extends RuntimeException {
    ChapterNotReadyException() {
      super(null, null, false, false);
    }
  }
}
