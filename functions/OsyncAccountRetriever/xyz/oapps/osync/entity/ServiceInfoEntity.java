 package xyz.oapps.osync.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import xyz.oapps.osync.annotation.Table;

@Table(name = "ServiceInfo", id="service_id")
public class ServiceInfoEntity extends OsyncEntity {

	private String serviceId;

	private String name;
	
	private String displayName;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String authType;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String authScopes;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String clientId;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String clientSecret;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String authorizeUrl;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String tokenUrl;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String refreshTokenUrl;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String revokeTokenUrl;
	
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private boolean dynamicModule;

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public String getAuthScopes() {
		return authScopes;
	}

	public void setAuthScopes(String authScopes) {
		this.authScopes = authScopes;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getAuthorizeUrl() {
		return authorizeUrl;
	}

	public void setAuthorizeUrl(String authorizeUrl) {
		this.authorizeUrl = authorizeUrl;
	}

	public String getTokenUrl() {
		return tokenUrl;
	}

	public void setTokenUrl(String tokenUrl) {
		this.tokenUrl = tokenUrl;
	}

	public String getRefreshTokenUrl() {
		return refreshTokenUrl;
	}

	public void setRefreshTokenUrl(String refreshTokenUrl) {
		this.refreshTokenUrl = refreshTokenUrl;
	}

	public String getRevokeTokenUrl() {
		return revokeTokenUrl;
	}

	public void setRevokeTokenUrl(String revokeTokenUrl) {
		this.revokeTokenUrl = revokeTokenUrl;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public boolean isDynamicModule() {
		return dynamicModule;
	}

	public void setDynamicModule(boolean dynamicModule) {
		this.dynamicModule = dynamicModule;
	}
	
}
