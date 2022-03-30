package xyz.oapps.osync.util;

import java.util.List;

import xyz.oapps.osync.SyncHandler;
import xyz.oapps.osync.SyncHandlerRepo;
import xyz.oapps.osync.entity.IntegrationPropsEntity;
import xyz.oapps.osync.entity.ModuleInfoEntity;
import xyz.oapps.osync.entity.ServiceInfoEntity;
import xyz.oapps.osync.repo.IntegrationPropsRepository;
import xyz.oapps.osync.repo.ModuleInfoRepository;
import xyz.oapps.osync.repo.ServiceInfoRepository;

public class SyncProps {
	String osyncId;
	String integId;
	IntegrationPropsEntity intProps = null;
	IntegrationPropsRepository intPropsRepo = new IntegrationPropsRepository();
	ServiceInfoRepository serviceInfoRepo = new ServiceInfoRepository();
	ModuleInfoRepository moduleInfoRepo = new ModuleInfoRepository();
	ServiceInfoEntity serviceA = null;
	ModuleInfoEntity moduleA = null;
	ServiceInfoEntity serviceB = null;
	ModuleInfoEntity moduleB = null; 
	SyncHandler controllerA = null;
	SyncHandler controllerB = null;
	
	public SyncProps(String osyncId, String integId) throws Exception {
		this.osyncId = osyncId;
		this.integId = integId;
		
		intProps = intPropsRepo.findById(integId);
		
		setServices();
		setModules();
		setSyncControllerInstances();
	}
	
	
	public ServiceInfoEntity getServiceA() {
		return serviceA;
	}


	public void setServiceA(ServiceInfoEntity serviceA) {
		this.serviceA = serviceA;
	}


	public ModuleInfoEntity getModuleA() {
		return moduleA;
	}


	public void setModuleA(ModuleInfoEntity moduleA) {
		this.moduleA = moduleA;
	}


	public ServiceInfoEntity getServiceB() {
		return serviceB;
	}


	public void setServiceB(ServiceInfoEntity serviceB) {
		this.serviceB = serviceB;
	}


	public ModuleInfoEntity getModuleB() {
		return moduleB;
	}


	public void setModuleB(ModuleInfoEntity moduleB) {
		this.moduleB = moduleB;
	}


	public SyncHandler getControllerA() {
		return controllerA;
	}


	public void setControllerA(SyncHandler controllerA) {
		this.controllerA = controllerA;
	}


	public SyncHandler getControllerB() {
		return controllerB;
	}


	public void setControllerB(SyncHandler controllerB) {
		this.controllerB = controllerB;
	}


	public String getOsyncId() {
		return osyncId;
	}


	public void setOsyncId(String osyncId) {
		this.osyncId = osyncId;
	}


	public String getIntegId() {
		return integId;
	}


	public void setIntegId(String integId) {
		this.integId = integId;
	}


	public IntegrationPropsEntity getIntProps() {
		return intProps;
	}


	public void setIntProps(IntegrationPropsEntity intProps) {
		this.intProps = intProps;
	}


	public IntegrationPropsRepository getIntPropsRepo() {
		return intPropsRepo;
	}


	public void setIntPropsRepo(IntegrationPropsRepository intPropsRepo) {
		this.intPropsRepo = intPropsRepo;
	}

	public void setServices() throws Exception {
		serviceA =  serviceInfoRepo.findByServiceId(this.intProps.getLeftServiceId());
		serviceB =  serviceInfoRepo.findByServiceId(this.intProps.getRightServiceId());
	}
	
	public void setModules() throws Exception {
		moduleA =  getModuleInfo(getServiceA() , true, getIntProps().getLeftModuleId());
		moduleB =  getModuleInfo(getServiceB() , false, getIntProps().getRightModuleId());
	}
	
	public void setSyncControllerInstances() throws Exception {
		controllerA =  getSyncControllerInstance(getServiceA(), getModuleA() , true);
		controllerB =  getSyncControllerInstance(getServiceB(), getModuleB(), false);
	}

	
	public ModuleInfoEntity getModuleInfo(ServiceInfoEntity service, boolean isLeft,
			String moduleId) throws Exception {
		if (service.isDynamicModule()) {
			SyncHandler syncHandler = SyncHandlerRepo.getInstance(service, null, this.osyncId, this.integId, isLeft);
			List<ModuleInfoEntity> modules = syncHandler.getModules(this.osyncId, service.getServiceId());
			if (modules != null) {
				for (ModuleInfoEntity moduleInfoEntity : modules) {
					if (moduleInfoEntity.getModuleId().equals(moduleId)) {
						return moduleInfoEntity;
					}
				}
			}
			throw new Exception("Module not found");
		} else {
			return moduleInfoRepo.findByModuleId(moduleId);
		}
	}
	
	private SyncHandler getSyncControllerInstance(ServiceInfoEntity service, ModuleInfoEntity module, boolean isLeft) {
		return SyncHandlerRepo.getInstance(service, module, this.osyncId, this.integId, isLeft);
	}
}
