package xyz.oapps.osync.repo;

import java.util.ArrayList;
import java.util.List;

import org.jooq.Record;
import org.jooq.SelectQuery;

import xyz.oapps.osync.api.RequestController;
import xyz.oapps.osync.db.OsyncDB;
import xyz.oapps.osync.entity.ModuleInfoEntity;

public class ModuleInfoRepository {

	static List<ModuleInfoEntity> modules = null;
	
	public ModuleInfoEntity findByModuleId(String moduleId) throws Exception {
		List<ModuleInfoEntity> modules2 = findAllModules();
		for (ModuleInfoEntity moduleInfoEntity : modules2) {
			if(moduleInfoEntity.getModuleId().equals(moduleId)) {
				return moduleInfoEntity;
			}
		}
		return null;
	}

	public ModuleInfoEntity findByModuleName(String serviceId, String moduleName) throws Exception {
		List<ModuleInfoEntity> modules2 = findAllModules();
		for (ModuleInfoEntity moduleInfoEntity : modules2) {
			if(moduleInfoEntity.getModuleId().equalsIgnoreCase(moduleName) && moduleInfoEntity.getServiceId().equals(serviceId)) {
				return moduleInfoEntity;
			}
		}
		return null;
	}

	public static List<ModuleInfoEntity> findAllModules() throws Exception {
		if (modules == null) {
			SelectQuery<Record> query = RequestController.query().selectFrom("Module").getQuery();
			modules = OsyncDB.get().executeQuery(ModuleInfoEntity.class, query);
		}
		return modules;
	}

	public ModuleInfoEntity save(ModuleInfoEntity moduleInfoObj) throws Exception {
		String moduleId = RequestController.getUUID();
		moduleInfoObj.setModuleId(moduleId);
		return OsyncDB.get().insert(moduleInfoObj);
	}

	public List<ModuleInfoEntity> findAllByServiceId(String serviceId) throws Exception {
		List<ModuleInfoEntity> serviceModules = new ArrayList<>();
		List<ModuleInfoEntity> allModules = findAllModules();
		for (ModuleInfoEntity moduleInfoEntity : allModules) {
			if(moduleInfoEntity.getServiceId().equals(serviceId)) {
				serviceModules.add(moduleInfoEntity);
			}
		}
		return serviceModules;
	}

}
