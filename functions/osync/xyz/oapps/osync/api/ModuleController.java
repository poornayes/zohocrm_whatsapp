package xyz.oapps.osync.api;

import java.util.logging.Logger;

import xyz.oapps.osync.annotation.PathVariable;
import xyz.oapps.osync.annotation.RequestBody;
import xyz.oapps.osync.annotation.RequestMapping;
import xyz.oapps.osync.annotation.RequestParam;
import xyz.oapps.osync.api.OsyncConstants.AccessLevel;
import xyz.oapps.osync.entity.ModuleInfoEntity;
import xyz.oapps.osync.repo.ModuleInfoRepository;

public class ModuleController {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ModuleController.class.getName());
	private static final String short_name = "mod_cn";

	ModuleInfoRepository moduleRepo = new ModuleInfoRepository();

	@RequestMapping(path = "/api/v1/module", method = "post", produces = "application/json", accessLevel = AccessLevel.OSYNC_ADMIN)
	public ModuleInfoEntity addModule(@RequestBody ModuleInfoEntity moduleObj) throws Exception {

		return moduleRepo.save(moduleObj);

	}

	@RequestMapping(path = "/api/v1/module/{module_id}", method = "get", produces = "application/json", accessLevel = AccessLevel.ADMIN)
	public ModuleInfoEntity getModule(@PathVariable("module_id") String moduleId)
			throws NumberFormatException, Exception {
		return moduleRepo.findByModuleId(moduleId);
	}

}
