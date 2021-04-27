package ca.uhn.fhir.jpa.starter;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.r4.hapi.rest.server.ServerCapabilityStatementProvider;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestSecurityComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.RestfulCapabilityMode;

import ca.uhn.fhir.rest.api.server.RequestDetails;

public class Metadata extends ServerCapabilityStatementProvider {

    public static String getOauthTokenUrl() {
        return HapiProperties.getServerAddress() + "oauth/token"; 
    }

    public static String getOauthAuthorizationUrl() {
        return HapiProperties.getServerAddress() + "oauth/authorization"; 
    }

    public static String getOauthIntrospectionUrl() {
        return HapiProperties.getServerAddress() + "oauth/introspect"; 
    }

    public static String getOauthRegisterUrl() {
        return HapiProperties.getServerAddress() + "oauth/register/client"; 
    }

    @Override
    public CapabilityStatement getServerConformance(HttpServletRequest request, RequestDetails requestDetails) {

        CapabilityStatement c = super.getServerConformance(request, requestDetails);

        Extension oauthExtension = new Extension();
        ArrayList<Extension> uris = new ArrayList<>();
        uris.add(new Extension("token", new UriType(getOauthTokenUrl())));
        uris.add(new Extension("authorize", new UriType(getOauthAuthorizationUrl())));
        uris.add(new Extension("introspect", new UriType(getOauthIntrospectionUrl())));
        oauthExtension.setUrl("http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris");
        oauthExtension.setExtension(uris);

        CapabilityStatementRestSecurityComponent securityComponent = new CapabilityStatementRestSecurityComponent();
        securityComponent.addExtension(oauthExtension);

        // Get the CapabilityStatementRestComponent for the server if one exists
        List<CapabilityStatementRestComponent> restComponents = c.getRest();
        CapabilityStatementRestComponent rest = null;
        for (CapabilityStatementRestComponent rc : restComponents) {
            if (rc.getMode().equals(RestfulCapabilityMode.SERVER)) {
                rest = rc;
            }
        }

        if (rest == null) {
            // Create new rest component
            rest = new CapabilityStatementRestComponent();
            rest.setMode(RestfulCapabilityMode.SERVER);
            rest.setSecurity(securityComponent);
            c.addRest(rest);
        } else
            rest.setSecurity(securityComponent);

        return c;
    }
}