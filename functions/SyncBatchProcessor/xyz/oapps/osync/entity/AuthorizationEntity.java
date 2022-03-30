package xyz.oapps.osync.entity;

import xyz.oapps.osync.annotation.Table;

@Table(name = "OsyncAuthorization")
public class AuthorizationEntity extends OsyncEntity {

	private String osyncId;

	private String osyncUserId;

	private String token;

	private boolean admin = false;

	public String getOsyncId() {
		return osyncId;
	}

	public void setOsyncId(String osyncId) {
		this.osyncId = osyncId;
	}

	public String getOsyncUserId() {
		return osyncUserId;
	}

	public void setOsyncUserId(String osyncUserId) {
		this.osyncUserId = osyncUserId;
	}

	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

}
