package ca.uhn.fhir.jpa.starter;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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

import ca.uhn.fhir.jpa.starter.authorization.OauthEndpointController;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;

@SuppressWarnings("ConstantConditions")
public class PatientAuthorizationInterceptor extends AuthorizationInterceptor {

    private static final Logger logger = ServerLogger.getLogger();

    @Override
    public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {

        IdType userIdPatientId = null;
        boolean userIsAdmin = false;
        String authHeader = theRequestDetails.getHeader("Authorization");

        if (authHeader != null) {
            try {
                // Get the JWT token from the Authorization header
                String regex = "Bearer (.*)";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(authHeader);
                String token = "";
                if (matcher.find() && matcher.groupCount() == 1)
                    token = matcher.group(1);
                else
                    throw new AuthenticationException("Authorization header is not in the form \"Bearer <token>\"");

                // Verify and decode the JWT token
                Algorithm algorithm = Algorithm.RSA256(OauthEndpointController.getPublicKey(), null);
                logger.fine("Verifying JWT token ISS and AUD is " + theRequestDetails.getFhirServerBase());
                JWTVerifier verifier = JWT.require(algorithm).withIssuer(theRequestDetails.getFhirServerBase())
                        .withAudience(theRequestDetails.getFhirServerBase()).build();
                DecodedJWT jwt = verifier.verify(token);
                String patientId = jwt.getClaim("patient_id").asString();

                // Set the userIdPatientId based on the token
                if (patientId.equals("admin"))
                    userIsAdmin = true;
                else
                    userIdPatientId = new IdType("Patient", patientId);
            } catch (SignatureVerificationException e) {
                String message = "Authorization failed: invalid signature";
                logger.log(Level.SEVERE, message, e);
                throw new AuthenticationException(message, e);
            } catch (TokenExpiredException e) {
                String message = "Authorization failed: access token expired";
                logger.log(Level.SEVERE, message, e);
                throw new AuthenticationException(message, e);
            } catch (JWTVerificationException e) {
                String message = "Authorization failed";
                logger.log(Level.SEVERE, message, e);
                throw new AuthenticationException(message, e);
            }

            // If the user is a specific patient, we create the following rule chain:
            // Allow the user to read anything in their own patient compartment
            // Allow the user to read anything not in a patient compartment
            // If a client request doesn't pass either of the above, deny it
            if (userIdPatientId != null) {
                return new RuleBuilder().allow().read().resourcesOfType("Coverage").inCompartment("Patient", userIdPatientId)
                        .andThen().allow().read().resourcesOfType("ExplanationOfBenefit").inCompartment("Patient", userIdPatientId)
                        .andThen().allow().read().resourcesOfType("Patient").inCompartment("Patient", userIdPatientId)
                        .andThen().allow().read().resourcesOfType("Practitioner").withAnyId()
                        .andThen().allow().read().resourcesOfType("Organization").withAnyId()
                        .andThen().allow().metadata().andThen().denyAll()
                        .build();
            }

            // If the user is an admin, allow everything
            if (userIsAdmin) {
                return new RuleBuilder().allowAll().build();
            }
        }

        // By default, deny everything except the metadata. This is for
        // unathorized users
        return new RuleBuilder().allow().metadata().andThen().denyAll().build();

    }
}