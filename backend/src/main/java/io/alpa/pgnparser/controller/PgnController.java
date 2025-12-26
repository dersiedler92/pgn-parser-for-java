package io.alpa.pgnparser.controller;

import io.alpa.pgnparser.api.PgnApi;
import io.alpa.pgnparser.api.model.CombinedPgnResponse;
import io.alpa.pgnparser.api.model.PgnRequest;
import io.alpa.pgnparser.api.model.SeparatedPgnResponse;
import io.alpa.pgnparser.model.ChessGame;
import io.alpa.pgnparser.service.PgnConversionService;
import io.alpa.pgnparser.service.PgnParsingService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for PGN parsing and conversion endpoints. */
@RestController
@RequestMapping("/api")
public class PgnController implements PgnApi {

  private final PgnConversionService pgnConversionService;
  private final PgnParsingService pgnParsingService;

  /**
   * Constructs a new PgnController with required services.
   *
   * @param pgnConversionService service for PGN conversion
   * @param pgnParsingService service for PGN parsing
   */
  public PgnController(
      PgnConversionService pgnConversionService, PgnParsingService pgnParsingService) {
    this.pgnConversionService = pgnConversionService;
    this.pgnParsingService = pgnParsingService;
  }

  /**
   * Converts PGN to separated format (one line per variation).
   *
   * @param request PGN request
   * @return separated PGN response
   */
  @Override
  public ResponseEntity<SeparatedPgnResponse> convertPgnToSeparated(PgnRequest request) {
    ChessGame chessGame = pgnParsingService.pgnToChessGame(request.getPgn());
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
    ChessGame chessGame = pgnParsingService.pgnToChessGame(request.getPgn());
    String combinedPgn = pgnConversionService.getCombinedPgn(chessGame);
    CombinedPgnResponse response = new CombinedPgnResponse();
    response.setCombined(combinedPgn);
    return ResponseEntity.ok(response);
  }
}
