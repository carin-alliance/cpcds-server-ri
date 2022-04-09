package ca.uhn.fhir.jpa.starter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;

public class SearchInterceptor extends InterceptorAdapter {

  private static final Logger logger = ServerLogger.getLogger();

  @Override
  public void incomingRequestPreHandled(RestOperationTypeEnum theOperation,
      ActionRequestDetails theProcessedRequest) {
    // Ignore if using admin token
    String authHeader = theProcessedRequest.getRequestDetails().getHeader("Authorization");
    String adminHeader = "Bearer " + System.getenv("ADMIN_TOKEN");
    if (adminHeader.equals(authHeader))
      return;

    RequestDetails theRequestDetails = theProcessedRequest.getRequestDetails();
    String resourceType = theRequestDetails.getResourceName();
    List<String> allowedSearchParameters = allowedSearchParameters(resourceType);
    List<String> allowedIncludes = allowedIncludes(resourceType);
    List<String> autoAllowedResources = Arrays
        .asList(new String[] { "CodeSystem", "StructureDefinition", "ValueSet", "SearchParameter" });

    if (autoAllowedResources.contains(resourceType))
      return;

    if (theRequestDetails.getParameters().size() > 0) {
      // Check each of the search params is allowed
      if (!allowedSearchParameters.containsAll(theRequestDetails.getParameters().keySet())) {
        String message = "Unsupported search parameter in query " + theRequestDetails.getCompleteUrl();
        message += ". Supported search parameters for " + resourceType + " are " + allowedSearchParameters.toString();
        logger.severe(message);
        throw new InvalidRequestException(message);
      }

      // Check each of the include params is allowed
      String[] includes = theRequestDetails.getParameters().get("_include");
      if (includes != null) {
        for (String i : includes) {
          if (!allowedIncludes.contains(i)) {
            String message = "Unsupported _include parameter " + i;
            message += ". Supported _include for " + resourceType + " are " + allowedIncludes.toString();
            logger.severe(message);
            throw new InvalidRequestException(message);
          }
        }
      }
    }
  }

  /**
   * Get list of the valid search parameters for the resource type
   *
   * @param resourceType - the type of the resource
   * @return a list of valid parameters
   */
  private static List<String> allowedSearchParameters(String resourceType) {
    Map<String, ArrayList<String>> searchParams = new HashMap<>();
    ArrayList<String> eobParams = new ArrayList<>();
    ArrayList<String> covParams = new ArrayList<>();
    eobParams.add("_id");
    eobParams.add("patient");
    eobParams.add("_lastUpdated");
    eobParams.add("type");
    eobParams.add("identifier");
    eobParams.add("service-date");
    eobParams.add("service-start-date");
    eobParams.add("_include");

    covParams.add("_include");
    covParams.add("beneficiary");
    covParams.add("patient");
    covParams.add("subscriber");
    covParams.add("_id");

    searchParams.put("ExplanationOfBenefit", eobParams);
    searchParams.put("Coverage", covParams);

    ArrayList<String> allowedParams = searchParams.get(resourceType);
    if (allowedParams == null) {
      return Collections.singletonList("_format");
    } else {
      allowedParams.add("_format");
      return allowedParams;
    }
  }

  /**
   * Get list of the valid _includes search params for the resource type
   *
   * @param resourceType - the type of the resource
   * @return a list of valid _includes or null
   */
  private static List<String> allowedIncludes(String resourceType) {
    Map<String, List<String>> includesParams = new HashMap<>();
    ArrayList<String> eobIncludes = new ArrayList<>();
    eobIncludes.add("ExplanationOfBenefit:patient");
    eobIncludes.add("ExplanationOfBenefit:provider");
    eobIncludes.add("ExplanationOfBenefit:care-team");
    eobIncludes.add("ExplanationOfBenefit:coverage");
    eobIncludes.add("ExplanationOfBenefit:insurer");
    eobIncludes.add("ExplanationOfBenefit:*");
    includesParams.put("ExplanationOfBenefit", eobIncludes);
    includesParams.put("Coverage", Collections.singletonList("Coverage:payor"));
    return includesParams.get(resourceType);
  }
}