package ca.uhn.fhir.jpa.starter.authorization;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.uhn.fhir.jpa.starter.authorization.Database.Table;

@RestController
public class OauthEndpointController {


    /**
     * TODO:
     *  Port introspect endpoint
     *  PatientAuthorizationInterceptor
     *  Move & rename Authorization db
     *  Logger
     *  Update hapi.properties
     *  Update metadata
     *  Update .well-known/jwks
     *  Update readme
     */

    private static Database DB;
    private static RSAPublicKey publicKey;
    private static RSAPrivateKey privateKey;
    private static String keyId = "NjVBRjY5MDlCMUIwNzU4RTA2QzZFMDQ4QzQ2MDAyQjVDNjk1RTM2Qg";

    @PostConstruct
    protected static void postConstruct() throws NoSuchAlgorithmException, InvalidKeySpecException {
        DB = new Database();
        AuthUtils.initializeDB();
        initializeRSAKeys();
        System.out.println("Authorization Endpoint Controller created.");
    }

    private static void initializeRSAKeys() throws NoSuchAlgorithmException, InvalidKeySpecException {
		/*
		 * Code to generate keys adpated from
		 * https://stackoverflow.com/questions/24546397/generating-rsa-keys-for-given-
		 * modulus-and-exponent
		 * 
		 * p :
		 * 72685705065169728266450789649852557970253453395982124202036441196258256631809
		 * q :
		 * 78284536054273880334636739102056358277733743810954757271523407605758722854617
		 * Public Key :
		 * 30819a300d06092a864886f70d01010105000381880030818402406ca4f49908368ecdb45458942ab9360fc4e52a3540b955fe8e2f764c51a5f09a8bc32c8222f7e3abc30bc405327de78c6b988bc4cc81de367737163b92bdc6d90240308b84d7670d1aa24477bf2df8bb81f0268c4557240367befd67452777f798d1b029d2add2fbbcbd9a6f95dd8732903b6884636cedba6226bb9da631c20069f9
		 * Private Key :
		 * 3081b1020100300d06092a864886f70d010101050004819c30819902010002406ca4f49908368ecdb45458942ab9360fc4e52a3540b955fe8e2f764c51a5f09a8bc32c8222f7e3abc30bc405327de78c6b988bc4cc81de367737163b92bdc6d9020100024059287f5af88d7173c627008266f4b8da84f9290971502e1f52d709102fb2b11cd49e248756fd51867119e18db7c40f9f4d360b3ac4eeab5db070a5014aee4849020100020100020100020100020100
		 */
		BigInteger modulus = new BigInteger(
				"5690166698804597197330905768480486858877596610886363234480576904931540875874759967271069328480055496837733730620168171327423013607454238318286896004712153");
		BigInteger publicExponent = new BigInteger(
				"2542507730329925502019959402417157606871382892503593304032212496853538284138894186312740754437083011799807189636117018396940516735918014461610794552420857");
		BigInteger privateExponent = new BigInteger(
				"4669593480441015206282423283120213741469693238159411936473600414351862440739333747236900477132741820141503738805857072650451472878490200305741807022393417");

		KeyFactory kf = KeyFactory.getInstance("RSA");
		RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);
		RSAPrivateKeySpec privateKeySpec = new RSAPrivateKeySpec(modulus, privateExponent);
		publicKey = (RSAPublicKey) kf.generatePublic(publicKeySpec);
		privateKey = (RSAPrivateKey) kf.generatePrivate(privateKeySpec);
	}

    public static Database getDB() {
        return DB;
    }

    public static RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public static RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    public static String getKeyId() {
        return keyId;
    }

    @GetMapping(value = "/debug/Clients")
    public String getClients() {
        return DB.generateAndRunQuery(Table.CLIENTS);
    }

    @GetMapping(value = "/debug/Users")
    public String getUsers() {
        return DB.generateAndRunQuery(Table.USERS);
    }

    @GetMapping(value = "/register/user")
    public String getRegisterUserPage() {
        try {
            return new String(Files.readAllBytes(Paths.get("src/main/resources/templates/registerUser.html")));
        } catch (IOException e) {
            return "Error: Not Found";
        }
    } 

    @GetMapping(value = "/register/client")
    public String getRegisterClientPage() {
        try {
            return new String(Files.readAllBytes(Paths.get("src/main/resources/templates/registerClient.html")));
        } catch (IOException e) {
            return "Error: Not Found";
        }
    } 

    @PostMapping(value = "/register/client")
    public ResponseEntity<String> postRegisterClient(HttpServletRequest request, HttpEntity<String> entity,
        @RequestParam(name = "redirect_uri") String redirectUri) {
            return RegisterEndpoint.handleRegisterClient(redirectUri);
    }

    @GetMapping(value = "/authorization", params = { "response_type", "client_id", "redirect_uri", "scope", 
        "state", "aud" })
    public String getAuthorization(@RequestParam(name = "response_type") String responseType,
      @RequestParam(name = "client_id") String clientId, @RequestParam(name = "redirect_uri") String redirectURI,
      @RequestParam(name = "scope") String scope, @RequestParam(name = "state") String state,
      @RequestParam(name = "aud") String aud) {
        // Escape all the query parameters
        aud = StringEscapeUtils.escapeJava(aud);
        scope = StringEscapeUtils.escapeJava(scope);
        state = StringEscapeUtils.escapeJava(state);
        clientId = StringEscapeUtils.escapeJava(clientId);
        redirectURI = StringEscapeUtils.escapeJava(redirectURI);
        responseType = StringEscapeUtils.escapeJava(responseType);

        System.out.println(
            "AuthorizationEndpoint::Authorization:Received /authorization?response_type=" + responseType + "&client_id="
                + clientId + "&redirect_uri=" + redirectURI + "&scope=" + scope + "&state=" + state + "&aud=" + aud);

       return AuthorizationEndpoint.handleAuthorizationGet();
    }

    @PostMapping(value = "/authorization", params = { "response_type", "client_id", "redirect_uri", "scope", 
        "state", "aud" })
    public ResponseEntity<String> postAuthorization(HttpServletRequest request, HttpEntity<String> entity,
      @RequestParam(name = "response_type") String responseType, @RequestParam(name = "client_id") String clientId,
      @RequestParam(name = "redirect_uri") String redirectURI, @RequestParam(name = "scope") String scope,
      @RequestParam(name = "state") String state, @RequestParam(name = "aud") String aud) {
        // Escape all the query parameters
        aud = StringEscapeUtils.escapeJava(aud);
        scope = StringEscapeUtils.escapeJava(scope);
        state = StringEscapeUtils.escapeJava(state);
        clientId = StringEscapeUtils.escapeJava(clientId);
        redirectURI = StringEscapeUtils.escapeJava(redirectURI);
        responseType = StringEscapeUtils.escapeJava(responseType);

        System.out.println(
            "AuthorizationEndpoint::Authorization:Received /authorization?response_type=" + responseType + "&client_id="
                + clientId + "&redirect_uri=" + redirectURI + "&scope=" + scope + "&state=" + state + "&aud=" + aud);

       return AuthorizationEndpoint.handleAuthorizationPost(request, entity, aud, state, clientId, redirectURI, responseType);
    }

    @PostMapping(value = "/token", params = { "grant_type", "code", "redirect_uri" })
    public ResponseEntity<String> postAccessToken(HttpServletRequest request, @RequestParam(name = "grant_type") String grantType,
            @RequestParam(name = "code") String code, @RequestParam(name = "redirect_uri") String redirectURI) {
        // Escape all the query parameters
        code = StringEscapeUtils.escapeJava(code);
        grantType = StringEscapeUtils.escapeJava(grantType);
        redirectURI = StringEscapeUtils.escapeJava(redirectURI);

        System.out.println("TokenEndpoint::Token:Received request /token?grant_type=" + grantType + "&code=" + code
                + "&redirect_uri=" + redirectURI);
        return TokenEndpoint.handleTokenRequest(request, grantType, code, redirectURI);
    }

    @PostMapping(value = "/token", params = { "grant_type", "refresh_token" })
    public ResponseEntity<String> postRefreshToken(HttpServletRequest request, @RequestParam(name = "grant_type") String grantType,
            @RequestParam(name = "refresh_token") String refreshToken) {
        // Escape all the query parameters
        grantType = StringEscapeUtils.escapeJava(grantType);
        refreshToken = StringEscapeUtils.escapeJava(refreshToken);

        System.out.println("TokenEndpoint::RefreshToken:Received request /token?grant_type=" + grantType + "&refresh_token="
                + refreshToken);
        return TokenEndpoint.handleTokenRequest(request, grantType, refreshToken, null);
    }
}
