package ca.uhn.fhir.jpa.starter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;

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

        if (authHeader != null) {

            // Get the Authorization Secret
            String secret = "secret";
            if (System.getenv("jwt.secret") != null)
                secret = System.getenv("jwt.secret");

            // Get the JWT token from the Authorization header
            String regex = "Bearer (.*)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(authHeader);
            String token = "";
            if (matcher.find() && matcher.groupCount() == 1)
                token = matcher.group(1);
            else
                throw new AuthenticationException("Authorization header is not in the form \"Bearer <token>\"");

            try {
                // Verify and decode the JWT token
                Algorithm algorithm = Algorithm.HMAC256(secret);
                // TODO: make iss shared
                JWTVerifier verifier = JWT.require(algorithm).withIssuer("http://localhost:8180")
                        .withAudience(theRequestDetails.getFhirServerBase()).build();
                DecodedJWT jwt = verifier.verify(token);
                String patientId = jwt.getClaim("client_id").asString();

                // Set the userIdPatientId based on the token
                if (patientId.equals("admin"))
                    userIsAdmin = true;
                else
                    userIdPatientId = new IdType("Patient", patientId);
            } catch (SignatureVerificationException exception) {
                throw new AuthenticationException("Authorization failed: invalid signature", exception);
            } catch (TokenExpiredException exception) {
                throw new AuthenticationException("Authorization failed: access token expired", exception);
            } catch (JWTVerificationException exception) {
                throw new AuthenticationException("Authorization failed", exception);
            }

            // If the user is a specific patient, we create the following rule chain:
            // Allow the user to read anything in their own patient compartment
            // Allow the user to read anything not in a patient compartment
            // If a client request doesn't pass either of the above, deny it
            if (userIdPatientId != null) {
                return new RuleBuilder().allow().read().allResources().inCompartment("Patient", userIdPatientId)
                        .andThen().allow().read().resourcesOfType("Practitioner").withAnyId().andThen().allow().read()
                        .resourcesOfType("PractitionerRole").withAnyId().andThen().allow().read()
                        .resourcesOfType("Organization").withAnyId().andThen().allow().read()
                        .resourcesOfType("Location").withAnyId().andThen().denyAll().build();
            }

            // If the user is an admin, allow everything
            if (userIsAdmin) {
                return new RuleBuilder().allowAll().build();
            }
        }

        // By default, deny everything. This should never get hit, but it's
        // good to be defensive
        return new RuleBuilder().allow().metadata().andThen().denyAll().build();

    }
}