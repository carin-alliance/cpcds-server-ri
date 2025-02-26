package ca.uhn.fhir.jpa.starter.authorization;

import ca.uhn.fhir.jpa.starter.ServerLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;
import java.util.logging.Logger;

public class RegisterEndpoint {

	private static final Logger logger = ServerLogger.getLogger();

	public static ResponseEntity<String> handleRegisterClient(String redirectUri) {
		// Escape all the query parameters
		redirectUri = StringEscapeUtils.escapeJava(redirectUri);

		logger.info("RegisterEndpoint::Register: /register/client");
		logger.fine("RegisterClient:RedirectURI:" + redirectUri);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		String clientId = UUID.randomUUID().toString();
		String clientSecret = RandomStringUtils.randomAlphanumeric(256);
		Client newClient = new Client(clientId, clientSecret, redirectUri);

		if (OauthEndpointController.getDB().write(newClient))
			return new ResponseEntity<>(gson.toJson(newClient.toMap()), HttpStatus.CREATED);
		else return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
}
