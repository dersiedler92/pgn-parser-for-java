package io.alpa.pgnparser.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/** Generates PKCE verifier/challenge pairs and unguessable {@code state} values (RFC 7636). */
public final class PkceUtil {

  private static final SecureRandom RANDOM = new SecureRandom();
  private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

  private PkceUtil() {}

  /** Returns a 64-byte (URL-safe, unpadded) random string suitable as a PKCE {@code verifier}. */
  public static String generateCodeVerifier() {
    byte[] bytes = new byte[64];
    RANDOM.nextBytes(bytes);
    return URL_ENCODER.encodeToString(bytes);
  }

  /** Returns the S256 challenge for the given verifier: {@code BASE64URL(SHA256(verifier))}. */
  public static String codeChallengeS256(String verifier) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashed = digest.digest(verifier.getBytes(StandardCharsets.US_ASCII));
      return URL_ENCODER.encodeToString(hashed);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  /** Returns an unguessable random string suitable as an OAuth {@code state} value. */
  public static String generateState() {
    byte[] bytes = new byte[32];
    RANDOM.nextBytes(bytes);
    return URL_ENCODER.encodeToString(bytes);
  }
}
