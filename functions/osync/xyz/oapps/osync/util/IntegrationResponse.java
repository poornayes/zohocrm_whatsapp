package xyz.oapps.osync.util;

import java.util.List;

import org.json.JSONArray;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import xyz.oapps.osync.entity.IntegrationPropsEntity;
import xyz.oapps.osync.entity.ModuleInfoEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class IntegrationResponse {

	@JsonProperty("integId")
	private String id;
	private String osyncId;
	private String hash;

	@JsonProperty("data")
	private IntegrationResponse.Entity entity;
	
	@JsonProperty("chosenModules")
	private IntegrationResponse.ChosenModules chosenModules;

	@JsonProperty("left")
	private IntegrationResponse.ServiceDetails leftDetails;

	@JsonProperty("right")
	private IntegrationResponse.ServiceDetails rightDetails;

	private IntegrationPropsEntity integProps;
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public class ServiceDetails {
		private String serviceId;
		private String serviceName;
		private String serviceDisplayName;
		private Long moduleId;

		@JsonProperty("auth")
		private IntegrationResponse.AuthDetails authDetails;

		private List<ModuleInfoEntity> modules;

		public String getServiceId() {
			return serviceId;
		}

		public void setServiceId(String serviceId) {
			this.serviceId = serviceId;
		}

		public String getServiceName() {
			return serviceName;
		}

		public void setServiceName(String serviceName) {
			this.serviceName = serviceName;
		}

		public Long getModuleId() {
			return moduleId;
		}

		public void setModuleId(Long moduleId) {
			this.moduleId = moduleId;
		}

		public List<ModuleInfoEntity> getModules() {
			return modules;
		}

		public void setModules(List<ModuleInfoEntity> modules) {
			this.modules = modules;
		}

		public IntegrationResponse.AuthDetails getAuthDetails() {
			return authDetails;
		}

		public void setAuthDetails(IntegrationResponse.AuthDetails authDetails) {
			this.authDetails = authDetails;
		}

		public String getServiceDisplayName() {
			return serviceDisplayName;
		}

		public void setServiceDisplayName(String serviceDisplayName) {
			this.serviceDisplayName = serviceDisplayName;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public class AuthDetails {
		private String type;
		private String url;
		private boolean isAuthorized;
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
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public boolean isAuthorized() {
			return isAuthorized;
		}
		public void setAuthorized(boolean isAuthorized) {
			this.isAuthorized = isAuthorized;
		}

	}

	public class Entity {

		@JsonProperty("left_module_id")
		private String leftId;

		@JsonProperty("right_module_id")
		private String rightId;

		private String direction;

		public String getLeftId() {
			return leftId;
		}

		public void setLeftId(String leftId) {
			this.leftId = leftId;
		}

		public String getRightId() {
			return rightId;
		}

		public void setRightId(String rightId) {
			this.rightId = rightId;
		}

		public String getDirection() {
			return direction;
		}

		public void setDirection(String direction) {
			this.direction = direction;
		}
	}
	
	public class ChosenModules {

		@JsonProperty("left_chosen_modules")
		private List<String> leftChosenModules;

		@JsonProperty("right_chosen_modules")
		private List<String> rightChosenModules;

		public List<String> getLeftChosenModules() {
			return leftChosenModules;
		}

		public void setLeftChosenModules(List<String> leftChosenModules) {
			this.leftChosenModules = leftChosenModules;
		}

		public List<String> getRightChosenModules() {
			return rightChosenModules;
		}

		public void setRightChosenModules(List<String> rightChosenModules) {
			this.rightChosenModules = rightChosenModules;
		}

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOsyncId() {
		return osyncId;
	}

	public void setOsyncId(String osyncId) {
		this.osyncId = osyncId;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public IntegrationResponse.ServiceDetails getLeftDetails() {
		return leftDetails;
	}

	public void setLeftDetails(IntegrationResponse.ServiceDetails leftDetails) {
		this.leftDetails = leftDetails;
	}

	public IntegrationResponse.ServiceDetails getRightDetails() {
		return rightDetails;
	}

	public void setRightDetails(IntegrationResponse.ServiceDetails rightDetails) {
		this.rightDetails = rightDetails;
	}

	public IntegrationResponse.Entity getEntity() {
		return entity;
	}

	public void setEntity(IntegrationResponse.Entity entity) {
		this.entity = entity;
	}

	public IntegrationPropsEntity getIntegProps() {
		return integProps;
	}

	public void setIntegProps(IntegrationPropsEntity integProps) {
		this.integProps = integProps;
	}

	public IntegrationResponse.ChosenModules getChosenModules() {
		return chosenModules;
	}

	public void setChosenModules(IntegrationResponse.ChosenModules chosenModules) {
		this.chosenModules = chosenModules;
	}
	
	
}
