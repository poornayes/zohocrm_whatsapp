package xyz.oapps.osync;

import org.json.JSONObject;

public class User {
	
	private String email;
	private String fullName;
	private String uniqueUserId;
	private JSONObject fullJson;

	public User(String email) {
		this.email = email;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUniqueUserId() {
		return uniqueUserId;
	}

	public void setUniqueUserId(String uniqueUserId) {
		this.uniqueUserId = uniqueUserId;
	}

	public String getFullName() {
		if (fullName == null || fullName.trim().isEmpty()) {
			return email.substring(0, email.indexOf("@"));
		}
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public JSONObject getFullJson() {
		return this.fullJson;
	}
	
	public void setFullJson(JSONObject userObject) {
		this.fullJson = userObject;
	}
}
