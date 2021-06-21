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

        // Create Interaction Components
        TypeRestfulInteractionEnumFactory f = new TypeRestfulInteractionEnumFactory();
        ResourceInteractionComponent readInteractionComponent = new ResourceInteractionComponent(new Enumeration<>(f, TypeRestfulInteraction.READ));
        ResourceInteractionComponent vreadInteractionComponent = new ResourceInteractionComponent(new Enumeration<>(f, TypeRestfulInteraction.VREAD));
        ResourceInteractionComponent searchTypeInteractionComponent = new ResourceInteractionComponent(new Enumeration<>(f, TypeRestfulInteraction.SEARCHTYPE));

        // Create Search Param Components
        CapabilityStatementRestResourceSearchParamComponent idSearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
        idSearchParamComponent.setDefinition("http://hl7.org/fhir/SearchParameter/Resource-id");
        idSearchParamComponent.setName("_id");
        CapabilityStatementRestResourceSearchParamComponent lastUpdatedSearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
        lastUpdatedSearchParamComponent.setDefinition("http://hl7.org/fhir/SearchParameter/Resource-lastUpdated");
        lastUpdatedSearchParamComponent.setName("_lastUpdated");
        CapabilityStatementRestResourceSearchParamComponent eobPatientSearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
        eobPatientSearchParamComponent.setDefinition("http://hl7.org/fhir/us/carin-bb/SearchParameter/explanationofbenefit-patient");
        eobPatientSearchParamComponent.setName("patient");
        CapabilityStatementRestResourceSearchParamComponent eobTypeSearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
        eobTypeSearchParamComponent.setDefinition("http://hl7.org/fhir/us/carin-bb/SearchParameter/explanationofbenefit-type");
        eobTypeSearchParamComponent.setName("type");
        CapabilityStatementRestResourceSearchParamComponent eobIdentifierSearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
        eobIdentifierSearchParamComponent.setDefinition("http://hl7.org/fhir/us/carin-bb/SearchParameter/explanationofbenefit-identifier");
        eobIdentifierSearchParamComponent.setName("identifier");
        CapabilityStatementRestResourceSearchParamComponent eobServiceDateSearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
        eobServiceDateSearchParamComponent.setDefinition("http://hl7.org/fhir/us/carin-bb/SearchParameter/explanationofbenefit-service-date");
        eobServiceDateSearchParamComponent.setName("service-date");
        CapabilityStatementRestResourceSearchParamComponent patientSearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
        patientSearchParamComponent.setName("patient");
        CapabilityStatementRestResourceSearchParamComponent beneficiarySearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
        beneficiarySearchParamComponent.setName("beneficiary");
        CapabilityStatementRestResourceSearchParamComponent subscriberSearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
        subscriberSearchParamComponent.setName("subscriber");

        // Create Coverage Resource component
        CapabilityStatementRestResourceComponent coverageResourceComponent = new CapabilityStatementRestResourceComponent();
        coverageResourceComponent.setType("Coverage");
        coverageResourceComponent.addSupportedProfile("http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-Coverage");
        coverageResourceComponent.addSupportedProfile("http://hl7.org/fhir/us/davinci-pdex/StructureDefinition/hrex-coverage");
        coverageResourceComponent.addSearchInclude("Coverage:payor");
        coverageResourceComponent.addInteraction(readInteractionComponent);
        coverageResourceComponent.addSearchParam(patientSearchParamComponent);
        coverageResourceComponent.addSearchParam(beneficiarySearchParamComponent);
        coverageResourceComponent.addSearchParam(subscriberSearchParamComponent);

        // Create EOB Resource component
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
        explanationOfBenefitResourceComponent.addSearchParam(idSearchParamComponent);
        explanationOfBenefitResourceComponent.addSearchParam(eobPatientSearchParamComponent);
        explanationOfBenefitResourceComponent.addSearchParam(lastUpdatedSearchParamComponent);
        explanationOfBenefitResourceComponent.addSearchParam(eobTypeSearchParamComponent);
        explanationOfBenefitResourceComponent.addSearchParam(eobIdentifierSearchParamComponent);
        explanationOfBenefitResourceComponent.addSearchParam(eobServiceDateSearchParamComponent);

        // Create Organization Resource Component
        CapabilityStatementRestResourceComponent organizationResourceComponent = new CapabilityStatementRestResourceComponent();
        organizationResourceComponent.setType("Organization");
        organizationResourceComponent.addSupportedProfile("http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-Organization");
        organizationResourceComponent.addSupportedProfile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-organization");
        organizationResourceComponent.addInteraction(readInteractionComponent);
        organizationResourceComponent.addInteraction(vreadInteractionComponent);

        // Create Patient Resource Component
        CapabilityStatementRestResourceComponent patientResourceComponent = new CapabilityStatementRestResourceComponent();
        patientResourceComponent.setType("Patient");
        patientResourceComponent.addSupportedProfile("http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-Patient");
        patientResourceComponent.addSupportedProfile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient");
        patientResourceComponent.addInteraction(readInteractionComponent);
        patientResourceComponent.addInteraction(vreadInteractionComponent);

        // Create Practitioner Resource Component
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

        CapabilityStatementImplementationComponent implementationComponent = new CapabilityStatementImplementationComponent(new StringType("MITRE CPCDS Reference Implementation for Carin BB 1.1.0"));
        implementationComponent.setUrl(HapiProperties.getServerAddress());
        c.setImplementation(implementationComponent);
        CapabilityStatementSoftwareComponent softwareComponent = new CapabilityStatementSoftwareComponent(new StringType("MITRE CPCDS RI"));
        c.setSoftware(softwareComponent);

        return c;
    }
}