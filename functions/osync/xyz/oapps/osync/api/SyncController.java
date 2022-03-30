package xyz.oapps.osync.api;

import java.util.logging.Logger;

import xyz.oapps.osync.annotation.PathVariable;
import xyz.oapps.osync.annotation.RequestMapping;
import xyz.oapps.osync.annotation.RequestParam;
import xyz.oapps.osync.api.OsyncConstants.AccessLevel;
import xyz.oapps.osync.cron.OsyncCron;
import xyz.oapps.osync.entity.IntegrationPropsEntity;
import xyz.oapps.osync.entity.IntegrationStatusEntity;
import xyz.oapps.osync.entity.SyncLogEntity;
import xyz.oapps.osync.entity.SyncStatsEntity;
import xyz.oapps.osync.repo.IntegrationPropsRepository;
import xyz.oapps.osync.repo.IntegrationStatusRepository;
import xyz.oapps.osync.repo.SyncLogEntityRepo;
import xyz.oapps.osync.util.CommonUtil;


public class SyncController {
	
	IntegrationPropsRepository intPropsRepo = new IntegrationPropsRepository();
	IntegrationStatusRepository intStatusRepo = new IntegrationStatusRepository();
	SyncLogEntityRepo synclog = new SyncLogEntityRepo();
	private static final String short_name = "sy_cn";
	
	private static Logger log = Logger.getLogger(IntegrationController.class.getName());

	@RequestMapping(path = "/api/v1/synclog", method = "get", accessLevel = AccessLevel.ADMIN)
	public SyncLogEntity getSyncLog(@RequestParam("integ_id") String integId) throws Exception
	{
		IntegrationPropsEntity integrationPropsEntity = intPropsRepo.findById(integId);
		int syncStatus=integrationPropsEntity.getSyncStatus();
		CommonUtil.logOsyncInfo(short_name,"syncStatus>>>>>>> "+syncStatus);
		return synclog.findByIntegId(integId);	
	}
	
	@RequestMapping(path = "/api/v1/synchealthreport", method = "get", accessLevel = AccessLevel.ADMIN)
	public SyncStats getSyncHealthReport(@RequestParam("integ_id") String integId) throws Exception
	{
		SyncStats syncstats = new SyncStats();
		IntegrationPropsEntity integrationPropsEntity = intPropsRepo.findById(integId);
		syncstats.setIntegProps(integrationPropsEntity);
		
		IntegrationStatusEntity integrationStatusEntity = intStatusRepo.findById(integId);
		syncstats.setIntegStatus(integrationStatusEntity);
		
		SyncStatsEntity syncstatsEntity = synclog.findStatsByIntegId(integId);
		syncstats.setSyncstats(syncstatsEntity);			

		
			
		return syncstats;
	}
	
	
	@RequestMapping(path = "/api/v1/integration/{integ_id}/pause-sync", method = "post", accessLevel = AccessLevel.ADMIN)
	public IntegrationPropsEntity pauseSync(@PathVariable("integ_id") String integId) throws Exception {
		IntegrationPropsEntity integrationPropsEntity = intPropsRepo.findById(integId);
		if (integrationPropsEntity != null) {
			integrationPropsEntity.setSyncStatus(2);
			PageDetails page = new PageDetails();
			page.setsync_status(2);
			IntegrationPropsEntity ipe = intPropsRepo.update(integId, integrationPropsEntity);
			OsyncCron.pauseCron(ipe.getOsyncId());
			return ipe;
		}
		return null;
	}
	
	@RequestMapping(path = "/api/v1/integration/{integ_id}/resume-sync", method = "post", accessLevel = AccessLevel.ADMIN)
	public IntegrationPropsEntity resumeSync(@PathVariable("integ_id") String integId) throws Exception {
		IntegrationPropsEntity integrationPropsEntity = intPropsRepo.findById(integId);
		if (integrationPropsEntity != null) {
			integrationPropsEntity.setSyncStatus(1);
			PageDetails page = new PageDetails();
			page.setsync_status(1);
			IntegrationPropsEntity ipe = intPropsRepo.update(integId, integrationPropsEntity);
			OsyncCron.createOrUpdateCron(ipe.getOsyncId(), ipe.getSyncDuration());
			return ipe;
		}
		return null;
	}
	
	@RequestMapping(path = "/api/v1/integration/{integ_id}/stop-sync", method = "post", accessLevel = AccessLevel.ADMIN)
	public IntegrationPropsEntity stopSync(@PathVariable("integ_id") String integId) throws Exception {
		IntegrationPropsEntity integrationPropsEntity = intPropsRepo.findById(integId);
		if (integrationPropsEntity != null) {
			integrationPropsEntity.setSyncStatus(3);
			PageDetails page = new PageDetails();
			page.setsync_status(3);
			return intPropsRepo.update(integId, integrationPropsEntity);
		}
		return null;
	}

}
