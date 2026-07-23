package com.domeni.kapita.payment.e2e.support;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
@Profile("e2e")
public class JwtTokenFactory {

  private static final RSAPrivateKey PRIVATE_KEY = loadPrivateKey();
  private static final String E2E_USER_ID = "44a31cb2-bb38-4734-b8d8-9be15c7fb7b5";
  private final String audience;

  public JwtTokenFactory(@Value("${kapita.security.jwt.audience}") String audience) {
    this.audience = audience;
  }

  public String createToken(String... scopes) {
    Instant now = Instant.now();
    String scopeValue = String.join(" ", Arrays.asList(scopes));

    JWTClaimsSet claimsSet =
        new JWTClaimsSet.Builder()
            .issuer("http://auth-service.local")
            .subject(E2E_USER_ID)
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plusSeconds(3600)))
            .jwtID(UUID.randomUUID().toString())
            .audience(List.of(audience))
            .claim("scope", scopeValue)
            .build();

    SignedJWT signedJwt =
        new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).build(), claimsSet);

    try {
      signedJwt.sign(new RSASSASigner(PRIVATE_KEY));
      return signedJwt.serialize();
    } catch (JOSEException e) {
      throw new IllegalStateException("Unable to sign e2e jwt token", e);
    }
  }

  private static RSAPrivateKey loadPrivateKey() {
    try {
      String pem =
          new String(
                  new ClassPathResource("security/jwt-private.pem").getInputStream().readAllBytes(),
                  StandardCharsets.UTF_8)
              .replace("-----BEGIN PRIVATE KEY-----", "")
              .replace("-----END PRIVATE KEY-----", "")
              .replaceAll("\\s", "");

      byte[] decodedPem = Base64.getDecoder().decode(pem);
      PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedPem);
      return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    } catch (Exception e) {
      throw new IllegalStateException("Unable to load RSA private key for e2e tests", e);
    }
  }
}
