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

import org.hl7.fhir.instance.model.api.IIdType;
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
        String authHeader = theRequestDetails.getHeader("Authorization");

        if (authHeader != null) {
            // Get the JWT token from the Authorization header
            String regex = "Bearer (.*)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(authHeader);
            String token = "";
            if (matcher.find() && matcher.groupCount() == 1) {
                token = matcher.group(1);

                String adminToken = System.getenv("ADMIN_TOKEN");
                if (adminToken != null && token.equals(adminToken)) {
                    return adminAuthorizedRule();
                } else {
                    try {
                        IIdType patientId = verify(token, theRequestDetails.getFhirServerBase());
                        if (patientId != null) return authorizedRule(patientId);
                        else return unauthorizedRule();
                    } catch (SignatureVerificationException e) {
                        String message = "Authorization failed: invalid signature";
                        logger.log(Level.SEVERE, message, e);
                        throw new AuthenticationException(message, e);
                    } catch (TokenExpiredException e) {
                        String message = "Authorization failed: access token expired";
                        logger.log(Level.SEVERE, message, e);
                        throw new AuthenticationException(message, e);
                    } catch (Exception e) {
                        String message = "Authorization failed";
                        logger.log(Level.SEVERE, message, e);
                        throw new AuthenticationException(message, e);
                    }
                }
            } else {
                throw new AuthenticationException(
                    "Authorization header is not in the form \"Bearer <token>\"");
            }
        }

        return unauthorizedRule();
    }

    private List<IAuthRule> adminAuthorizedRule() {
        return new RuleBuilder().allowAll().build();
    }

    private List<IAuthRule> authorizedRule(IIdType userIdPatientId) {
        return new RuleBuilder().allow().read().resourcesOfType("Coverage").inCompartment("Patient", userIdPatientId)
                .andThen().allow().read().resourcesOfType("ExplanationOfBenefit").inCompartment("Patient", userIdPatientId)
                .andThen().allow().read().resourcesOfType("Patient").inCompartment("Patient", userIdPatientId)
                .andThen().allow().read().resourcesOfType("Practitioner").withAnyId()
                .andThen().allow().read().resourcesOfType("PractitionerRole").withAnyId()
                .andThen().allow().read().resourcesOfType("Organization").withAnyId()
                .andThen().allow().read().resourcesOfType("OrganizationAffiliation").withAnyId()
                .andThen().allow().read().resourcesOfType("MedicationKnowledge").withAnyId()
                .andThen().allow().read().resourcesOfType("List").withAnyId()
                .andThen().allow().read().resourcesOfType("Location").withAnyId()
                .andThen().allow().read().resourcesOfType("HealthcareService").withAnyId()
                .andThen().allow().metadata().andThen().denyAll()
                .build();
    }

    private List<IAuthRule> unauthorizedRule() {
        // By default, deny everything except the metadata. This is for
        // unathorized users
        return new RuleBuilder().allow().metadata().andThen().denyAll().build();
    }

    private IIdType verify(String token, String fhirBase) throws SignatureVerificationException, TokenExpiredException, JWTVerificationException {
        Algorithm algorithm = Algorithm.RSA256(OauthEndpointController.getPublicKey(), null);
        logger.fine("Verifying JWT token ISS and AUD is " + fhirBase);
        JWTVerifier verifier = JWT.require(algorithm).withIssuer(fhirBase)
                .withAudience(fhirBase).build();
        DecodedJWT jwt = verifier.verify(token);
        String patientId = jwt.getClaim("patient_id").asString();
        if (patientId != null) return new IdType("Patient", patientId);
        else return null;
    }

}