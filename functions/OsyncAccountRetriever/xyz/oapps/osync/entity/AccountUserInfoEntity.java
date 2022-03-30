package xyz.oapps.osync.entity;

import xyz.oapps.osync.annotation.Table;

@Table(name = "AccountUserInfo", id="osync_user_id")
public class AccountUserInfoEntity extends OsyncEntity {

	private String osyncUserId;

	private String osyncId;

	private String name;

	private String email;
	
	private boolean adminUser = false;
	
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isAdminUser() {
		return adminUser;
	}

	public void setAdminUser(boolean adminUser) {
		this.adminUser = adminUser;
	}

}
