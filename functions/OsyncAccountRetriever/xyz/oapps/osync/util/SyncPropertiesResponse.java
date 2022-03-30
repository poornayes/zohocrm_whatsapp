package xyz.oapps.osync.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import xyz.oapps.osync.OsyncException;
import xyz.oapps.osync.SyncHandler;
import xyz.oapps.osync.entity.ModuleInfoEntity;
import xyz.oapps.osync.entity.ServiceInfoEntity;

public class SyncPropertiesResponse {
	
	
	
	public SyncPropertiesResponse(SyncProps syncProp) throws OsyncException {
		
		this.leftDetails = new ServiceDetails(syncProp.getServiceA(),syncProp.getModuleA(),syncProp.getControllerA());
		
		this.rightDetails = new ServiceDetails(syncProp.getServiceB(),syncProp.getModuleB(),syncProp.getControllerB());
	}

	@JsonProperty("left")
	private SyncPropertiesResponse.ServiceDetails leftDetails;

	@JsonProperty("right")
	private SyncPropertiesResponse.ServiceDetails rightDetails;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public class ServiceDetails {
		private String serviceId;
		private String serviceName;
		private String moduleId;
		private Integer totalRecordCount;
		
		

		public ServiceDetails(ServiceInfoEntity service , ModuleInfoEntity module , SyncHandler syncHandler) throws OsyncException {
			this.serviceId = service.getServiceId();
			this.serviceName = service.getName();
			this.moduleId = module.getModuleId();
//			this.totalRecordCount = syncHandler.getTotalRecordsCountToSync();
		}

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

		public String getModuleId() {
			return moduleId;
		}

		public void setModuleId(String moduleId) {
			this.moduleId = moduleId;
		}

		public Integer getTotalRecordCount() {
			return totalRecordCount;
		}

		public void setTotalRecordCount(Integer totalRecordCount) {
			this.totalRecordCount = totalRecordCount;
		}
		
	}
}
