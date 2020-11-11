package ca.uhn.fhir.jpa.starter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;

public class SearchInterceptor extends InterceptorAdapter {

    private static final Logger logger = ServerLogger.getLogger();

    @Override
    public void incomingRequestPreHandled(RestOperationTypeEnum theOperation,
            ActionRequestDetails theProcessedRequest) {
                RequestDetails theRequestDetails = theProcessedRequest.getRequestDetails();
                String resourceType = theRequestDetails.getResourceName();
                List<String> allowedSearchParameters = allowedSearchParameters(resourceType);
                List<String> allowedIncludes = allowedIncludes(resourceType);

                if (allowedSearchParameters == null && theRequestDetails.getParameters().size() > 0) {
                    String message = "No search parameters are allowed for this query: " + theRequestDetails.getCompleteUrl();
                    logger.severe("SearchInterceptor::MethodNotAllowedException:" + message);
                    throw new MethodNotAllowedException(message);
                } else if (theRequestDetails.getParameters().size() > 0) {
                    // Check each of the search params is allowed
                    if (allowedSearchParameters != null && !allowedSearchParameters.containsAll(theRequestDetails.getParameters().keySet()))  {
                        String message = "Unsupported search parameter in query " + theRequestDetails.getCompleteUrl();
                        logger.severe(message);
                        throw new MethodNotAllowedException(message);
                    }

                    // Check each of the include params is allowed
                    String[] includes = theRequestDetails.getParameters().get("_include");
                    if (includes != null) {
                        for (String i : includes) {
                            if (!allowedIncludes.contains(i)) {
                                String message = "Unsupported _include parameter " + i;
                                logger.severe(message);
                                throw new MethodNotAllowedException(message);
                            }
                        }
                    }
                }
    }

    /**
     * Get list of the valid search parameters for the resource type
     * @param resourceType - the type of the resource
     * @return a list of valid parameters or null
     */
    private static List<String> allowedSearchParameters(String resourceType) {
        Map<String, List<String>> searchParams = new HashMap<>();
        ArrayList<String> eobParams = new ArrayList<>();
        eobParams.add("_id");
        eobParams.add("patient");
        eobParams.add("_lastUpdated");
        eobParams.add("type");
        eobParams.add("identifier");
        eobParams.add("service-date");
        eobParams.add("_include");
        searchParams.put("ExplanationOfBenefit", eobParams);
        searchParams.put("Coverage", Collections.singletonList("_include"));
        return searchParams.get(resourceType);
    }

    /**
     * Get list of the valid _includes search params for the resource type
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