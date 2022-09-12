package ca.uhn.fhir.jpa.starter;

import java.time.LocalDate;
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
    RequestDetails theRequestDetails = theProcessedRequest.getRequestDetails();
    Map<String, String[]> parameters = theRequestDetails.getParameters();
    // Check the value of date parameters is of correct type.
    for (String k : parameters.keySet()) {
      List<String> keys = Arrays.asList("_lastUpdated");
      if (keys.contains(k)) {
        try {
          LocalDate.parse(parameters.get(k)[0]);
        } catch (Exception e) {
          String message = String.format("Incorrect value for search parameter %s provided. %s should be of type date",
              k, k);
          throwInvalidRequestException(message);
        }
      }
    }
    ;
    // Ignore if using admin token
    String authHeader = theRequestDetails.getHeader("Authorization");
    String adminHeader = "Bearer " + System.getenv("ADMIN_TOKEN");
    if (adminHeader.equals(authHeader))
      return;

    String resourceType = theRequestDetails.getResourceName();
    List<String> allowedSearchParameters = allowedSearchParameters(resourceType);
    List<String> allowedIncludes = allowedIncludes(resourceType);
    List<String> autoAllowedResources = Arrays
        .asList(new String[] { "CodeSystem", "StructureDefinition", "ValueSet", "SearchParameter" });

    if (autoAllowedResources.contains(resourceType))
      return;

    if (parameters.size() > 0) {
      // Check each of the search params is allowed
      if (!allowedSearchParameters.containsAll(parameters.keySet())) {
        String message = "Unsupported search parameter in query " + theRequestDetails.getCompleteUrl();
        message += ". Supported search parameters for " + resourceType + " are " + allowedSearchParameters.toString();
        throwInvalidRequestException(message);
      }

      // Check each of the include params is allowed
      String[] includes = parameters.get("_include");
      if (includes != null) {
        for (String i : includes) {
          if (!allowedIncludes.contains(i)) {
            String message = "Unsupported _include parameter " + i;
            message += ". Supported _include for " + resourceType + " are " + allowedIncludes.toString();
            throwInvalidRequestException(message);
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
    eobParams.add("billable-period-start");
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

  private static void throwInvalidRequestException(String message) {
    logger.severe(message);
    throw new InvalidRequestException(message);
  }
}
