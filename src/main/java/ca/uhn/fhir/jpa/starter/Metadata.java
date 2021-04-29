package ca.uhn.fhir.jpa.starter;

import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.r4.hapi.rest.server.ServerCapabilityStatementProvider;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.Enumeration;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementImplementationComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestSecurityComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementSoftwareComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.ResourceInteractionComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.RestfulCapabilityMode;
import org.hl7.fhir.r4.model.CapabilityStatement.TypeRestfulInteraction;
import org.hl7.fhir.r4.model.CapabilityStatement.TypeRestfulInteractionEnumFactory;

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

        TypeRestfulInteractionEnumFactory f = new TypeRestfulInteractionEnumFactory();
        ResourceInteractionComponent readInteractionComponent = new ResourceInteractionComponent(new Enumeration<>(f, TypeRestfulInteraction.READ));
        ResourceInteractionComponent vreadInteractionComponent = new ResourceInteractionComponent(new Enumeration<>(f, TypeRestfulInteraction.VREAD));
        ResourceInteractionComponent searchTypeInteractionComponent = new ResourceInteractionComponent(new Enumeration<>(f, TypeRestfulInteraction.SEARCHTYPE));

        CapabilityStatementRestResourceComponent coverageResourceComponent = new CapabilityStatementRestResourceComponent();
        coverageResourceComponent.setType("Coverage");
        coverageResourceComponent.addSupportedProfile("http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-Coverage");
        coverageResourceComponent.addSearchInclude("Coverage:payor");
        coverageResourceComponent.addInteraction(readInteractionComponent);

        CapabilityStatementRestResourceComponent explanationOfBenefitResourceComponent = new CapabilityStatementRestResourceComponent();
        explanationOfBenefitResourceComponent.setType("ExplanationOfBenefit");
        explanationOfBenefitResourceComponent.addSupportedProfile("http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-Explanation-of-Benefit");
        explanationOfBenefitResourceComponent.addSupportedProfile("http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-ExplanationOfBenefit-Inpatient-Institutional");
        explanationOfBenefitResourceComponent.addSupportedProfile("http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-ExplanationOfBenefit-Outpatient-Institutional");
        explanationOfBenefitResourceComponent.addSupportedProfile("http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-ExplanationOfBenefit-Pharmacy");
        explanationOfBenefitResourceComponent.addSupportedProfile("http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-ExplanationOfBenefit-Professional-NonClinician");
        explanationOfBenefitResourceComponent.addSearchInclude("ExplanationOfBenefit:patient");
        explanationOfBenefitResourceComponent.addSearchInclude("ExplanationOfBenefit:provider");
        explanationOfBenefitResourceComponent.addSearchInclude("ExplanationOfBenefit:carre-team");
        explanationOfBenefitResourceComponent.addSearchInclude("ExplanationOfBenefit:coverage");
        explanationOfBenefitResourceComponent.addSearchInclude("ExplanationOfBenefit:insurer");
        explanationOfBenefitResourceComponent.addSearchInclude("ExplanationOfBenefit:*");
        explanationOfBenefitResourceComponent.addInteraction(readInteractionComponent);
        explanationOfBenefitResourceComponent.addInteraction(searchTypeInteractionComponent);

        CapabilityStatementRestResourceSearchParamComponent idSearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
        idSearchParamComponent.setDefinition("http://hl7.org/fhir/SearchParameter/Resource-id");
        idSearchParamComponent.setName("_id");
        explanationOfBenefitResourceComponent.addSearchParam(idSearchParamComponent);
        CapabilityStatementRestResourceSearchParamComponent patientSearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
        patientSearchParamComponent.setDefinition("http://hl7.org/fhir/us/carin-bb/SearchParameter/explanationofbenefit-patient");
        patientSearchParamComponent.setName("patient");
        explanationOfBenefitResourceComponent.addSearchParam(patientSearchParamComponent);
        CapabilityStatementRestResourceSearchParamComponent lastUpdatedSearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
        lastUpdatedSearchParamComponent.setDefinition("http://hl7.org/fhir/SearchParameter/Resource-lastUpdated");
        lastUpdatedSearchParamComponent.setName("_lastUpdated");
        explanationOfBenefitResourceComponent.addSearchParam(lastUpdatedSearchParamComponent);
        CapabilityStatementRestResourceSearchParamComponent typeSearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
        typeSearchParamComponent.setDefinition("http://hl7.org/fhir/us/carin-bb/SearchParameter/explanationofbenefit-type");
        typeSearchParamComponent.setName("type");
        explanationOfBenefitResourceComponent.addSearchParam(typeSearchParamComponent);
        CapabilityStatementRestResourceSearchParamComponent identifierSearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
        identifierSearchParamComponent.setDefinition("http://hl7.org/fhir/us/carin-bb/SearchParameter/explanationofbenefit-identifier");
        identifierSearchParamComponent.setName("identifier");
        explanationOfBenefitResourceComponent.addSearchParam(identifierSearchParamComponent);
        CapabilityStatementRestResourceSearchParamComponent serviceDateSearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
        serviceDateSearchParamComponent.setDefinition("http://hl7.org/fhir/us/carin-bb/SearchParameter/explanationofbenefit-service-date");
        serviceDateSearchParamComponent.setName("service-date");
        explanationOfBenefitResourceComponent.addSearchParam(serviceDateSearchParamComponent);

        CapabilityStatementRestResourceComponent organizationResourceComponent = new CapabilityStatementRestResourceComponent();
        organizationResourceComponent.setType("Organization");
        organizationResourceComponent.addSupportedProfile("http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-Organization");
        organizationResourceComponent.addInteraction(readInteractionComponent);
        organizationResourceComponent.addInteraction(vreadInteractionComponent);

        CapabilityStatementRestResourceComponent patientResourceComponent = new CapabilityStatementRestResourceComponent();
        patientResourceComponent.setType("Patient");
        patientResourceComponent.addSupportedProfile("http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-Patient");
        patientResourceComponent.addInteraction(readInteractionComponent);
        patientResourceComponent.addInteraction(vreadInteractionComponent);

        CapabilityStatementRestResourceComponent practitionerResourceComponent = new CapabilityStatementRestResourceComponent();
        practitionerResourceComponent.setType("Practitioner");
        practitionerResourceComponent.addSupportedProfile("http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-Practitioner");
        practitionerResourceComponent.addInteraction(readInteractionComponent);
        practitionerResourceComponent.addInteraction(vreadInteractionComponent);


        // Create new rest component
        CapabilityStatementRestComponent rest = new CapabilityStatementRestComponent();
        rest = new CapabilityStatementRestComponent();
        rest.setMode(RestfulCapabilityMode.SERVER);
        rest.setSecurity(securityComponent);
        rest.addResource(coverageResourceComponent);
        rest.addResource(explanationOfBenefitResourceComponent);
        rest.addResource(organizationResourceComponent);
        rest.addResource(patientResourceComponent);
        rest.addResource(practitionerResourceComponent);
        c.setRest(Collections.singletonList(rest));

        c.addImplementationGuide("http://hl7.org/fhir/us/carin-bb/ImplementationGuide/hl7.fhir.us.carin-bb");

        CapabilityStatementImplementationComponent implementationComponent = new CapabilityStatementImplementationComponent(new StringType("MITRE CPCDS Reference Implementation for Carin BB STU2"));
        implementationComponent.setUrl(HapiProperties.getServerAddress());
        c.setImplementation(implementationComponent);
        CapabilityStatementSoftwareComponent softwareComponent = new CapabilityStatementSoftwareComponent(new StringType("MITRE CPCDS RI"));
        c.setSoftware(softwareComponent);

        return c;
    }
}