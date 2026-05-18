package io.alpa.pgnparser.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown when a request needs a Lichess OAuth session but none is present. */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class LichessNotAuthenticatedException extends RuntimeException {

  public LichessNotAuthenticatedException(String message) {
    super(message);
  }
}
