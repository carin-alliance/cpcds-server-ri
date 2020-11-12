package ca.uhn.fhir.jpa.starter.authorization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;

import ca.uhn.fhir.jpa.starter.ServerLogger;

public class AuthorizationEndpoint {

    private static final String ERROR_KEY = "error";
    private static final String ERROR_DESCRIPTION_KEY = "error_description";

    private static final Logger logger = ServerLogger.getLogger();

    public static String handleAuthorizationGet() {
        try {
            return new String(Files.readAllBytes(Paths.get("src/main/resources/templates/login.html")));
        } catch (IOException e) {
            return "Error: Not Found";
        }
    }

    public static ResponseEntity<String> handleAuthorizationPost(HttpServletRequest request, HttpEntity<String> entity, 
        String aud, String state, String clientId, String redirectURI, String responseType) {
        final String baseUrl = AuthUtils.getFhirBaseUrl(request);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        HashMap<String, String> attributes = new HashMap<>();

        HttpStatus status = HttpStatus.OK;
        if (!aud.equals(baseUrl)) {
            status = HttpStatus.BAD_REQUEST;
            attributes.put(ERROR_KEY, "invalid_request");
            attributes.put(ERROR_DESCRIPTION_KEY, "aud is invalid");
        } else if (!responseType.equals("code")) {
            status = HttpStatus.BAD_REQUEST;
            attributes.put(ERROR_KEY, "invalid_request");
            attributes.put(ERROR_DESCRIPTION_KEY, "response_type must be code");
        } else if (Client.getClient(clientId) == null) {
            status = HttpStatus.BAD_REQUEST;
            attributes.put(ERROR_KEY, "unauthorized_client");
            attributes.put(ERROR_DESCRIPTION_KEY, "client is not registered");
        } else {
            User userRequest = gson.fromJson(entity.getBody(), User.class);
            logger.info("AuthorizationEndpoint::handleAuthorizationPost:Received login request from " + userRequest.getUsername());
            User user = OauthEndpointController.getDB().readUser(userRequest.getUsername());
            if (user == null) {
                status = HttpStatus.BAD_REQUEST;
                attributes.put(ERROR_KEY, "access_denied");
                attributes.put(ERROR_DESCRIPTION_KEY, "user does not exist");
            } else if (BCrypt.checkpw(userRequest.getPassword(), user.getPassword())) {
                logger.info("AuthorizationEndpoint::User " + user.getUsername() + " is authorized");

                String code = AuthUtils.generateAuthorizationCode(baseUrl, clientId, redirectURI, user.getUsername());
                logger.info("AuthorizationEndpoint::Generated code " + code);
                if (code == null) {
                    status = HttpStatus.INTERNAL_SERVER_ERROR;
                    attributes.put(ERROR_KEY, "server_error");
                } else {
                    attributes.put("code", code);
                    attributes.put("state", state);
                }
            } else {
                status = HttpStatus.UNAUTHORIZED;
                attributes.put(ERROR_KEY, "access_denied");
                attributes.put(ERROR_DESCRIPTION_KEY, "invalid username/password");
                logger.severe("AuthorizationEndpoint::Authorization:Failed loging attempt from " + user.getUsername());
            }
        }

        redirectURI = AuthUtils.getRedirect(redirectURI, attributes);
        logger.info("Redirecting to " + redirectURI);
        return new ResponseEntity<>(gson.toJson(Collections.singletonMap("redirect", redirectURI)), status);
    }
    
}
