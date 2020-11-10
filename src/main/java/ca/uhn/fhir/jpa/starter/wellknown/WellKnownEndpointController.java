package ca.uhn.fhir.jpa.starter.wellknown;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.uhn.fhir.jpa.starter.Metadata;

@RestController
public class WellKnownEndpointController {

    private static final String WELL_KNOWN_AUTHORIZATION_ENDPOINT_KEY = "authorization_endpoint";
    private static final String WELL_KNOWN_TOKEN_ENDPOINT_KEY = "token_endpoint";
    private static final String WELL_KNOWN_REGISTRATION_ENDPOINT = "registration_endpoint";
    private static final String WELL_KNOWN_INTROSPECTION_ENDPOINT = "introspection_endpoint";
    private static final String WELL_KNOWN_SUPPORTED_AUTH_METHODS_KEY = "token_endpoint_auth_methods_supported";
    private static final String WELL_KNOWN_RESPONSE_TYPES_KEY = "response_types_supported";
    private static final String WELL_KNOWN_CAPABILITIES_KEY = "capabilities";

    // http://hl7.org/fhir/smart-app-launch/conformance/index.html#core-capabilities
    private static final String[] capabilityValues = { "launch-ehr", "launch-standalone" };
    private static final JSONArray WELL_KNOWN_CAPABILITIES_VALUES = new JSONArray(capabilityValues);

    // http://build.fhir.org/ig/HL7/carin-bb/Authorization_Authentication_and_Registration.html#authorization-and-authentication
    private static final String[] authMethodValues = { "client_secret_basic" };
    private static final JSONArray WELL_KNOWN_SUPPORTED_AUTH_METHODS_VALUES = new JSONArray(authMethodValues);

    private static final String[] responseTypeValues = { "code", "refresh_token" };
    private static final JSONArray WELL_KNOWN_RESPONSE_TYPE_VALUES = new JSONArray(responseTypeValues);

    @PostConstruct
    protected void postConstruct() {
        System.out.println("Well Known controller added.");
    }
    
    /**
     * Get request to support well-known endpoints for authorization metadata. See
     * http://www.hl7.org/fhir/smart-app-launch/conformance/index.html#using-well-known
     *
     * @return String representing json object of metadata returned at this url
     * @throws IOException when the request fails
     */
    @GetMapping(path = "/smart-configuration", produces = {"application/json"})
    public String getWellKnownJson(HttpServletRequest theRequest) {

        JSONObject wellKnownJson = new JSONObject();
        wellKnownJson.put(WELL_KNOWN_AUTHORIZATION_ENDPOINT_KEY, Metadata.getOauthAuthorizationUrl(theRequest));
        wellKnownJson.put(WELL_KNOWN_TOKEN_ENDPOINT_KEY, Metadata.getOauthTokenUrl(theRequest));
        wellKnownJson.put(WELL_KNOWN_REGISTRATION_ENDPOINT, Metadata.getOauthRegisterUrl(theRequest));
        wellKnownJson.put(WELL_KNOWN_INTROSPECTION_ENDPOINT, Metadata.getOauthIntrospectionUrl(theRequest));
        wellKnownJson.put(WELL_KNOWN_SUPPORTED_AUTH_METHODS_KEY, WELL_KNOWN_SUPPORTED_AUTH_METHODS_VALUES);
        wellKnownJson.put(WELL_KNOWN_RESPONSE_TYPES_KEY, WELL_KNOWN_RESPONSE_TYPE_VALUES);
        wellKnownJson.put(WELL_KNOWN_CAPABILITIES_KEY, WELL_KNOWN_CAPABILITIES_VALUES);

        return wellKnownJson.toString();
    }
}
