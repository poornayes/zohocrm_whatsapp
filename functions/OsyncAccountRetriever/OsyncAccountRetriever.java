import com.catalyst.Context;
import com.catalyst.basic.BasicIO;
import com.catalyst.basic.ZCFunction;

import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import com.zc.common.ZCProject;
import com.zc.component.cache.ZCCache;
import com.zc.component.circuits.ZCCircuit;
import com.zc.component.circuits.ZCCircuitDetails;
import com.zc.component.circuits.ZCCircuitExecutionDetails;

import xyz.oapps.osync.CurrentContext;
import xyz.oapps.osync.api.OsyncConstants.AccessLevel;
import xyz.oapps.osync.entity.AuthorizationEntity;
import xyz.oapps.osync.entity.IntegrationPropsEntity;
import xyz.oapps.osync.repo.IntegrationPropsRepository;
import xyz.oapps.osync.util.CommonUtil;

public class OsyncAccountRetriever implements ZCFunction {
	private static final Logger LOGGER = Logger.getLogger(OsyncAccountRetriever.class.getName());

	private static final String short_name = "osyn_acc_ret";

	IntegrationPropsRepository intPropsRepo = new IntegrationPropsRepository();

	@Override
	public void runner(Context context, BasicIO basicIO) throws Exception {


		try {
			ZCProject.initProject();
			AuthorizationEntity authEntity = new AuthorizationEntity();
			CurrentContext.createOrGetContext().setDisableDBCheck(Boolean.FALSE);
			CurrentContext.setCurrentContext("dummy_osync_id", authEntity, AccessLevel.CRON);
			
			JSONArray osyncIdsArray = (JSONArray) basicIO.getParameter("forceSyncOsyncId");
			List<String> osyncIdList = new ArrayList<String>();
			
			String result = "";
			
			CommonUtil.logOsyncInfo(short_name, "osyncIdsArray >>>>>>>>>>>>"+osyncIdsArray);
			
			if(osyncIdsArray != null && osyncIdsArray.length() > 0) {
				
				result = osyncIdsArray.toString(); 
				
			} else {

				CommonUtil.logOsyncInfo(short_name, "OSYNC_ACCOUNT_RETRIEVER");

				Long lastRowIdInLong = 0L;

				Integer offsetValue = 0;

				Integer limitValue = 100;


				CommonUtil.logOsyncInfo(short_name,  "FINAL :::::: Getting all integrations >>>>>>>> lastRowId:" + lastRowIdInLong + ":: offset:"
						+ offsetValue + ":::limit:::" + limitValue);


				//Integer integrationPropsCount = (Integer) intPropsRepo.getIntegrationPropsCount();

				List<IntegrationPropsEntity> findAllIntPropsForSync = intPropsRepo.findAllIntOsyncIdsForSync(lastRowIdInLong,offsetValue,limitValue);
				CommonUtil.logOsyncInfo(short_name,  "findAllIntPropsForSync  >>>>>>>> sizeeee ::::::" + findAllIntPropsForSync.size());
				CommonUtil.logOsyncInfo(short_name,  "findAllIntPropsForSync  >>>>>>>> first rowwww :" + findAllIntPropsForSync.get(0));

				
				for ( IntegrationPropsEntity intPropsEnt :  findAllIntPropsForSync) {
					String osyncId = intPropsEnt.getOsyncId();
					if(!osyncIdList.contains(osyncId)) {
						osyncIdList.add(osyncId);
					}
				}

				result = osyncIdList.toString();
				
				
//				List<String> splittedOsyncIdList = new ArrayList<String>();
//
//				List<String> finalRespJsonArr = processOsyncIds(osyncIdList, splittedOsyncIdList);
//				if(finalRespJsonArr.size() > 0) {
////					result = finalRespJsonArr.get(0).toString();
//					result = finalRespJsonArr.toString();
//				}
//				for (int i= 1; i < finalRespJsonArr.size() ; i++) {
//					
//					ZCCircuitDetails userBackupCircuit = ZCCircuit.getInstance().getCircuitInstance(4344000001192042L);
//					org.json.simple.JSONObject execInputJson = new org.json.simple.JSONObject();
//					execInputJson.put("osyncIdsArray", finalRespJsonArr.get(i));
//					
//					CommonUtil.logOsyncInfo(short_name, "osyncID list sent to Circuits for the second batch::: "+finalRespJsonArr.get(i));
//					ZCCircuitExecutionDetails circuitExecution = userBackupCircuit.execute("FS_"+CommonUtil.getRandomString(), execInputJson);
//					String executionId = circuitExecution.getExecutionId();
//				}
				
			}
			
//			JSONObject resultJson = new JSONObject();
//			resultJson.put("osyncAccounts", result);

			basicIO.setStatus(200);
			basicIO.write(result.toString());
		}
		catch(Exception e) {
			LOGGER.log(Level.SEVERE,"Exception in OsyncAccountRetriever",e);
			basicIO.setStatus(500);
		}

	}

	private List<String> processOsyncIds(List<String> osyncIdList, List<String> splittedOsyncIdList)
			throws JSONException {
		List<String> finalRespJsonArr = new ArrayList<String>();
		int indexCount = 0;
		int maxLimit = 5;


		for (String osyncId : osyncIdList) {
			splittedOsyncIdList.add(osyncId);
			//osyncIdList.remove(osyncId);
			indexCount++;
			if(indexCount == maxLimit){
				JSONObject responseJson = new JSONObject();
				responseJson.put("osyncIds",splittedOsyncIdList);

				finalRespJsonArr.add(responseJson.toString());

				splittedOsyncIdList = new ArrayList<String>();
				indexCount = 0;
			}
		}

		if(splittedOsyncIdList.size() > 0) {
			JSONObject responseJson = new JSONObject();
			responseJson.put("osyncIds",splittedOsyncIdList);

			finalRespJsonArr.add(responseJson.toString());
		}
		for (String osyncId : finalRespJsonArr) {
			osyncIdList.remove(osyncId);
		}
		CommonUtil.logOsyncInfo(short_name,  "finalRespJsonArr :::" + finalRespJsonArr);


		return finalRespJsonArr;
	}

}