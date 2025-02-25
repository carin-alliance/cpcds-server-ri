package ca.uhn.fhir.jpa.starter;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseConformance;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestSecurityComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.RestfulCapabilityMode;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.jpa.starter.authorization.AuthUtils;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import jakarta.servlet.http.HttpServletRequest;

@Interceptor
public class Metadata {

    private AppProperties appProperties;

    public Metadata(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Hook(Pointcut.SERVER_CAPABILITY_STATEMENT_GENERATED)
    public void customize(IBaseConformance theCapabilityStatement, RequestDetails theRequestDetails, HttpServletRequest theServletRequest) {

        CapabilityStatement c = (CapabilityStatement) theCapabilityStatement;

        if (theServletRequest == null) {
            try {
                theServletRequest = ((ServletRequestDetails) theRequestDetails).getServletRequest();
            } catch (ClassCastException e) {
                return;
            }
        }

        Extension oauthExtension = new Extension();
        ArrayList<Extension> uris = new ArrayList<>();
        uris.add(new Extension("token", new UriType(AuthUtils.getOauthTokenUrl(theServletRequest, appProperties))));
        uris.add(new Extension("authorize", new UriType(AuthUtils.getOauthAuthorizationUrl(theServletRequest, appProperties))));
        uris.add(new Extension("introspect", new UriType(AuthUtils.getOauthIntrospectionUrl(theServletRequest, appProperties))));
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

    }
}