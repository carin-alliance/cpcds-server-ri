package ca.uhn.fhir.jpa.starter.authorization;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import org.springframework.security.crypto.bcrypt.BCrypt;

import ca.uhn.fhir.jpa.starter.HapiProperties;
import ca.uhn.fhir.jpa.starter.ServerLogger;
import ca.uhn.fhir.jpa.starter.authorization.TokenEndpoint.TokenType;

public class AuthUtils {

    private static final String CLIENT_ID_KEY = "client_id";
    private static final String REDIRECT_URI_KEY = "redirect_uri";

    private static final Logger logger = ServerLogger.getLogger();

    public static void initializeDB() {
        // Add default Clients and Users
		Client heroku = new Client("6cfecf41-e364-44ab-a06f-77f8b0c56c2b", "XHNdbHQlOrWXQ8eeXHvZal1EDjI3n2ISlqhtP30Zc89Ad2NuzreoorWQ5P8dPrxtk267SJ23mbxlMzjriAGgkaTnm6Y9f1cOas4Z6xhWXxG43bkIKHhawMR6gGDXAuEWc8wXUHteZIi4YCX6E1qAvGdsXS1KBhkUf1CLcGmauhbCMd73CjMugT527mpLnIebuTp4LYDiJag0usCE6B6fYuTWV21AbvydLnLsMsk83T7aobE4p9R0upL2Ph3OFTE1", "https://cpcds-client-ri.herokuapp.com/login");
		Client localhost = new Client("b0c46635-c0b4-448c-a8b9-9bd282d2e05a", "bUYbEj5wpazS8Xv1jyruFKpuXa24OGn9MHuZ3ygKexaI5mhKUIzVEBvbv2uggVf1cW6kYD3cgTbCIGK3kjiMcmJq3OG9bn85Fh2x7JKYgy7Jwagdzs0qufgkhPGDvEoVpImpA4clIhfwn58qoTrfHx86ooWLWJeQh4s0StEMqoxLqboywr8u11qmMHd1xwBLehGXUbqpEBlkelBHDWaiCjkhwZeRe4nVu4o8wSAbPQIECQcTjqYBUrBjHlMx5vXU", "http://localhost:4000/login");
		User patient1 = new User("Patient1", BCrypt.hashpw("password", BCrypt.gensalt()), "Patient1");
		User examplePatient1 = new User("ExamplePatient1", BCrypt.hashpw("password", BCrypt.gensalt()), "ExamplePatient1");
		User pdexPatient1 = new User("PDexPatient1", BCrypt.hashpw("password", BCrypt.gensalt()), "PDexPatient1");
        User admin = new User("admin", BCrypt.hashpw("password", BCrypt.gensalt()), "admin");

		OauthEndpointController.getDB().write(heroku);
		OauthEndpointController.getDB().write(localhost);
		OauthEndpointController.getDB().write(patient1);
        OauthEndpointController.getDB().write(admin);
		OauthEndpointController.getDB().write(examplePatient1);
		OauthEndpointController.getDB().write(pdexPatient1);
    }

    /**
     * Get the fhir base url from the HttpServletRequest
     * Ex: http://localhost:8080/cpcds-server/fhir
     * 
     * @param request - the HttpServletRequest from the controller
     * @return the fhir base url for the service
     */
    public static String getFhirBaseUrl() {
        String baseUrl = HapiProperties.getServerAddress();
        if (baseUrl.endsWith("/")) 
            return baseUrl.substring(0, baseUrl.length() - 1);
        else   
            return baseUrl;
    }

    /**
     * Generate the Authorization code for the client with a 2 minute expiration
     * time
     * 
     * @param baseUrl     - the baseUrl for this service
     * @param clientId    - the client_id received in the GET request
     * @param redirectURI - the redirect_uri received in the GET request
     * @param username    - the user's log in username
     * @return signed JWT token for the authorization code
     */
    public static String generateAuthorizationCode(String baseUrl, String clientId, String redirectURI, String username) {
        try {
            Algorithm algorithm = Algorithm.RSA256(OauthEndpointController.getPublicKey(), OauthEndpointController.getPrivateKey());
            Instant twoMinutes = LocalDateTime.now().plusMinutes(2).atZone(ZoneId.systemDefault()).toInstant();
            return JWT.create().withIssuer(baseUrl).withExpiresAt(Date.from(twoMinutes)).withIssuedAt(new Date())
                .withAudience(baseUrl).withClaim(CLIENT_ID_KEY, clientId).withClaim(REDIRECT_URI_KEY, redirectURI)
                .withClaim("username", username).sign(algorithm);
        } catch (JWTCreationException e) {
            // Invalid Signing configuration / Couldn't convert Claims.
            logger.log(Level.SEVERE, "AuthorizationEndpoint::generateAuthorizationCode:Unable to generate code for " + clientId, e);
            return null;
        }
    }

    /**
     * Simple method to produce the redirect uri from the attributes
     * 
     * @param redirectURI - the base redirect uri
     * @param attributes  - the attributes to add to the base redirect uri
     * @return formatted redirect uri
     */
    public static String getRedirect(String redirectURI, Map<String, String> attributes) {
        if (attributes.size() > 0) {
            redirectURI += "?";

            int i = 1;
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                redirectURI += entry.getKey() + "=" + entry.getValue();

                if (i != attributes.size())
                    redirectURI += "&";

                i++;
            }
        }

        return redirectURI;
    }

    /**
     * Determine if the client is authorized based on the Basic Authorization
     * header. Currently accepts all clients
     * 
     * @param request - the current request
     * @return the clientId from the authorization header
     */
    public static String clientIsAuthorized(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        logger.log(Level.FINE, "TokenEndpoint::AuthHeader:" + authHeader);
        if (authHeader != null) {
            String regex = "Basic (.*)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(authHeader);
            if (matcher.find() && matcher.groupCount() == 1) {
                String clientAuthorization = new String(Base64.getDecoder().decode(matcher.group(1)));
                String clientAuthRegex = "(.*):(.*)";
                Pattern clientAuthPattern = Pattern.compile(clientAuthRegex);
                Matcher clientAuthMatcher = clientAuthPattern.matcher(clientAuthorization);
                if (clientAuthMatcher.find() && clientAuthMatcher.groupCount() == 2) {
                    String clientId = clientAuthMatcher.group(1);
                    String clientSecret = clientAuthMatcher.group(2);
                    logger.log(Level.FINE, "TokenEndpoint::client:" + clientId + "(" + clientSecret + ")");
                    if (Client.getClient(clientId).validateSecret(clientSecret)) {
                        logger.info("TokenEndpoint::clientIsAuthorized:" + clientId);
                        return clientId;
                    }
                }
            }
        }
        logger.info("TokenEndpoint::clientIsAuthorized:false");
        return null;
    }

    /**
     * Generate an access (or request) token for the user with the correct claims.
     * Access token is valid for 1 hour
     * 
     * @param code      - the authorization code from the POST request
     * @param baseUrl   - the base url of this service
     * @param clientId  - the id of the requesting client
     * @param patientId - the user's patient ID
     * @param jwtId     - the unique id for this token
     * @param tokenType - the type of token to generate
     * @return access token for granted user or null
     */
    public static String generateToken(String code, String baseUrl, String clientId, String patientId, String jwtId,
            TokenType tokenType, HttpServletRequest request) {
        try {
            // Create the access token JWT
            Algorithm algorithm = Algorithm.RSA256(OauthEndpointController.getPublicKey(), OauthEndpointController.getPrivateKey());
            Instant exp;
            if (patientId.equals("admin")) exp = LocalDateTime.now().plusDays(2000).atZone(ZoneId.systemDefault()).toInstant();
            else if (tokenType == TokenType.ACCESS) exp = LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant();
            else exp = LocalDateTime.now().plusDays(30).atZone(ZoneId.systemDefault()).toInstant();
            return JWT.create().withKeyId(OauthEndpointController.getKeyId()).withIssuer(baseUrl).withExpiresAt(Date.from(exp))
                    .withIssuedAt(new Date()).withAudience(baseUrl).withClaim(CLIENT_ID_KEY, clientId)
                    .withClaim("patient_id", patientId).withJWTId(jwtId).sign(algorithm);
        } catch (JWTCreationException e) {
            // Invalid Signing configuration / Couldn't convert Claims.
            logger.log(Level.SEVERE, "TokenEndpoint::generateToken:Unable to generate token", e);
            return null;
        } catch (JWTVerificationException e) {
            // Invalid code
            logger.log(Level.SEVERE, "TokenEndpoint::generateToken:Unable to verify code", e);
            return null;
        }
    }

    /**
     * Validate/verify the authorization code is valid
     * 
     * @param code        - the authorization code
     * @param baseUrl     - the base URL of this service
     * @param redirectURI - the redirect_uri provided in the POST request
     * @return patientId if the authorization code is valid and null otherwise
     */
    public static String authCodeIsValid(String code, String baseUrl, String redirectURI, String clientId) {
        String patientId = null;
        try {
            Algorithm algorithm = Algorithm.RSA256(OauthEndpointController.getPublicKey(), null);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(baseUrl).withAudience(baseUrl)
                    .withClaim(REDIRECT_URI_KEY, redirectURI).build();
            DecodedJWT jwt = verifier.verify(code);
            String jwtClientId = jwt.getClaim(CLIENT_ID_KEY).asString();
            if (!clientId.equals(jwtClientId)) {
                logger.warning(
                        "TokenEndpoint::Authorization code is invalid. Client ID does not match authorization header");
            } else {
                String username = jwt.getClaim("username").asString();
                patientId = User.getUser(username).getPatientId();
            }
        } catch (SignatureVerificationException e) {
            logger.log(Level.SEVERE, "TokenEndpoint::Authorization code is invalid. Signature invalid", e);
        } catch (TokenExpiredException e) {
            logger.log(Level.SEVERE, "TokenEndpoint::Authorization code is invalid. Token expired", e);
        } catch (JWTVerificationException e) {
            logger.log(Level.SEVERE, "TokenEndpoint::Authorization code is invalid. Please obtain a new code", e);
        }
        return patientId;
    }

    /**
     * Validate/verify the refresh token is valid
     * 
     * @param token   - the refresg token
     * @param baseUrl - the base URL of this service
     * @return patientId if the refresh token is valid and null otherwise
     */
    public static String refreshTokenIsValid(String code, String baseUrl, String clientId) {
        String patientId = null;
        try {
            Algorithm algorithm = Algorithm.RSA256(OauthEndpointController.getPublicKey(), null);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(baseUrl).withAudience(baseUrl).build();
            DecodedJWT jwt = verifier.verify(code);
            String jwtId = jwt.getId();
            String jwtClientId = jwt.getClaim(CLIENT_ID_KEY).asString();
            if (!clientId.equals(jwtClientId)) {
                logger.warning(
                        "TokenEndpoint::Refresh token is invalid. Client ID does not match authorization header");
                return null;
            }

            patientId = jwt.getClaim("patient_id").asString();
            if (!jwtId.equals(OauthEndpointController.getDB().readRefreshToken(patientId))) {
                logger.warning("TokenEndpoint::Refresh token is invalid. Please reauthorize");
                return null;
            }
        } catch (JWTVerificationException e) {
            logger.log(Level.SEVERE, "TokenEndpoint::Refresh token is invalid. Please reauthorize", e);
        }
        return patientId;
    }

    /**
     * Get an array of all scopes supported
     * 
     * @return String[] of scopes supported
     */
    public static List<String> supportedScopes() {
        String[] scopes = { "patient/*.read", "user/*.read", "offline_access", "launch/patient", "openid", "fhirUser" };
        return Arrays.asList(scopes);
    }

    /**
     * Helper method to determine if the scope is supported
     * 
     * @param scope - the scope in question
     * @return true if the scope is in supportedScopes, false otherwise
     */
    public static boolean isSupportedScope(String scope) {
        return supportedScopes().contains(scope);
    }

    /**
     * Format the scopes list to string of form "a, b, ..., z"
     * 
     * @param scopes - the list of scopes to format
     * @return string in the form "a, b, ..., z"
     */
    public static String scopesToString(List<String> scopes) {
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < scopes.size(); i++) {
           str.append(scopes.get(i));
           if (i != scopes.size() - 1) str.append(", ");
        }

        return str.toString();
    }
}
