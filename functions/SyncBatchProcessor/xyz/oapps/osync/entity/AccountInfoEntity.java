package xyz.oapps.osync.entity;

import xyz.oapps.osync.annotation.Table;

@Table(name="OsyncAccount", id="osync_id")
public class AccountInfoEntity extends OsyncEntity {

	private String osyncId;

	private String remoteIdentifier;

	private String name;

	private String email;

	private String planName;

	public String getOsyncId() {
		return osyncId;
	}

	public void setOsyncId(String osyncId) {
		this.osyncId = osyncId;
	}

	public String getRemoteIdentifier() {
		return remoteIdentifier;
	}

	public void setRemoteIdentifier(String remoteIdentifier) {
		this.remoteIdentifier = remoteIdentifier;
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

	public String getPlanName() {
		return planName;
	}

	public void setPlanName(String planName) {
		this.planName = planName;
	}

}