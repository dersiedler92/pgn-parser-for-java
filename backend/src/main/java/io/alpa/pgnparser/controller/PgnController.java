package io.alpa.pgnparser.controller;

import io.alpa.pgnparser.api.PgnApi;
import io.alpa.pgnparser.api.model.CombinedPgnResponse;
import io.alpa.pgnparser.api.model.PgnRequest;
import io.alpa.pgnparser.api.model.SeparatedPgnResponse;
import io.alpa.pgnparser.model.ChessGame;
import io.alpa.pgnparser.service.PgnConversionService;
import io.alpa.pgnparser.service.PgnParsingService;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/** REST controller for PGN parsing and conversion endpoints. */
@RestController
@RequestMapping("/api")
public class PgnController implements PgnApi {

  private final PgnConversionService pgnConversionService;
  private final PgnParsingService pgnParsingService;
  private final int maxPgnLength;

  /**
   * Constructs a new PgnController with required services.
   *
   * @param pgnConversionService service for PGN conversion
   * @param pgnParsingService service for PGN parsing
   * @param maxPgnLength max allowed length of the PGN body (chars)
   */
  public PgnController(
      PgnConversionService pgnConversionService,
      PgnParsingService pgnParsingService,
      @Value("${app.pgn.max-length:5000000}") int maxPgnLength) {
    this.pgnConversionService = pgnConversionService;
    this.pgnParsingService = pgnParsingService;
    this.maxPgnLength = maxPgnLength;
  }

  /**
   * Converts PGN to separated format (one line per variation).
   *
   * @param request PGN request
   * @return separated PGN response
   */
  @Override
  public ResponseEntity<SeparatedPgnResponse> convertPgnToSeparated(PgnRequest request) {
    String pgn = validatePgn(request);
    ChessGame chessGame = pgnParsingService.pgnToChessGame(pgn);
    List<String> separatedPgn = pgnConversionService.getVariationsAsStrings(chessGame);
    SeparatedPgnResponse response = new SeparatedPgnResponse();
    response.setVariations(separatedPgn);
    return ResponseEntity.ok(response);
  }

  /**
   * Converts PGN to combined format (single PGN string).
   *
   * @param request PGN request
   * @return combined PGN response
   */
  @Override
  public ResponseEntity<CombinedPgnResponse> convertPgnToCombined(PgnRequest request) {
    String pgn = validatePgn(request);
    ChessGame chessGame = pgnParsingService.pgnToChessGame(pgn);
    String combinedPgn = pgnConversionService.getCombinedPgn(chessGame);
    CombinedPgnResponse response = new CombinedPgnResponse();
    response.setCombined(combinedPgn);
    return ResponseEntity.ok(response);
  }

  private String validatePgn(PgnRequest request) {
    String pgn = request == null ? null : request.getPgn();
    if (pgn == null || pgn.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pgn is required");
    }
    if (pgn.length() > maxPgnLength) {
      throw new ResponseStatusException(
          HttpStatus.PAYLOAD_TOO_LARGE,
          "pgn exceeds maximum allowed length of " + maxPgnLength + " characters");
    }
    return pgn;
  }
}
