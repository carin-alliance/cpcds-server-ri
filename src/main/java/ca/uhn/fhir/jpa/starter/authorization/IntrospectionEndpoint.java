package ca.uhn.fhir.jpa.starter.authorization;

import javax.servlet.http.HttpServletRequest;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class IntrospectionEndpoint {

    public static ResponseEntity<String> handleIntrospection(HttpServletRequest request, String token) {
        JSONObject response = new JSONObject();

        String baseUrl = AuthUtils.getFhirBaseUrl(request);

        try {
            Algorithm algorithm = Algorithm.RSA256(OauthEndpointController.getPublicKey(), null);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(baseUrl).withAudience(baseUrl).build();
            DecodedJWT jwt = verifier.verify(token);
            response.put("active", true);
            response.put("aud", jwt.getAudience().get(0));
            response.put("iss", jwt.getIssuer());
            response.put("exp", jwt.getExpiresAt().getTime() / 1000); // Display in sec not ms
            response.put("iat", jwt.getIssuedAt().getTime() / 1000); // Display in sec not ms
            response.put("patient_id", jwt.getClaim("patient_id").asString());
        } catch (JWTVerificationException exception) {
            response.put("active", false);
        }

        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }
    
}
