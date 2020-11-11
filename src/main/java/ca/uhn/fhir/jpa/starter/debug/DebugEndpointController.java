package ca.uhn.fhir.jpa.starter.debug;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.uhn.fhir.jpa.starter.ServerLogger;
import ca.uhn.fhir.jpa.starter.authorization.OauthEndpointController;
import ca.uhn.fhir.jpa.starter.authorization.Database.Table;

@RestController
public class DebugEndpointController {

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

}
