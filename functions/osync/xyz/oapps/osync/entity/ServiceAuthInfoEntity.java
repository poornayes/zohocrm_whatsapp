package xyz.oapps.osync.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import xyz.oapps.osync.annotation.Table;

@Table(name = "ServiceAuthInfo" , id="auth_id")
public class ServiceAuthInfoEntity extends OsyncEntity {

	private String authId;
	
	private String osyncId;
	
	private String serviceId;
	
	private String osyncUserId;
	
	private String integId;
	
	private String tokenType;
	
	private String accessToken;
	
	private String refreshToken;
	
	private boolean leftService;
	
	private String userEmail;
	
	private String userFullName;
	
	private String userUniqueId;
	
	
	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getUserFullName() {
		return userFullName;
	}

	public void setUserFullName(String userFullName) {
		this.userFullName = userFullName;
	}

	public String getUserUniqueId() {
		return userUniqueId;
	}

	public void setUserUniqueId(String userUniqueId) {
		this.userUniqueId = userUniqueId;
	}

	public String getAuthId() {
		return authId;
	}

	public void setAuthId(String authId) {
		this.authId = authId;
	}

	public String getOsyncId() {
		return osyncId;
	}

	public void setOsyncId(String osyncId) {
		this.osyncId = osyncId;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getOsyncUserId() {
		return osyncUserId;
	}

	public void setOsyncUserId(String osyncUserId) {
		this.osyncUserId = osyncUserId;
	}

	public String getIntegId() {
		return integId;
	}

	public void setIntegId(String integId) {
		this.integId = integId;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public boolean isLeftService() {
		return leftService;
	}

	public void setLeftService(boolean leftService) {
		this.leftService = leftService;
	}

}
