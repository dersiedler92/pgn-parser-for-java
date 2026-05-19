package io.alpa.pgnparser.lichess.client;

import io.alpa.pgnparser.api.model.CreateLichessStudyRequest;
import io.alpa.pgnparser.api.model.CreateLichessStudyResponse;
import io.alpa.pgnparser.auth.LichessTokenProvider;
import io.alpa.pgnparser.lichess.api.StudiesApi;
import io.alpa.pgnparser.lichess.config.LichessClientConfig;
import io.alpa.pgnparser.lichess.model.StudyUserSelection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class LichessStudiesClient {

  private final LichessClientConfig clientConfig;
  private final LichessTokenProvider tokenProvider;

  public LichessStudiesClient(
      LichessClientConfig clientConfig, LichessTokenProvider tokenProvider) {
    this.clientConfig = clientConfig;
    this.tokenProvider = tokenProvider;
  }

  public CreateLichessStudyResponse createStudyWithPgn(CreateLichessStudyRequest request) {
    StudiesApi studiesApi = clientConfig.studiesApiFor(tokenProvider.requireToken());

    // First create a study with the provided study name
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
            .block();

    try {
      Thread.sleep(3000); // 3000 milliseconds = 3 seconds
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt(); // restore interrupt flag
    }

    // Retrieve study specific information from the created study
    String studyId = createResponse.getId();
    String studyPgn = studiesApi.studyAllChaptersPgn(studyId, null, null, null, null).block();
    String chapterId = getChapterId(studyPgn);

    // Import the provided PGN to the newly created study
    studiesApi.apiStudyImportPGN(studyId, pgn, name, color, null, "gamebook").block();

    // Delete the default Chapter that is created whenever a new study is created
    studiesApi.apiStudyStudyIdChapterIdDelete(studyId, chapterId).block();

    // Return study ID and study URL to the Client
    CreateLichessStudyResponse response = new CreateLichessStudyResponse();
    response.setStudyId(studyId);
    response.setUrl("https://lichess.org/study/" + studyId);
    return response;
  }

  private String getChapterId(String pgn) {
    Pattern pattern = Pattern.compile("lichess\\.org/study/[^/\\s\"]+/([^\\s\"]+)");
    Matcher matcher = pattern.matcher(pgn);

    return matcher.find() ? matcher.group(1) : null;
  }
}
