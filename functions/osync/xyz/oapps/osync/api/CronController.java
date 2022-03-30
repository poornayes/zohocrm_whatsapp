package xyz.oapps.osync.api;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.RandomStringUtils;

import xyz.oapps.osync.CurrentContext;
import xyz.oapps.osync.annotation.PathVariable;
import xyz.oapps.osync.annotation.RequestMapping;
import xyz.oapps.osync.annotation.RequestParam;
import xyz.oapps.osync.api.OsyncConstants.AccessLevel;
import xyz.oapps.osync.entity.IntegrationPropsEntity;
import xyz.oapps.osync.entity.ModuleInfoEntity;
import xyz.oapps.osync.repo.IntegrationPropsRepository;
import xyz.oapps.osync.repo.IntegrationStatusRepository;
import xyz.oapps.osync.repo.ModuleInfoRepository;
import xyz.oapps.osync.repo.SyncLogEntityRepo;
import xyz.oapps.osync.service.IntegrationService;
import xyz.oapps.osync.util.CommonUtil;
public class CronController {

	private static final String short_name = "cron_cn";
	
	SyncLogEntityRepo syncRepo = new SyncLogEntityRepo();

	IntegrationStatusRepository integstatusRepo = new IntegrationStatusRepository();

	IntegrationPropsRepository intPropsRepo = new IntegrationPropsRepository();

	ModuleInfoRepository moduleRepo = new ModuleInfoRepository();

	IntegrationService intService = new IntegrationService();


	@RequestMapping(path = "/api/v1/integrations/count", method = "get", produces = "application/json", accessLevel = AccessLevel.CRON	)
	public Object getIntegrationCount() throws Exception {
		
		CommonUtil.logOsyncInfo(short_name, "/api/v1/integrations/count :: ");
		
		return intPropsRepo.getIntegrationPropsCount();
	}

	@RequestMapping(path = "/api/v1/integrations/all", method = "get", produces = "application/json", accessLevel = AccessLevel.CRON	)
	public List<IntegrationPropsEntity> getAllIntegrations(@RequestParam("lastRowId") String lastRowId,
			@RequestParam("offset") String offset,@RequestParam("limit") String limit) throws Exception {
		
		CommonUtil.logOsyncInfo(short_name, "/api/v1/integrations/all :: lastRowId >>>"+lastRowId+"::: offset>>> "+offset +":: limit >>"+limit);
		
		CommonUtil.logOsyncInfo(short_name,  "Getting all integrations >>>>>>>> lastRowId:" + lastRowId + ":: offset:"
				+ offset + ":::limit:::" + limit);
		
		Long lastRowIdInLong = 0L;
		if(lastRowId != null && !lastRowId.isEmpty()) {
			lastRowIdInLong = Long.valueOf(lastRowId);
		}
		
		Integer offsetValue = 0;
		if(offset != null && !offset.isEmpty()) {
			offsetValue = Integer.valueOf(offset);
		}
		
		Integer limitValue = 100;
		if(limit != null && !limit.isEmpty()) {
			limitValue = Integer.valueOf(limit);
		}
		
		CommonUtil.logOsyncInfo(short_name,  "FINAL :::::: Getting all integrations >>>>>>>> lastRowId:" + lastRowIdInLong + ":: offset:"
				+ offsetValue + ":::limit:::" + limitValue);
		
		List<IntegrationPropsEntity> findAllIntPropsForSync = intPropsRepo.findAllIntPropsForSync(lastRowIdInLong,offsetValue,limitValue);
		CommonUtil.logOsyncInfo(short_name,  "findAllIntPropsForSync  >>>>>>>> sizeeee ::::::" + findAllIntPropsForSync.size());
		CommonUtil.logOsyncInfo(short_name,  "findAllIntPropsForSync  >>>>>>>> first rowwww :" + findAllIntPropsForSync.get(0));
		return findAllIntPropsForSync;
	}

	@RequestMapping(path = "/api/v1/sync/{osync_id}", method = "post", produces = "application/json", accessLevel = AccessLevel.CRON	)
	public void startSync(@PathVariable("osync_id") String osyncId) throws Exception {
		CurrentContext.setCurrentContext(osyncId.toString());
		
		CommonUtil.logOsyncInfo(short_name, "/api/v1/sync/{osync_id} :: osyncID >>>"+osyncId);
		
		
		List<IntegrationPropsEntity> ipes = intPropsRepo.findAllByOsyncId(osyncId.toString());
		if (ipes == null || ipes.size() == 0) {
			CommonUtil.logOsyncInfo(short_name,  "ipes is null >>>>>>>>>." + ipes);
		}
		List<ModuleInfoEntity> modules = moduleRepo.findAllByServiceId(ipes.get(0).getLeftServiceId());
		for (ModuleInfoEntity moduleInfoEntity : modules) {
			for (IntegrationPropsEntity ipe : ipes) {
				if (moduleInfoEntity.getModuleId().equals(ipe.getLeftModuleId())) {
					CommonUtil.logOsyncInfo(short_name,  "Running sync for osyncId:" + osyncId + ":: module:"
							+ moduleInfoEntity + ":::IPE:::" + ipe);
					if (ipe.getSyncStatus() == 1) {
						intService.sync2(ipe.getOsyncId(), ipe.getIntegId(), false);
					} else {
						CommonUtil.logOsyncInfo(short_name,  "Paused integration." + ipe);
					}
				}
			}
		}
	}

}
