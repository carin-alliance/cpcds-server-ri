package ca.uhn.fhir.jpa.starter.authorization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class RegisterEndpoint {

    public static ResponseEntity<String> handleRegisterClient(String redirectUri) {
        // Escape all the query parameters
        redirectUri = StringEscapeUtils.escapeJava(redirectUri);

        System.out.println("RegisterEndpoint::Register: /register/client");
        System.out.println("RegisterClient:RedirectURI:" + redirectUri);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String clientId = UUID.randomUUID().toString();
        String clientSecret = RandomStringUtils.randomAlphanumeric(256);
        Client newClient = new Client(clientId, clientSecret, redirectUri);

        if (OauthEndpointController.getDB().write(newClient))
            return new ResponseEntity<>(gson.toJson(newClient.toMap()), HttpStatus.CREATED);
        else
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    }
}
