package ca.uhn.fhir.jpa.starter.authorization;

import java.util.HashMap;
import java.util.Map;

public class Client {

	private String id;
	private String secret;
	private String redirectUri;
	private String createdDate;

	public Client(String id, String secret, String redirectUri) {
		this(id, secret, redirectUri, null);
	}

	public Client(String id, String secret, String redirectUri, String createdDate) {
		this.id = id;
		this.secret = secret;
		this.redirectUri = redirectUri;
		this.createdDate = createdDate;
	}

	public String getId() {
		return this.id;
	}

	public String getSecret() {
		return this.secret;
	}

	public String getRedirectUri() {
		return this.redirectUri;
	}

	public String getCreatedDate() {
		return this.createdDate;
	}

	public static Client getClient(String id) {
		return OauthEndpointController.getDB().readClient(id);
	}

	public Map<String, Object> toMap() {
		HashMap<String, Object> map = new HashMap<>();
		map.put("id", this.id);
		map.put("secret", this.secret);
		map.put("redirect", this.redirectUri);
		return map;
	}

	public boolean validateSecret(String secret) {
		return this.secret.equals(secret);
	}

	@Override
	public String toString() {
		return "Client " + this.id + " (" + this.secret + "): " + this.redirectUri;
	}
}
