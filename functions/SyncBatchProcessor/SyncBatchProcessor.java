import com.catalyst.Context;
import com.catalyst.basic.BasicIO;
import com.catalyst.basic.ZCFunction;

import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.logging.Level;

import com.zc.common.ZCProject;
import com.zc.component.cache.ZCCache;

import xyz.oapps.osync.CurrentContext;
import xyz.oapps.osync.api.OsyncConstants.AccessLevel;
import xyz.oapps.osync.entity.AuthorizationEntity;
import xyz.oapps.osync.entity.IntegrationPropsEntity;
import xyz.oapps.osync.entity.ModuleInfoEntity;
import xyz.oapps.osync.repo.IntegrationPropsRepository;
import xyz.oapps.osync.repo.IntegrationStatusRepository;
import xyz.oapps.osync.repo.ModuleInfoRepository;
import xyz.oapps.osync.repo.SyncLogEntityRepo;
import xyz.oapps.osync.service.IntegrationService;
import xyz.oapps.osync.util.CommonUtil;

public class SyncBatchProcessor implements ZCFunction {
	private static final Logger LOGGER = Logger.getLogger(SyncBatchProcessor.class.getName());
	
	private static final String short_name = "syn_batch_processor";
	
	SyncLogEntityRepo syncRepo = new SyncLogEntityRepo();

	IntegrationStatusRepository integstatusRepo = new IntegrationStatusRepository();

	IntegrationPropsRepository intPropsRepo = new IntegrationPropsRepository();

	ModuleInfoRepository moduleRepo = new ModuleInfoRepository();
	
	IntegrationService intService = new IntegrationService();
	
	@Override
    public void runner(Context context, BasicIO basicIO) throws Exception {
		try {
			ZCProject.initProject();
			
			AuthorizationEntity authEntity = new AuthorizationEntity();
			CurrentContext.createOrGetContext().setDisableDBCheck(Boolean.FALSE);
			
			String osyncId = (String) basicIO.getParameter("osyncId");
			
			CurrentContext.setCurrentContext(osyncId, authEntity, AccessLevel.ADMIN);
			
			LOGGER.log(Level.INFO, "BASICIO_OsyncAccountRefiner >>>>> "+basicIO.getParameter("osyncId"));
			LOGGER.log(Level.INFO, "BASICIO_OsyncAccountRefiner STR>>>>> "+basicIO.getParameter("osyncId").toString());
			
			List<IntegrationPropsEntity> ipes = intPropsRepo.findAllByOsyncId(osyncId);
			if (ipes == null || ipes.size() == 0) {
				CommonUtil.logOsyncInfo(short_name,  "ipes is null >>>>>>>>>." + ipes);
			}
			List<ModuleInfoEntity> modules = moduleRepo.findAllByServiceId(ipes.get(0).getLeftServiceId());
			
			JSONArray finalIntegIdArray = new JSONArray();
			String result = "";
			for (ModuleInfoEntity moduleInfoEntity : modules) {
				for (IntegrationPropsEntity ipe : ipes) {
					if (moduleInfoEntity.getModuleId().equals(ipe.getLeftModuleId())) {
						CommonUtil.logOsyncInfo(short_name,  "Running sync for osyncId:" + osyncId + ":: module:"
								+ moduleInfoEntity + ":::IPE:::" + ipe);
						if (ipe.getSyncStatus() == 1) {
							
							try {
								intService.sync2(osyncId, ipe.getIntegId(), false);
								result = "Successfully done";
							} catch (Exception e) {
								result = "Exception occured = " + e.getMessage();
							}
							
						} else {
							CommonUtil.logOsyncInfo(short_name,  "Paused integration." + ipe);
						}
					}
				}
			}
			
			basicIO.setStatus(200);
			basicIO.write(osyncId+" - " + result);
		}
		catch(Exception e) {
			LOGGER.log(Level.SEVERE,"Exception in SyncBatchProcessor",e);
			basicIO.setStatus(500);
		}
	}
	
}