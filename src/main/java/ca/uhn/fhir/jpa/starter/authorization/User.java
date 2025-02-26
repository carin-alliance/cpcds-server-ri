package ca.uhn.fhir.jpa.starter.authorization;

import org.apache.commons.text.StringEscapeUtils;

import java.util.HashMap;
import java.util.Map;

public class User {

	private String username;
	private String password;
	private String patientId;
	private String createdDate;
	private String refreshToken;

	public User(String username, String password) {
		this(username, password, null);
	}

	public User(String username, String password, String patientId) {
		this(username, password, patientId, null, null);
	}

	public User(String username, String password, String patientId, String createdDate, String refreshToken) {
		// Escape all the inputs (since it could be from the browser)
		username = StringEscapeUtils.escapeJava(username);
		password = StringEscapeUtils.escapeJava(password);
		patientId = StringEscapeUtils.escapeJava(patientId);
		createdDate = StringEscapeUtils.escapeJava(createdDate);
		refreshToken = StringEscapeUtils.escapeJava(refreshToken);

		this.username = username;
		this.password = password;
		this.patientId = patientId;
		this.createdDate = createdDate;
		this.refreshToken = refreshToken;
	}

	public String getPatientId() {
		return this.patientId;
	}

	public String getUsername() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}

	public String getCreatedDate() {
		return this.createdDate;
	}

	public String getRefreshToken() {
		return this.refreshToken;
	}

	public static User getUser(String username) {
		return OauthEndpointController.getDB().readUser(username);
	}

	public Map<String, Object> toMap() {
		HashMap<String, Object> map = new HashMap<>();
		map.put("username", this.username);
		map.put("password", this.password);
		map.put("patient_id", this.patientId);
		return map;
	}

	@Override
	public String toString() {
		return "User " + this.username + "(" + this.patientId + "): password(" + this.password + ")";
	}
}
