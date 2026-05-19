package io.alpa.pgnparser.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class PkceUtilTest {

  @Test
  void generateCodeVerifier_producesUrlSafeStringInValidLengthRange() {
    String verifier = PkceUtil.generateCodeVerifier();
    assertThat(verifier).matches("[A-Za-z0-9_-]+");
    // RFC 7636: verifier length between 43 and 128 characters.
    assertThat(verifier.length()).isBetween(43, 128);
  }

  @Test
  void codeChallengeS256_equalsBase64UrlSha256OfVerifier() throws Exception {
    String verifier = "exampleVerifierForTest_1234567890_abcdefghij";

    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    String expected =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(digest.digest(verifier.getBytes(StandardCharsets.US_ASCII)));

    assertThat(PkceUtil.codeChallengeS256(verifier)).isEqualTo(expected);
  }

  @Test
  void generateState_returnsUnguessableUrlSafeString() {
    String state = PkceUtil.generateState();
    assertThat(state).matches("[A-Za-z0-9_-]+");
    assertThat(state.length()).isGreaterThanOrEqualTo(32);
    assertThat(state).isNotEqualTo(PkceUtil.generateState());
  }
}
