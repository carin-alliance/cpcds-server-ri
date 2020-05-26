package ca.uhn.fhir.jpa.starter;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

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

                // Decode the header to get the kid
                DecodedJWT jwtHeader = JWT.decode(token);
                String kid = jwtHeader.getKeyId();

                // Verify and decode the JWT token
                Algorithm algorithm = Algorithm.RSA256(getRSAPublicKey(kid), null);
                System.out.println("Verifying JWT token: ISS (" + HapiProperties.getAuthServerAddress() + ") AUD ("
                        + theRequestDetails.getFhirServerBase() + ")");
                JWTVerifier verifier = JWT.require(algorithm).withIssuer(HapiProperties.getAuthServerAddress())
                        .withAudience(theRequestDetails.getFhirServerBase()).build();
                DecodedJWT jwt = verifier.verify(token);
                String patientId = jwt.getClaim("patient_id").asString();

                // Set the userIdPatientId based on the token
                if (patientId.equals("admin"))
                    userIsAdmin = true;
                else
                    userIdPatientId = new IdType("Patient", patientId);
            } catch (IOException exception) {
                System.out.println(exception);
                throw new AuthenticationException("Authorization failed: unable to retrieve RSA Keys", exception);
            } catch (NoSuchAlgorithmException exception) {
                System.out.println(exception);
                throw new AuthenticationException("Authorization failed: unable to use RSA Keys", exception);
            } catch (InvalidKeySpecException exception) {
                System.out.println(exception);
                throw new AuthenticationException("Authorization failed: unable to use RSA Keys", exception);
            } catch (SignatureVerificationException exception) {
                System.out.println(exception);
                throw new AuthenticationException("Authorization failed: invalid signature", exception);
            } catch (TokenExpiredException exception) {
                System.out.println(exception);
                throw new AuthenticationException("Authorization failed: access token expired", exception);
            } catch (JWTVerificationException exception) {
                System.out.println(exception);
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

    /**
     * Helper method to get the RSAPublicKey from the authorization server
     * 
     * @param kid - the key id to get
     * @return the RSAPublicKey for verifying the signature
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    private RSAPublicKey getRSAPublicKey(String kid)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        // Get the public key from the auth server
        OkHttpClient client = new OkHttpClient();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // TODO: Make this GET request work within a docker container
        // Request request = new
        // Request.Builder().url(HapiProperties.getAuthServerAddress() +
        // "/.well-known/jwks.json")
        // .build();
        // System.out.println(request.urlString());
        // Response response = client.newCall(request).execute();
        // if (response.code() != 200)
        // throw new AuthenticationException(
        // "Unable to GET " + HapiProperties.getAuthServerAddress() +
        // "/.well-known/jwks.json");
        // String body = response.body().string();

        String body = "{ 'keys': [ { 'kty': 'RSA', 'e': '2542507730329925502019959402417157606871382892503593304032212496853538284138894186312740754437083011799807189636117018396940516735918014461610794552420857', 'use': 'sig', 'kid': 'NjVBRjY5MDlCMUIwNzU4RTA2QzZFMDQ4QzQ2MDAyQjVDNjk1RTM2Qg', 'alg': 'RS256', 'n': '5690166698804597197330905768480486858877596610886363234480576904931540875874759967271069328480055496837733730620168171327423013607454238318286896004712153' }]}";

        Jwks jwks = gson.fromJson(body, Jwks.class);

        // Get the specific key from jwks
        BigInteger modulus = null;
        BigInteger publicExponent = null;
        for (RSAKey key : jwks.getKeys()) {
            if (key.getAlgorithm().equals("RS256") && key.getUse().equals("sig") && key.getKeyId().equals(kid)) {
                modulus = new BigInteger(key.getModulus());
                publicExponent = new BigInteger(key.getExponent());
            }
        }

        if (modulus == null || publicExponent == null) {
            throw new AuthenticationException("No public RS256 key (" + kid + ") for verifying signature");
        }

        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);
        return (RSAPublicKey) kf.generatePublic(publicKeySpec);
    }
}