package ca.uhn.fhir.jpa.starter.authorization;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import ca.uhn.fhir.jpa.starter.ServerLogger;

public class TokenEndpoint {

    private static final String ERROR_KEY = "error";
    private static final String ERROR_DESCRIPTION_KEY = "error_description";

    private static final Logger logger = ServerLogger.getLogger();

    /**
     * Enum for types of tokens
     */
    public enum TokenType {
        REFRESH, ACCESS;
    }

    public static ResponseEntity<String> handleTokenRequest(HttpServletRequest request, String grantType, String token,
            String redirectURI) {
        // Set the headers for the response
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add(HttpHeaders.CACHE_CONTROL, "no-store");
        headers.add(HttpHeaders.PRAGMA, "no-store");

        HashMap<String, String> response = new HashMap<>();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String baseUrl = AuthUtils.getFhirBaseUrl(request);

        // Validate the client is authorized
        String clientId = AuthUtils.clientIsAuthorized(request);
        if (clientId == null) {
            response.put(ERROR_KEY, "invalid_client");
            response.put(ERROR_DESCRIPTION_KEY,
                    "Authorization header is missing, malformed, or client_id/client_secret is invalid");
            return new ResponseEntity<>(gson.toJson(response), headers, HttpStatus.UNAUTHORIZED);
        }

        // Validate the grant_type is authorization_code or refresh_token
        String patientId = null;
        if (grantType.equals("authorization_code")) {
            // Request is to trade authorization_code for access token
            patientId = AuthUtils.authCodeIsValid(token, baseUrl, redirectURI, clientId);
        } else if (grantType.equals("refresh_token")) {
            // Request is to trade refresh_token for access token
            patientId = AuthUtils.refreshTokenIsValid(token, baseUrl, clientId);
        } else {
            response.put(ERROR_KEY, "invalid_request");
            response.put(ERROR_DESCRIPTION_KEY, "grant_type must be authorization_code not " + grantType);
            return new ResponseEntity<>(gson.toJson(response), headers, HttpStatus.BAD_REQUEST);
        }

        logger.fine("TokenEndpoint::Token:Patient:" + patientId);
        if (patientId != null) {
            String accessToken = AuthUtils.generateToken(token, baseUrl, clientId, patientId, UUID.randomUUID().toString(),
                    TokenType.ACCESS, request);
            logger.fine("TokenEndpoint::Token:Generated token " + accessToken);
            if (accessToken != null) {
                String jwtId = UUID.randomUUID().toString();
                response.put("access_token", accessToken);
                response.put("token_type", "bearer");
                response.put("expires_in", "3600");
                response.put("patient", patientId);
                response.put("scope", "patient/*.read");
                response.put("refresh_token",
                        AuthUtils.generateToken(token, baseUrl, clientId, patientId, jwtId, TokenType.REFRESH, request));
                OauthEndpointController.getDB().setRefreshTokenId(patientId, jwtId);
                return new ResponseEntity<>(gson.toJson(response), headers, HttpStatus.OK);
            } else {
                response.put(ERROR_KEY, "invalid_request");
                response.put(ERROR_DESCRIPTION_KEY, "Internal server error. Please try again");
                return new ResponseEntity<>(gson.toJson(response), headers, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            response.put(ERROR_KEY, "invalid_grant");
            response.put(ERROR_DESCRIPTION_KEY, "Unable to verify. Please make sure the code/token is still valid");
            return new ResponseEntity<>(gson.toJson(response), headers, HttpStatus.BAD_REQUEST);
        }
    }
    
}
