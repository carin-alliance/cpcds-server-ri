package ca.uhn.fhir.jpa.starter.debug;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.uhn.fhir.jpa.starter.ServerLogger;
import ca.uhn.fhir.jpa.starter.authorization.Client;
import ca.uhn.fhir.jpa.starter.authorization.OauthEndpointController;
import ca.uhn.fhir.jpa.starter.authorization.Database.Table;

@RestController
public class DebugEndpointController {

    private static final Logger logger = ServerLogger.getLogger();

    @GetMapping(value = "/Clients")
    public String getClients() {
        return OauthEndpointController.getDB().generateAndRunQuery(Table.CLIENTS);
    }

    @GetMapping(value = "/Users")
    public String getUsers() {
        return OauthEndpointController.getDB().generateAndRunQuery(Table.USERS);
    }

    @GetMapping(value = "/Log")
    public String getLog() {
        try {
            return new String(Files.readAllBytes(Paths.get(ServerLogger.getLogPath())));
        } catch (IOException e) {
            return "ERROR: Unable to read logfile";
        }
    }

    @PutMapping(value = "/UpdateClient", params = { "client_id" })
    public ResponseEntity<String> updateClient(HttpServletRequest request, HttpEntity<String> entity, @RequestParam(name = "client_id") String clientId) {
        String body = entity.getBody();
        clientId = StringEscapeUtils.escapeJava(clientId);
        logger.info("DebugEndpoint::UpdateClient:" + clientId + "\n" + body); 

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Client client = gson.fromJson(entity.getBody(), Client.class);

        if (OauthEndpointController.getDB().readClient(client.getId()) != null) {
            if (OauthEndpointController.getDB().updateClient(client))
                return new ResponseEntity<>(HttpStatus.OK);
            else
                return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        } else {
            OauthEndpointController.getDB().write(client);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

}
