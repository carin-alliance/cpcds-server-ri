package ca.uhn.fhir.jpa.starter;

import java.util.List;

import org.hl7.fhir.r4.model.IdType;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;

@SuppressWarnings("ConstantConditions")
public class PatientAuthorizationInterceptor extends AuthorizationInterceptor {

    @Override
    public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {

        IdType userIdPatientId = null;
        boolean userIsAdmin = false;
        String authHeader = theRequestDetails.getHeader("Authorization");
        if ("Bearer dfw98h38r".equals(authHeader)) {
            // This user has access only to Patient/465cd4ab-4b71-4765-963b-c90fb19257be
            // resources
            userIdPatientId = new IdType("Patient", "465cd4ab-4b71-4765-963b-c90fb19257be");
        } else if ("Bearer 89gh653a2".equals(authHeader)) {
            // This user has access only to Patient/02d45714-445e-4093-aec5-5873f17c9ba8
            // resources
            userIdPatientId = new IdType("Patient", "02d45714-445e-4093-aec5-5873f17c9ba8");
        } else if ("Bearer 39ff939jgg".equals(authHeader)) {
            // This user has access to everything
            userIsAdmin = true;
        } else {
            // Throw an HTTP 401
            throw new AuthenticationException("Missing or invalid Authorization header value");
        }

        // If the user is a specific patient, we create the following rule chain:
        // Allow the user to read anything in their own patient compartment
        // Allow the user to write anything in their own patient compartment
        // If a client request doesn't pass either of the above, deny it
        if (userIdPatientId != null) {
            return new RuleBuilder().allow().read().allResources().inCompartment("Patient", userIdPatientId).andThen()
                    .allow().write().allResources().inCompartment("Patient", userIdPatientId).andThen().denyAll()
                    .build();
        }

        // If the user is an admin, allow everything
        if (userIsAdmin) {
            return new RuleBuilder().allowAll().build();
        }

        // By default, deny everything. This should never get hit, but it's
        // good to be defensive
        return new RuleBuilder().denyAll().build();

    }
}