package ca.uhn.fhir.jpa.starter;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.r4.hapi.rest.server.ServerCapabilityStatementProvider;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementImplementationComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestSecurityComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementSoftwareComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.ReferenceHandlingPolicy;
import org.hl7.fhir.r4.model.CapabilityStatement.ResourceInteractionComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.TypeRestfulInteraction;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;

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
    c.setName("CARIN CPCDS Reference Server");
    c.setTitle("CARIN Consumer Directed Payer Data Exchange Reference Implementation Server");
    c.addImplementationGuide("http://hl7.org/fhir/us/carin-bb/ImplementationGuide/hl7.fhir.us.carin-bb");
    c.addInstantiates("http://hl7.org/fhir/us/carin-bb/CapabilityStatement/c4bb");
    c.setStatus(PublicationStatus.DRAFT);
    c.setVersion("2.0.0");
    c.setExperimental(true);
    c.setPublisher("MITRE CARIN BB");

    CapabilityStatementImplementationComponent implementationComponent = new CapabilityStatementImplementationComponent(
        new StringType("MITRE CPCDS Reference Implementation for Carin BB 2.0.0"));
    implementationComponent.setUrl(HapiProperties.getServerAddress());
    c.setImplementation(implementationComponent);
    CapabilityStatementSoftwareComponent softwareComponent = new CapabilityStatementSoftwareComponent(
        new StringType("MITRE CPCDS RI"));
    c.setSoftware(softwareComponent);
    // Customize the rest component
    removeOperations(c.getRest());
    updateRestComponents(c.getRest());

    return c;
  }

  // Remove the operation component
  private void removeOperations(
      List<CapabilityStatementRestComponent> originalRests) {
    for (CapabilityStatementRestComponent rest : originalRests) {
      rest.setOperation(null);
    }
  }

  // Customize the Rest components
  private void updateRestComponents(List<CapabilityStatementRestComponent> originalRests) {

    for (CapabilityStatementRestComponent rest : originalRests) {
      rest.setSecurity(getSecurityComponent());
      for (CapabilityStatementRestResourceComponent resource : rest.getResource()) {
        resource.addReferencePolicy(ReferenceHandlingPolicy.RESOLVES);
        resource.setInteraction(getInteractionComponent());

        switch (resource.getType()) {
          case "Coverage":
            customizeCoverageResourceComponent(resource);
            break;
          case "ExplanationOfBenefit":
            customizeEobResourceComponent(resource);
            break;
          case "Organization":
            customizeOrganizationResourceComponent(resource);
            break;
          case "Patient":
            customizePatientResourceComponent(resource);
            break;
          case "RelatedPerson":
            customizeRelatedPersonResourceComponent(resource);
            break;
          case "Practitioner":
            customizePractitionerResourceComponent(resource);
            break;
          default:
            break;
        }
      }
    }
  }

  // Customize EOB resource component
  private void customizeEobResourceComponent(CapabilityStatementRestResourceComponent resource) {
    resource
        .addSupportedProfile("http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-ExplanationOfBenefit")
        .addSupportedProfile(
            "http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-ExplanationOfBenefit-Inpatient-Institutional")
        .addSupportedProfile(
            "http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-ExplanationOfBenefit-Outpatient-Institutional")
        .addSupportedProfile(
            "http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-ExplanationOfBenefit-Pharmacy")
        .addSupportedProfile(
            "http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-ExplanationOfBenefit-Professional-NonClinician")
        .addSupportedProfile(
            "http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-ExplanationOfBenefit-Oral")
        .setSearchParam(getEobSearchParameters())
        .setSearchInclude(getEobSearchIncludes());
  }

  // Customize Coverage resource component
  private void customizeCoverageResourceComponent(CapabilityStatementRestResourceComponent resource) {
    resource.addSupportedProfile("http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-Coverage")
        .setSearchParam(getCoverageSearchParameters())
        .setSearchInclude(getCoverageSearchInclude());
  }

  // Customize Patient resource component
  private void customizePatientResourceComponent(CapabilityStatementRestResourceComponent resource) {
    resource.addSupportedProfile("http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-Patient");

  }

  // Customize Patient resource component
  private void customizeRelatedPersonResourceComponent(CapabilityStatementRestResourceComponent resource) {
    resource.addSupportedProfile("http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-RelatedPerson");

  }

  // Customize Organization resource component
  private void customizeOrganizationResourceComponent(CapabilityStatementRestResourceComponent resource) {
    resource.addSupportedProfile("http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-Organization");

  }

  // Customize Practitioner resource component
  private void customizePractitionerResourceComponent(CapabilityStatementRestResourceComponent resource) {
    resource.addSupportedProfile("http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-Practitioner");

  }

  /**
   * Define the Security Component
   *
   * @return CapabilityStatementRestSecurityComponent instance
   */
  private CapabilityStatementRestSecurityComponent getSecurityComponent() {
    CapabilityStatementRestSecurityComponent securityComponent = new CapabilityStatementRestSecurityComponent();
    // Defining the service field
    CodeableConcept service = new CodeableConcept();
    ArrayList<Coding> codings = new ArrayList<>();
    codings.add(
        new Coding("http://terminology.hl7.org/CodeSystem/restful-security-service", "SMART-on-FHIR", "SMART on FHIR"));
    service.setCoding(codings);
    service.setText("OAuth2 using SMART-on-FHIR profile (see http://docs.smarthealthit.org)");

    // Defining the extension field
    Extension oauthExtension = new Extension();
    ArrayList<Extension> uris = new ArrayList<>();
    uris.add(new Extension("token", new UriType(getOauthTokenUrl())));
    uris.add(new Extension("authorize", new UriType(getOauthAuthorizationUrl())));
    uris.add(new Extension("introspect", new UriType(getOauthIntrospectionUrl())));
    oauthExtension.setUrl("http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris");
    oauthExtension.setExtension(uris);

    securityComponent.addService(service).addExtension(oauthExtension);
    return securityComponent;
  }

  /**
   * Define a list of supported Interaction Components
   */
  private List<ResourceInteractionComponent> getInteractionComponent() {
    List<ResourceInteractionComponent> interactions = new ArrayList<>();
    interactions.add(new ResourceInteractionComponent().setCode(TypeRestfulInteraction.READ));
    interactions.add(new ResourceInteractionComponent().setCode(TypeRestfulInteraction.SEARCHTYPE));
    interactions.add(new ResourceInteractionComponent().setCode(TypeRestfulInteraction.VREAD));
    return interactions;
  }

  private List<StringType> getEobSearchIncludes() {
    List<StringType> includes = new ArrayList<>();
    includes.add(new StringType("ExplanationOfBenefit:patient"));
    includes.add(new StringType("ExplanationOfBenefit:provider"));
    includes.add(new StringType("ExplanationOfBenefit:care-team"));
    includes.add(new StringType("ExplanationOfBenefit:coverage"));
    includes.add(new StringType("ExplanationOfBenefit:insurer"));
    includes.add(new StringType("ExplanationOfBenefit:*"));
    return includes;
  }

  private List<StringType> getCoverageSearchInclude() {
    List<StringType> includes = new ArrayList<>();
    includes.add(new StringType("Coverage:payor"));
    return includes;
  }

  /**
   * Define the list of supported search parameters for Coverage
   *
   * @return List<CapabilityStatementRestResourceSearchParamComponent>
   */
  private List<CapabilityStatementRestResourceSearchParamComponent> getCoverageSearchParameters() {
    List<CapabilityStatementRestResourceSearchParamComponent> searchParams = new ArrayList<>();
    searchParams.add(getIdSearchParamComponent());
    searchParams.add(getBeneficiarySearchParamComponent());
    searchParams.add(getPatientSearchParamComponent());
    searchParams.add(getSubscriberSearchParamComponent());

    return searchParams;
  }

  /**
   * Define the list of supported search parameters for EOB
   *
   * @return List<CapabilityStatementRestResourceSearchParamComponent>
   */
  private List<CapabilityStatementRestResourceSearchParamComponent> getEobSearchParameters() {
    List<CapabilityStatementRestResourceSearchParamComponent> searchParamComponents = new ArrayList<>();
    searchParamComponents.add(getIdSearchParamComponent());
    searchParamComponents.add(getLastUpdatedSearchParamComponent());
    searchParamComponents.add(getEobPatientSearchParamComponent());
    searchParamComponents.add(getEobTypeSearchParamComponent());
    searchParamComponents.add(getEobIdentifierSearchParamComponent());
    searchParamComponents.add(getEobServiceDateSearchParamComponent());
    searchParamComponents.add(getEobServiceStartDateSearchParamComponent());
    searchParamComponents.add(getEobBillablePeriodStartSearchParamComponent());
    return searchParamComponents;
  }

  private CapabilityStatementRestResourceSearchParamComponent getIdSearchParamComponent() {
    CapabilityStatementRestResourceSearchParamComponent idSearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
    idSearchParamComponent.setDefinition("http://hl7.org/fhir/SearchParameter/Resource-id");
    idSearchParamComponent.setName("_id");
    idSearchParamComponent.setType(SearchParamType.TOKEN);
    idSearchParamComponent.setDocumentation("The logical id of this artifact");
    return idSearchParamComponent;
  }

  private CapabilityStatementRestResourceSearchParamComponent getLastUpdatedSearchParamComponent() {
    CapabilityStatementRestResourceSearchParamComponent lastUpdatedSearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
    lastUpdatedSearchParamComponent.setDefinition("http://hl7.org/fhir/SearchParameter/Resource-lastUpdated");
    lastUpdatedSearchParamComponent.setName("_lastUpdated");
    lastUpdatedSearchParamComponent.setType(SearchParamType.DATE);
    lastUpdatedSearchParamComponent.setDocumentation("When the resource version last changed");

    return lastUpdatedSearchParamComponent;
  }

  private CapabilityStatementRestResourceSearchParamComponent getSubscriberSearchParamComponent() {
    CapabilityStatementRestResourceSearchParamComponent subscriberSearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
    subscriberSearchParamComponent.setName("subscriber");
    subscriberSearchParamComponent.setDocumentation("Reference to the subscriber");
    subscriberSearchParamComponent.setDefinition("http://hl7.org/fhir/SearchParameter/Coverage-subscriber");
    subscriberSearchParamComponent.setType(SearchParamType.REFERENCE);

    return subscriberSearchParamComponent;
  }

  private CapabilityStatementRestResourceSearchParamComponent getBeneficiarySearchParamComponent() {
    CapabilityStatementRestResourceSearchParamComponent beneficiarySearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
    beneficiarySearchParamComponent.setName("beneficiary");
    beneficiarySearchParamComponent.setDocumentation("Covered party");
    beneficiarySearchParamComponent.setDefinition("http://hl7.org/fhir/SearchParameter/Coverage-beneficiary");
    beneficiarySearchParamComponent.setType(SearchParamType.REFERENCE);

    return beneficiarySearchParamComponent;
  }

  private CapabilityStatementRestResourceSearchParamComponent getPatientSearchParamComponent() {
    CapabilityStatementRestResourceSearchParamComponent patientSearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
    patientSearchParamComponent.setName("patient");
    patientSearchParamComponent.setDocumentation("Retrieve coverages for a patient");
    patientSearchParamComponent.setDefinition("http://hl7.org/fhir/SearchParameter/Coverage-patient");
    patientSearchParamComponent.setType(SearchParamType.REFERENCE);

    return patientSearchParamComponent;
  }

  private CapabilityStatementRestResourceSearchParamComponent getEobPatientSearchParamComponent() {
    CapabilityStatementRestResourceSearchParamComponent eobPatientSearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
    eobPatientSearchParamComponent
        .setDefinition("http://hl7.org/fhir/us/carin-bb/SearchParameter/explanationofbenefit-patient");
    eobPatientSearchParamComponent.setName("patient");
    eobPatientSearchParamComponent.setType(SearchParamType.REFERENCE);
    eobPatientSearchParamComponent.setDocumentation("The reference to the patient");

    return eobPatientSearchParamComponent;
  }

  private CapabilityStatementRestResourceSearchParamComponent getEobTypeSearchParamComponent() {
    CapabilityStatementRestResourceSearchParamComponent eobTypeSearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
    eobTypeSearchParamComponent
        .setDefinition("http://hl7.org/fhir/us/carin-bb/SearchParameter/explanationofbenefit-type");
    eobTypeSearchParamComponent.setName("type");
    eobTypeSearchParamComponent.setType(SearchParamType.TOKEN);
    eobTypeSearchParamComponent.setDocumentation("The type of the ExplanationOfBenefit");

    return eobTypeSearchParamComponent;
  }

  private CapabilityStatementRestResourceSearchParamComponent getEobIdentifierSearchParamComponent() {
    CapabilityStatementRestResourceSearchParamComponent eobIdentifierSearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
    eobIdentifierSearchParamComponent
        .setDefinition("http://hl7.org/fhir/us/carin-bb/SearchParameter/explanationofbenefit-identifier");
    eobIdentifierSearchParamComponent.setName("identifier");
    eobIdentifierSearchParamComponent.setType(SearchParamType.TOKEN);
    eobIdentifierSearchParamComponent.setDocumentation("The business/claim identifier of the Explanation of Benefit");

    return eobIdentifierSearchParamComponent;
  }

  private CapabilityStatementRestResourceSearchParamComponent getEobServiceDateSearchParamComponent() {
    CapabilityStatementRestResourceSearchParamComponent eobServiceDateSearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
    eobServiceDateSearchParamComponent
        .setDefinition("http://hl7.org/fhir/us/carin-bb/SearchParameter/explanationofbenefit-service-date");
    eobServiceDateSearchParamComponent.setName("service-date");
    eobServiceDateSearchParamComponent.setType(SearchParamType.DATE);
    eobServiceDateSearchParamComponent.setDocumentation("Date of the service for the EOB");

    return eobServiceDateSearchParamComponent;
  }

  private CapabilityStatementRestResourceSearchParamComponent getEobServiceStartDateSearchParamComponent() {
    CapabilityStatementRestResourceSearchParamComponent eobServiceStartDateSearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
    eobServiceStartDateSearchParamComponent
        .setDefinition("http://hl7.org/fhir/us/carin-bb/SearchParameter/explanationofbenefit-service-start-date");
    eobServiceStartDateSearchParamComponent.setName("service-start-date");
    eobServiceStartDateSearchParamComponent.setType(SearchParamType.DATE);
    eobServiceStartDateSearchParamComponent.setDocumentation("Starting Date of the service for the EOB");

    return eobServiceStartDateSearchParamComponent;
  }

  private CapabilityStatementRestResourceSearchParamComponent getEobBillablePeriodStartSearchParamComponent() {
    CapabilityStatementRestResourceSearchParamComponent eobBillablePeriodStartSearchParamComponent = new CapabilityStatementRestResourceSearchParamComponent();
    eobBillablePeriodStartSearchParamComponent
        .setDefinition("http://hl7.org/fhir/us/carin-bb/SearchParameter/explanationofbenefit-billable-period-start");
    eobBillablePeriodStartSearchParamComponent.setName("billable-period-start");
    eobBillablePeriodStartSearchParamComponent.setType(SearchParamType.DATE);
    eobBillablePeriodStartSearchParamComponent.setDocumentation(
        "Starting Date of the service for the EOB using billablePeriod.period.start. The billable-period-start search parameter using the billablePeriod.period.start provides results with the earliest billablePeriod.start from a professional and non-clinician EOB or an oral EOB.");

    return eobBillablePeriodStartSearchParamComponent;
  }
}
