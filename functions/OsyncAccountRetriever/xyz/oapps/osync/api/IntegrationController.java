package xyz.oapps.osync.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.jooq.tools.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.zc.component.circuits.ZCCircuit;
import com.zc.component.circuits.ZCCircuitDetails;
import com.zc.component.circuits.ZCCircuitExecutionDetails;

import xyz.oapps.osync.CurrentContext;
import xyz.oapps.osync.OsyncException;
import xyz.oapps.osync.SyncHandler;
import xyz.oapps.osync.SyncHandlerRepo;
import xyz.oapps.osync.annotation.PathVariable;
import xyz.oapps.osync.annotation.RequestBody;
import xyz.oapps.osync.annotation.RequestMapping;
import xyz.oapps.osync.annotation.RequestParam;
import xyz.oapps.osync.api.OsyncConstants.AccessLevel;
import xyz.oapps.osync.db.OsyncDB;
import xyz.oapps.osync.entity.AccountInfoEntity;
import xyz.oapps.osync.entity.AccountUserInfoEntity;
import xyz.oapps.osync.entity.AuthorizationEntity;
import xyz.oapps.osync.entity.DefaultFieldEntity;
import xyz.oapps.osync.entity.DefaultFieldMapEntity;
import xyz.oapps.osync.entity.FieldMapEntity;
import xyz.oapps.osync.entity.IntegrationPropsEntity;
import xyz.oapps.osync.entity.ModuleInfoEntity;
import xyz.oapps.osync.entity.ServiceAuthInfoEntity;
import xyz.oapps.osync.entity.ServiceInfoEntity;
import xyz.oapps.osync.entity.SyncLogEntity;
import xyz.oapps.osync.entity.UniqueValuesMapEntity;
import xyz.oapps.osync.fields.Fields;
import xyz.oapps.osync.repo.AccountInfoRepository;
import xyz.oapps.osync.repo.AccountUserInfoRepository;
import xyz.oapps.osync.repo.AuthorizationRepo;
import xyz.oapps.osync.repo.DefaultFieldsMappingRepo;
import xyz.oapps.osync.repo.DefaultFieldsRepo;
import xyz.oapps.osync.repo.FieldMapRepository;
import xyz.oapps.osync.repo.IntegrationPropsRepository;
import xyz.oapps.osync.repo.IntegrationStatusRepository;
import xyz.oapps.osync.repo.ModuleInfoRepository;
import xyz.oapps.osync.repo.ServiceAuthInfoRepository;
import xyz.oapps.osync.repo.ServiceInfoRepository;
import xyz.oapps.osync.repo.SyncLogEntityRepo;
import xyz.oapps.osync.repo.UniqueValuesMapRepo;
import xyz.oapps.osync.service.IntegrationService;
import xyz.oapps.osync.util.AuthorizerUtil;
import xyz.oapps.osync.util.CommonUtil;
import xyz.oapps.osync.util.IntegrationResponse;
import xyz.oapps.osync.util.SyncPropertiesResponse;
import xyz.oapps.osync.util.SyncProps;

public class IntegrationController {

	AccountInfoRepository accountRepo = new AccountInfoRepository();

	AccountUserInfoRepository userInfoRepo = new AccountUserInfoRepository();

	AuthorizationRepo authRepo = new AuthorizationRepo();

	ServiceAuthInfoRepository serviceAuthInfoRepo = new ServiceAuthInfoRepository();

	ServiceInfoRepository serviceRepo = new ServiceInfoRepository();

	IntegrationPropsRepository intPropsRepo = new IntegrationPropsRepository();

	IntegrationStatusRepository intStatusRepo = new IntegrationStatusRepository();

	FieldMapRepository fieldMapRepo = new FieldMapRepository();

	ModuleInfoRepository moduleMapRepo = new ModuleInfoRepository();

	DefaultFieldsRepo defaultFieldsRepo = new DefaultFieldsRepo();

	DefaultFieldsMappingRepo defaultFieldsMapRepo = new DefaultFieldsMappingRepo();

	IntegrationService intService = new IntegrationService();

	SyncLogEntityRepo synclog = new SyncLogEntityRepo();

	UniqueValuesMapRepo uvRepo = new UniqueValuesMapRepo();

	private static Logger log = Logger.getLogger(IntegrationController.class.getName());
	
	private static final String short_name = "int_cn";
	

	@RequestMapping(path = "/api/v1/default-fields", method = "get", produces = "application/json", accessLevel = AccessLevel.ADMIN)
	public List<DefaultFieldEntity> getDefaultFields(@RequestParam("service_id") String serviceId,
			@RequestParam("module_id") String moduleId) throws Exception {
		
		CommonUtil.logOsyncInfo(short_name, "/api/v1/default-fields  ::: service_id >"+ serviceId+":: module_id >>"+moduleId);
		return defaultFieldsRepo.findByServiceIdAndModuleId(serviceId, moduleId);
	}

	@RequestMapping(path = "/api/v1/all-fields", method = "get", produces = "application/json", accessLevel = AccessLevel.ADMIN)
	public Fields getAllFields(@RequestParam("service_id") String serviceId, @RequestParam("module_id") String moduleId,
			@RequestParam("integ_id") String integId, @RequestParam("left_service") boolean isLeft) throws Exception {
		
		CommonUtil.logOsyncInfo(short_name, "/api/v1/all-fields  ::: service_id >"+ serviceId+":: module_id >>"+moduleId+":: integ_id >>"+integId);
		
		ServiceInfoEntity serviceInfo = intService.getServiceInfo(serviceId);
		ModuleInfoEntity moduleInfo = intService.getModuleInfo(serviceInfo, CurrentContext.getCurrentOsyncId(), integId, isLeft, moduleId);
		return SyncHandlerRepo.getInstance(serviceInfo, moduleInfo, CurrentContext.getCurrentOsyncId(), integId, isLeft)
				.getFields();
	}

	@RequestMapping(path = "/api/v1/all-modules", method = "get", produces = "application/json", accessLevel = AccessLevel.ADMIN)
	public List<ModuleInfoEntity> getAllModules(@RequestParam("service_id") String serviceId) throws Exception {
		CommonUtil.logOsyncInfo(short_name, "/api/v1/all-modules  ::: service_id >"+ serviceId);
		return moduleMapRepo.findAllByServiceId(serviceId);
	}

	@RequestMapping(path = "/api/v1/default-fields-map", method = "get", produces = "application/json", accessLevel = AccessLevel.ADMIN)
	public List<DefaultFieldMapEntity> getDefaultFieldsMap(@RequestParam("left_service_id") String leftServiceId,
			@RequestParam("left_module_id") String leftModuleId,
			@RequestParam("right_service_id") String rightServiceId,
			@RequestParam("right_module_id") String rightModuleId) throws Exception {
		
		CommonUtil.logOsyncInfo(short_name, "/api/v1/default-fields-map  ::: left_module_id >"+ leftModuleId+" ::: right_service_id >>"+rightServiceId+":: right_module_id >> "+rightModuleId);
		return defaultFieldsMapRepo.findAllDefaultMappings(leftServiceId, leftModuleId, rightServiceId, rightModuleId);
	}

	@RequestMapping(path = "/api/v1/integration/{integ_id}/fields", method = "get", produces = "application/json", accessLevel = AccessLevel.ADMIN)

	public List<FieldMapEntity> getAllFields(@PathVariable("integ_id") String integId) throws Exception {
		CommonUtil.logOsyncInfo(short_name, "/api/v1/integration/{integ_id}/fields  ::: integ_id >"+ integId);
		return fieldMapRepo.findAllByIntegId(integId);
	}

	@RequestMapping(path = "/api/v1/integration/{integ_id}/modules", method = "get", produces = "application/json", accessLevel = AccessLevel.ADMIN)
	public IntegrationResponse getModules(@PathVariable("integ_id") String integId) throws Exception {
		
		CommonUtil.logOsyncInfo(short_name, "/api/v1/integration/{integ_id}/modules :: get ::: integ_id >"+ integId);
		
		IntegrationResponse integResponse = new IntegrationResponse();

		IntegrationPropsEntity findById = intPropsRepo.findById(integId);
		List<IntegrationPropsEntity> findAllByOsyncId = intPropsRepo
				.findAllByOsyncId(CurrentContext.getCurrentOsyncId());

		List<String> leftChosenModules = new ArrayList<String>();
		List<String> rightChosenModules = new ArrayList<String>();
		if (findById != null) {
			String leftServiceId = findById.getLeftServiceId();
			String rightServiceId = findById.getRightServiceId();

			IntegrationResponse.Entity entityDetails = integResponse.new Entity();
			entityDetails.setDirection(findById.getDirection() + "");
			entityDetails.setLeftId(findById.getLeftModuleId() + "");
			entityDetails.setRightId(findById.getRightModuleId() + "");

			IntegrationResponse.ServiceDetails leftServiceDetails = integResponse.new ServiceDetails();

			for (IntegrationPropsEntity integProps : findAllByOsyncId) {
				if (integProps.getLeftServiceId().equals(leftServiceId) && integProps.getLeftModuleId() != null
						&& !integProps.getLeftModuleId().isEmpty()) {
					leftChosenModules.add(integProps.getLeftModuleId());
				}
				if (integProps.getRightServiceId().equals(rightServiceId) && integProps.getRightModuleId() != null
						&& !integProps.getRightModuleId().isEmpty()) {
					rightChosenModules.add(integProps.getRightModuleId());
				}
			}
			IntegrationResponse.ChosenModules chosenModules = integResponse.new ChosenModules();
			chosenModules.setLeftChosenModules(leftChosenModules);
			chosenModules.setRightChosenModules(rightChosenModules);

			ServiceInfoEntity leftService = serviceRepo.findByServiceId(leftServiceId);
			leftServiceDetails.setModules(getModulesToMap(leftService, leftServiceId, findById.getOsyncId(), true));
			leftServiceDetails.setServiceId(leftServiceId + "");
			leftServiceDetails.setServiceName(leftService.getName());

			IntegrationResponse.ServiceDetails rightServiceDetails = integResponse.new ServiceDetails();

			ServiceInfoEntity rightService = serviceRepo.findByServiceId(rightServiceId);
			rightServiceDetails.setModules(getModulesToMap(rightService, rightServiceId, findById.getOsyncId(), false));
			rightServiceDetails.setServiceId(rightServiceId + "");
			rightServiceDetails.setServiceName(rightService.getName());

			integResponse.setLeftDetails(leftServiceDetails);
			integResponse.setRightDetails(rightServiceDetails);
			integResponse.setEntity(entityDetails);
			integResponse.setChosenModules(chosenModules);

			integResponse.setIntegProps(findById);
		}
		return integResponse;
	}

	@RequestMapping(path = "/api/v1/integration/{integ_id}/modules", method = "post", produces = "application/json", accessLevel = AccessLevel.ADMIN)
	public IntegrationPropsEntity saveModules(@PathVariable("integ_id") String integId, @RequestBody String payload)
			throws Exception {
		
		CommonUtil.logOsyncInfo(short_name, "/api/v1/integration/{integ_id}/modules :: post ::: integ_id >"+ integId);
		
		JSONObject payloadJson = new JSONObject(payload);
		String leftModuleId = payloadJson.optString("left_module_id");
		String rightModuleId = payloadJson.optString("right_module_id");
		int syncDirection = payloadJson.optInt("direction");

		IntegrationPropsEntity integrationPropsEntity = intPropsRepo.findById(integId);
		if (integrationPropsEntity != null) {
			integrationPropsEntity.setLeftModuleId(leftModuleId);
			integrationPropsEntity.setRightModuleId(rightModuleId);
			integrationPropsEntity.setDirection(syncDirection);

			return intPropsRepo.update(integId, integrationPropsEntity);
		}
		return null;
	}

	@RequestMapping(path = "/api/v1/integration/{integ_id}/start-sync", method = "post", produces = "application/json", accessLevel = AccessLevel.ADMIN)
	public IntegrationPropsEntity startSync(@PathVariable("integ_id") String integId, @RequestBody String payload)
			throws Exception {

		CommonUtil.logOsyncInfo(short_name, "/api/v1/integration/{integ_id}/start-sync ::: integ_id >"+ integId);
		
		JSONObject payloadJson = new JSONObject(payload);
		String masterService = payloadJson.optString("masterService");
		int syncInterval = payloadJson.optInt("syncDuration");

		String osyncId = payloadJson.optString("osyncId");

		IntegrationPropsEntity intProps = intPropsRepo.findById(integId);
		CommonUtil.logOsyncInfo(short_name, "IntegrationProps Entity..." + intProps);

		boolean isFieldMapAdded = fieldMapRepo.isFieldMapAdded(osyncId, integId);
		if (isFieldMapAdded == false) {
			throw new OsyncException(OsyncException.Code.INT_STATUS_FIELD_MAP_NOT_ADDED);
		}

		if (intProps == null) {
			throw new OsyncException(OsyncException.Code.INT_STATUS_PROPS_NULL);
		}

		String leftServiceId = intProps.getLeftServiceId();
		String rightServiceId = intProps.getRightServiceId();

		ServiceAuthInfoEntity leftServiceAuthInfo = serviceAuthInfoRepo.findLeftServiceAuthInfo(osyncId, leftServiceId);
		if (leftServiceAuthInfo == null) {
			throw new OsyncException(OsyncException.Code.INT_STATUS_LEFT_SERVICE_AUTH_NULL);
		}
		String leftAccessToken = leftServiceAuthInfo.getAccessToken();
		if (leftAccessToken == null) {
			throw new OsyncException(OsyncException.Code.INT_STATUS_LEFT_ACCESS_TOKEN_NULL);
		}

		ServiceAuthInfoEntity rightServiceAuthInfo = serviceAuthInfoRepo.findRightServiceAuthInfo(osyncId,
				rightServiceId);
		if (rightServiceAuthInfo == null) {
			throw new OsyncException(OsyncException.Code.INT_STATUS_RIGHT_SERVICE_AUTH_NULL);
		}

		String rightAccessToken = rightServiceAuthInfo.getAccessToken();
		if (rightAccessToken == null) {
			throw new OsyncException(OsyncException.Code.INT_STATUS_RIGHT_ACCESS_TOKEN_NULL);
		}

		intProps.setMasterService(masterService);
		intProps.setSyncDuration(syncInterval);
		intProps.setSyncStatus(1);
		IntegrationPropsEntity ipe = intPropsRepo.update(integId, intProps);
//		OsyncCron.createOrUpdateCron(ipe.getOsyncId(), syncInterval);
		return ipe;
	}

	@RequestMapping(path = "/api/v1/integration/{integ_id}/get-page", method = "get", produces = "application/json", accessLevel = AccessLevel.ADMIN)
	public PageDetails getPage(@PathVariable("integ_id") String integId, @RequestParam("osync_id") String osyncId)
			throws Exception {
		
		CommonUtil.logOsyncInfo(short_name, "/api/v1/integration/{integ_id}/get-page ::: osync_id >"+osyncId+":: integ_id >"+ integId);
		
		PageDetails page = new PageDetails();
		IntegrationPropsEntity findTopByOsyncIdAndIntegId = intPropsRepo.findTopByOsyncIdAndIntegId(osyncId, integId);

		page.setAuthorization_page(false);
		page.setModule_page(false);
		page.setField_page(false);
		page.setConfiguration_page(false);
		page.setsync_status(0);

		if (findTopByOsyncIdAndIntegId != null) {
			String leftServiceId = findTopByOsyncIdAndIntegId.getLeftServiceId();
			String rightServiceId = findTopByOsyncIdAndIntegId.getRightServiceId();

			String leftModuleId = findTopByOsyncIdAndIntegId.getLeftModuleId();
			String rightModuleId = findTopByOsyncIdAndIntegId.getRightModuleId();
			String masterService = findTopByOsyncIdAndIntegId.getMasterService();
			int sync_status = findTopByOsyncIdAndIntegId.getSyncStatus();

			boolean isFieldMapAdded = fieldMapRepo.isFieldMapAdded(osyncId, integId);
			CommonUtil.logOsyncInfo(short_name, "findTopByOsyncIdAndIntegIdField..." + isFieldMapAdded);

			ServiceAuthInfoEntity findTopByIntegIdAndLeftServiceId = serviceAuthInfoRepo
					.findLeftServiceAuthInfo(osyncId, leftServiceId);
			String leftAccessToken = "";
			String rightAccessToken = "";
			if (findTopByIntegIdAndLeftServiceId != null) {
				leftAccessToken = findTopByIntegIdAndLeftServiceId.getAccessToken();
			}
			ServiceAuthInfoEntity findTopByIntegIdAndRightServiceId = serviceAuthInfoRepo
					.findRightServiceAuthInfo(osyncId, rightServiceId);
			if (findTopByIntegIdAndRightServiceId != null) {
				rightAccessToken = findTopByIntegIdAndRightServiceId.getAccessToken();
			}

			if (leftAccessToken != null && !leftAccessToken.isEmpty() && rightAccessToken != null
					&& !rightAccessToken.isEmpty()) {
				page.setAuthorization_page(true);
			}

			if (leftModuleId != null && !leftModuleId.isEmpty() && rightModuleId != null && !rightModuleId.isEmpty()) {
				page.setModule_page(true);
			}

			if (isFieldMapAdded) {
				page.setField_page(true);
			}

			if (masterService != null && !masterService.isEmpty()) {
				page.setConfiguration_page(true);
			}

			page.setsync_status(sync_status);

			CommonUtil.logOsyncInfo(short_name, "fieldId..." + isFieldMapAdded);

			CommonUtil.logOsyncInfo(short_name, "leftAccessToken..." + leftAccessToken);
			CommonUtil.logOsyncInfo(short_name, "rightAccessToken..." + rightAccessToken);
			CommonUtil.logOsyncInfo(short_name, "leftServiceId..." + leftServiceId);
			CommonUtil.logOsyncInfo(short_name, "rightServiceId..." + rightServiceId);
			CommonUtil.logOsyncInfo(short_name, "leftModuleId..." + leftModuleId);
			CommonUtil.logOsyncInfo(short_name, "rightModuleId..." + rightModuleId);
			CommonUtil.logOsyncInfo(short_name, "fieldId..." + isFieldMapAdded);
			CommonUtil.logOsyncInfo(short_name, "masterService..." + masterService);

		}
		return page;
	}

	@RequestMapping(path = "/api/v1/integration/{integ_id}", method = "get", produces = "application/json", accessLevel = AccessLevel.PUBLIC)

	public IntegrationPropsEntity getIntegration(@PathVariable("integ_id") String integId) throws Exception {
		IntegrationPropsEntity findById = intPropsRepo.findById(integId);
		// if (findById != null) {
		// findById.setFields(getAllFields(integId));
		// }
		return findById;
	}

	@RequestMapping(path = "/api/v1/integration", method = "get", produces = "application/json", accessLevel = AccessLevel.ADMIN)
	public IntegrationPropsEntity getIntegration(@RequestBody IntegrationPropsEntity entity) throws Exception {
		
		CommonUtil.logOsyncInfo(short_name, "/api/v1/integration");
		
		entity.setOsyncId(CurrentContext.getCurrentContext().getOsyncId());
		return intPropsRepo.save(entity);
	}

	@RequestMapping(path = "/api/v1/integration/{integ_id}/fields", method = "post", produces = "application/json", accessLevel = AccessLevel.ADMIN)
	public List<FieldMapEntity> createFieldsMapping(@PathVariable("integ_id") String integId,
			@RequestBody FieldMapEntity[] fieldMapList) throws Exception {
		
		CommonUtil.logOsyncInfo(short_name, "/api/v1/integration/{integ_id}/fields :: integId >>>"+integId);
		
		List<FieldMapEntity> fieldMaps = fieldMapRepo.findAllByIntegId(integId);
		if (fieldMaps != null) {
			for (FieldMapEntity fieldMapEntity : fieldMaps) {
				fieldMapRepo.delete(fieldMapEntity);
			}
		}
		for (FieldMapEntity fieldMapEntity : fieldMapList) {
			fieldMapEntity.setIntegId(integId);
			fieldMapEntity.setOsyncId(CurrentContext.getCurrentContext().getOsyncId());
			fieldMapRepo.save(fieldMapEntity);
		}
		return getAllFields(integId);
	}

	@RequestMapping(path = "/api/v1/integrate", method = "post", produces = "application/json", accessLevel = AccessLevel.PUBLIC)
	public IntegrationResponse integrate(@RequestBody String payload) throws JSONException, IOException {
		CommonUtil.logOsyncInfo(short_name, "/api/v1/integration :: payload >>>"+payload);
		IntegrationResponse integResponse = new IntegrationResponse();

		AccountInfoEntity accInfoObject;
		IntegrationPropsEntity integInfoObj;
		try {
			ObjectMapper mapper = new ObjectMapper();
			JSONObject payloadJson = new JSONObject(payload);
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

			accInfoObject = mapper.readValue(payload, AccountInfoEntity.class);
			accInfoObject.setRemoteIdentifier(payloadJson.optString("companyId"));
			AccountInfoEntity findByRemoteIdentifier = accountRepo
					.findTopByRemoteIdentifierAndEmail(accInfoObject.getRemoteIdentifier(), accInfoObject.getEmail());

			if (findByRemoteIdentifier == null) {
				accInfoObject = accountRepo.save(accInfoObject);
				CurrentContext.setCurrentContext(accInfoObject.getOsyncId(),
						CurrentContext.getCurrentContext().getToken(),
						CurrentContext.getCurrentContext().getUserAccessLevel());

				AccountUserInfoEntity userInfo = new AccountUserInfoEntity();
				userInfo.setEmail(accInfoObject.getEmail());
				userInfo.setOsyncId(accInfoObject.getOsyncId());
				userInfo = userInfoRepo.insert(userInfo);

				// need to add authorization header
				AuthorizationEntity authorizationEntity = authRepo.create(userInfo.getOsyncId(),
						userInfo.getOsyncUserId());
				integResponse.setHash(authorizationEntity.getToken());
			} else {
				accInfoObject = findByRemoteIdentifier;
			}

			payloadJson.put("osync_id", accInfoObject.getOsyncId());
			payload = payloadJson.toString();
			integInfoObj = mapper.readValue(payload, IntegrationPropsEntity.class);

			List<IntegrationPropsEntity> integs = intPropsRepo.findAllByOsyncId(accInfoObject.getOsyncId());

			if (integs == null || integs.size() == 0) {
				integInfoObj = intPropsRepo.save(integInfoObj);
			} else {
				integInfoObj = integs.get(0);
			}

			integResponse.setId(integInfoObj.getIntegId() + "");
			integResponse.setOsyncId(accInfoObject.getOsyncId() + "");

			ServiceInfoEntity leftServiceAuthObj = serviceRepo.findByServiceId(integInfoObj.getLeftServiceId());

			if (leftServiceAuthObj.getAuthType().equals("oauth")) {
				String leftAuthUrl = constructOAuthUrl(leftServiceAuthObj, integInfoObj, true);
				IntegrationResponse.ServiceDetails leftServiceDetails = integResponse.new ServiceDetails();

				leftServiceDetails.setServiceId(leftServiceAuthObj.getServiceId() + "");
				leftServiceDetails.setServiceName(leftServiceAuthObj.getName());
				leftServiceDetails.setServiceDisplayName(leftServiceAuthObj.getDisplayName());

				IntegrationResponse.AuthDetails leftAuthDetails = integResponse.new AuthDetails();

				leftAuthDetails.setType(leftServiceAuthObj.getAuthType());
				leftAuthDetails.setUrl(leftAuthUrl);

				ServiceAuthInfoEntity byOsyncIdAndServiceIdAndIntegId = serviceAuthInfoRepo
						.findLeftServiceAuthInfo(accInfoObject.getOsyncId(), leftServiceAuthObj.getServiceId());
				if (byOsyncIdAndServiceIdAndIntegId == null) {
					leftAuthDetails.setAuthorized(false);
				} else {
					leftAuthDetails.setAuthorized(true);
					leftAuthDetails.setUserEmail(byOsyncIdAndServiceIdAndIntegId.getUserEmail());
					leftAuthDetails.setUserFullName(byOsyncIdAndServiceIdAndIntegId.getUserFullName());
					leftAuthDetails.setUserUniqueId(byOsyncIdAndServiceIdAndIntegId.getUserUniqueId());
				}

				leftServiceDetails.setAuthDetails(leftAuthDetails);

				integResponse.setLeftDetails(leftServiceDetails);

			}

			ServiceInfoEntity rightServiceAuthObj = serviceRepo.findByServiceId(integInfoObj.getRightServiceId());

			System.out.println(" auth type >>>>>>>>>>>>>>>>>>>>"+rightServiceAuthObj.getAuthType());
			if (rightServiceAuthObj.getAuthType().equals("oauth") || rightServiceAuthObj.getAuthType().equals("apikey")) {
				String rightAuthUrl = constructOAuthUrl(rightServiceAuthObj, integInfoObj, false);
				IntegrationResponse.ServiceDetails rightServiceDetails = integResponse.new ServiceDetails();

				rightServiceDetails.setServiceId(rightServiceAuthObj.getServiceId() + "");
				rightServiceDetails.setServiceName(rightServiceAuthObj.getName());
				rightServiceDetails.setServiceDisplayName(rightServiceAuthObj.getDisplayName());

				IntegrationResponse.AuthDetails rightAuthDetails = integResponse.new AuthDetails();

				rightAuthDetails.setType(rightServiceAuthObj.getAuthType());
				rightAuthDetails.setUrl(rightAuthUrl);

				rightServiceDetails.setAuthDetails(rightAuthDetails);

				ServiceAuthInfoEntity byOsyncIdAndServiceIdAndIntegId = serviceAuthInfoRepo
						.findRightServiceAuthInfo(accInfoObject.getOsyncId(), rightServiceAuthObj.getServiceId());
				if (byOsyncIdAndServiceIdAndIntegId == null) {
					rightAuthDetails.setAuthorized(false);
				} else {
					rightAuthDetails.setAuthorized(true);
					rightAuthDetails.setUserEmail(byOsyncIdAndServiceIdAndIntegId.getUserEmail());
					rightAuthDetails.setUserFullName(byOsyncIdAndServiceIdAndIntegId.getUserFullName());
					rightAuthDetails.setUserUniqueId(byOsyncIdAndServiceIdAndIntegId.getUserUniqueId());
				}

				integResponse.setRightDetails(rightServiceDetails);

			}

			return integResponse;

		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	@RequestMapping(path = "/api/v1/integration/new", method = "post", produces = "application/json", accessLevel = AccessLevel.PUBLIC)
	public IntegrationResponse integrateNew(@RequestBody String payload) throws JSONException, IOException {
		// osyncid, left_service_id , right_service_id
		CommonUtil.logOsyncInfo(short_name, "/api/v1/integration/new :: payload >>>>>>>"+payload);
		IntegrationResponse integResponse = new IntegrationResponse();
		IntegrationPropsEntity integInfoObj;
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

			integInfoObj = mapper.readValue(payload, IntegrationPropsEntity.class);

			List<IntegrationPropsEntity> findByOsyncIdAndLeftServiceIdAndRightServiceId = intPropsRepo
					.findByOsyncIdAndLeftServiceIdAndRightServiceId(integInfoObj.getOsyncId(),
							integInfoObj.getLeftServiceId(), integInfoObj.getRightServiceId());

			boolean updateExistingIntegration = false;

			if (findByOsyncIdAndLeftServiceIdAndRightServiceId != null) {
				for (IntegrationPropsEntity inteProps : findByOsyncIdAndLeftServiceIdAndRightServiceId) {
					if (inteProps.getLeftModuleId() == null || inteProps.getLeftModuleId().isEmpty()
							|| inteProps.getRightModuleId() == null || inteProps.getRightModuleId().isEmpty()) {
						integInfoObj = inteProps;
						updateExistingIntegration = true;
						break;
					}
				}
			}
			if (updateExistingIntegration) {
				integInfoObj = intPropsRepo.update(integInfoObj.getIntegId(), integInfoObj);
			} else {

				integInfoObj = intPropsRepo.save(integInfoObj);
			}

			String osyncId = integInfoObj.getOsyncId() + "";

			integResponse.setId(integInfoObj.getIntegId() + "");
			integResponse.setOsyncId(integInfoObj.getOsyncId() + "");

			ServiceInfoEntity leftServiceAuthObj = serviceRepo.findByServiceId(integInfoObj.getLeftServiceId());

			if (leftServiceAuthObj.getAuthType().equals("oauth")) {
				String leftAuthUrl = constructOAuthUrl(leftServiceAuthObj, integInfoObj, true);
				IntegrationResponse.ServiceDetails leftServiceDetails = integResponse.new ServiceDetails();

				leftServiceDetails.setServiceId(leftServiceAuthObj.getServiceId() + "");
				leftServiceDetails.setServiceName(leftServiceAuthObj.getName());
				leftServiceDetails.setServiceDisplayName(leftServiceAuthObj.getDisplayName());

				IntegrationResponse.AuthDetails leftAuthDetails = integResponse.new AuthDetails();

				leftAuthDetails.setType(leftServiceAuthObj.getAuthType());
				leftAuthDetails.setUrl(leftAuthUrl);

				ServiceAuthInfoEntity byOsyncIdAndServiceIdAndIntegId = serviceAuthInfoRepo
						.findLeftServiceAuthInfo(osyncId, leftServiceAuthObj.getServiceId());
				if (byOsyncIdAndServiceIdAndIntegId == null) {
					leftAuthDetails.setAuthorized(false);
				} else {
					leftAuthDetails.setAuthorized(true);
					leftAuthDetails.setUserEmail(byOsyncIdAndServiceIdAndIntegId.getUserEmail());
					leftAuthDetails.setUserFullName(byOsyncIdAndServiceIdAndIntegId.getUserFullName());
					leftAuthDetails.setUserUniqueId(byOsyncIdAndServiceIdAndIntegId.getUserUniqueId());
				}

				leftServiceDetails.setAuthDetails(leftAuthDetails);

				integResponse.setLeftDetails(leftServiceDetails);

			}

			ServiceInfoEntity rightServiceAuthObj = serviceRepo.findByServiceId(integInfoObj.getRightServiceId());
			System.out.println(" auth type >>>>>>>>>>>>>>>>>>>>"+rightServiceAuthObj.getAuthType());
			if (rightServiceAuthObj.getAuthType().equals("oauth") || rightServiceAuthObj.getAuthType().equals("apikey")) {
				String rightAuthUrl = constructOAuthUrl(rightServiceAuthObj, integInfoObj, false);
				IntegrationResponse.ServiceDetails rightServiceDetails = integResponse.new ServiceDetails();

				rightServiceDetails.setServiceId(rightServiceAuthObj.getServiceId() + "");
				rightServiceDetails.setServiceName(rightServiceAuthObj.getName());
				rightServiceDetails.setServiceDisplayName(rightServiceAuthObj.getDisplayName());

				IntegrationResponse.AuthDetails rightAuthDetails = integResponse.new AuthDetails();

				rightAuthDetails.setType(rightServiceAuthObj.getAuthType());
				rightAuthDetails.setUrl(rightAuthUrl);

				rightServiceDetails.setAuthDetails(rightAuthDetails);

				ServiceAuthInfoEntity byOsyncIdAndServiceIdAndIntegId = serviceAuthInfoRepo
						.findRightServiceAuthInfo(osyncId, rightServiceAuthObj.getServiceId());
				if (byOsyncIdAndServiceIdAndIntegId == null) {
					rightAuthDetails.setAuthorized(false);
				} else {
					rightAuthDetails.setAuthorized(true);
					rightAuthDetails.setUserEmail(byOsyncIdAndServiceIdAndIntegId.getUserEmail());
					rightAuthDetails.setUserFullName(byOsyncIdAndServiceIdAndIntegId.getUserFullName());
					rightAuthDetails.setUserUniqueId(byOsyncIdAndServiceIdAndIntegId.getUserUniqueId());
				}

				integResponse.setRightDetails(rightServiceDetails);

			}

			return integResponse;

		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	private String constructOAuthUrl(ServiceInfoEntity serviceAuthObj, IntegrationPropsEntity intInfoObj,
			boolean isLeft) {
		String url = "";

		String authorizeUrl = serviceAuthObj.getAuthorizeUrl();
		String authScopes = serviceAuthObj.getAuthScopes();
		String clientId = serviceAuthObj.getClientId();

		
		String stateParam = intInfoObj.getOsyncId() + "::" + serviceAuthObj.getServiceId() + "::"
				+ intInfoObj.getIntegId() + "::" + isLeft;
		

		url = authorizeUrl + "?response_type=code&client_id=" + clientId + "&redirect_uri=" + AuthorizerUtil.getRedirectUrl()
				+ "&state=" + stateParam;

		if (authScopes != null && !authScopes.isEmpty()) {
			url += "&scope=" + authScopes;
		}
		url += "&access_type=offline";

		return url;
	}

	@RequestMapping(path = "/api/v1/account/{osync_id}", method = "get", produces = "application/json", accessLevel = AccessLevel.ADMIN)
	public HashMap<String, Object> getIntegrationByOsyncId(@PathVariable("osync_id") String osyncId,
			@RequestParam("left_service_id") String leftServiceId,
			@RequestParam("right_service_id") String rightServiceId) throws Exception {
		
		CommonUtil.logOsyncInfo(short_name, "/api/v1/account/{osync_id} :: osyncID >>>>>>>"+osyncId+" ::: left_service_id >>"+leftServiceId+ "::: right_service_id >>"+rightServiceId);
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("account", accountRepo.findByOsyncId(osyncId));
		map.put("integrations", intPropsRepo.findByOsyncId(osyncId, leftServiceId, rightServiceId));
		ServiceInfoEntity leftService = serviceRepo.findByServiceId(leftServiceId);
		map.put("left_service", leftService);
		ServiceInfoEntity rightService = serviceRepo.findByServiceId(rightServiceId);
		map.put("right_service", rightService);
		map.put("left_modules", getModulesToMap(leftService, leftServiceId, osyncId, true));
		map.put("right_modules", getModulesToMap(rightService, rightServiceId, osyncId, false));
		map.put("integ_status", intStatusRepo.findAllIntStatus(osyncId));
		map.put("sync_report", synclog.findAllStats(osyncId));
		return map;
	}

	private List<ModuleInfoEntity> getModulesToMap(ServiceInfoEntity service, String serviceId, String osyncId, boolean isLeft) throws Exception {
		SyncHandler handler = SyncHandlerRepo.getInstance(service, null, osyncId, null, isLeft);
		return handler.getModules(osyncId, serviceId);
	}

	@RequestMapping(path = "/api/v1/run-sync", method = "get", produces = "application/json", accessLevel = AccessLevel.ADMIN)
	public void runSync(@RequestParam("osync_id") String osyncId, @RequestParam("integ_id") String integId) throws Exception{
		CommonUtil.logOsyncInfo(short_name, "/api/v1/run-sync :: osyncID >>>"+osyncId+" ::::: integ_id >>>"+integId);
		
		List<String> osyncIdList = new ArrayList<String>();
		osyncIdList.add(osyncId);
		ZCCircuitDetails userBackupCircuit = ZCCircuit.getInstance().getCircuitInstance(4344000001214001L);
		org.json.simple.JSONObject execInputJson = new org.json.simple.JSONObject();
		execInputJson.put("forceSyncOsyncId", osyncIdList.toString());
		
		CommonUtil.logOsyncInfo(short_name, "/api/v1/run-sync :: osyncID list sent to Circuits::: "+execInputJson);
		ZCCircuitExecutionDetails circuitExecution = userBackupCircuit.execute("FS_"+CommonUtil.getRandomString(), execInputJson);
		String executionId = circuitExecution.getExecutionId();
		CommonUtil.logOsyncInfo(short_name, "/api/v1/run-sync :: osyncID >>>"+osyncId+" ::::: integ_id >>>"+integId +"executionId >>> "+executionId);
		
		
		//return intService.sync2(osyncId, integId, true );
	}

	@RequestMapping(path = "/api/v1/integration/{integ_id}", method = "delete", produces = "application/json", accessLevel = AccessLevel.ADMIN)
	public IntegrationPropsEntity deleteIntegration(@PathVariable("integ_id") String integId) throws Exception {
		
		CommonUtil.logOsyncInfo(short_name, "/api/v1/integration/{integ_id} :: integId >>>>>>>"+integId);
		
		return intPropsRepo.deleteIntegrationByIntegId(integId);
	}

	@RequestMapping(path = "/api/v1/uninstallAction", method = "delete", produces = "application/json", accessLevel = AccessLevel.ADMIN)
	public boolean deleteAllOsyncInfo() throws Exception {
		
		CommonUtil.logOsyncInfo(short_name, "/api/v1/uninstallAction");
		
		// String osyncId = "bc2f2709-7718-4ae6-a117-87548c196146";
		String osyncId = OsyncDB.get(CurrentContext.getCurrentOsyncId()).toString();
		
		if (osyncId != null) {
			OsyncDB osp = OsyncDB.get(osyncId);

			List<IntegrationPropsEntity> integrationPropsEntity = intPropsRepo.findEntity(osyncId);
			for (IntegrationPropsEntity integraPropsEntity : integrationPropsEntity) {
				CommonUtil.logOsyncInfo(short_name, "above to delete current OsynId -integraPropsEntity");
				OsyncDB.get(integraPropsEntity.getOsyncId()).deleteByEntity(integraPropsEntity);
			}

			List<AccountInfoEntity> accountInfoEntity = accountRepo.findEntity(osyncId);
			for (AccountInfoEntity accountEntity : accountInfoEntity) {
				CommonUtil.logOsyncInfo(short_name, "above to delete current OsynId -accountEntity");
				OsyncDB.get(accountEntity.getOsyncId()).deleteByEntity(accountEntity);
			}

			List<ServiceAuthInfoEntity> serviceAuthInfoEntity = serviceAuthInfoRepo.findEntity(osyncId);

			for (ServiceAuthInfoEntity serviceAuthEntity : serviceAuthInfoEntity) {
				CommonUtil.logOsyncInfo(short_name, "above to delete current OsynId -serviceAuthInfoEntity");
				OsyncDB.get(serviceAuthEntity.getOsyncId()).deleteByEntity(serviceAuthEntity);
			}

			List<FieldMapEntity> fieldMapEntity = fieldMapRepo.findEntity(osyncId);
			for (FieldMapEntity FMentity : fieldMapEntity) {
				CommonUtil.logOsyncInfo(short_name, "above to delete current OsynId -FieldMapEntity");
				OsyncDB.get(FMentity.getOsyncId()).deleteByEntity(FMentity);
			}

			List<UniqueValuesMapEntity> uniqueValuesMapEntity = uvRepo.findEntity(osyncId);
			for (UniqueValuesMapEntity UVentity : uniqueValuesMapEntity) {
				CommonUtil.logOsyncInfo(short_name, "above to delete current OsynId -UniqueValuesMapEntity");
				OsyncDB.get(UVentity.getOsyncId()).deleteByEntity(UVentity);
			}

			return true;
		}
		return false;

	}
	
	@RequestMapping(path = "/api/v1/sync-props", method = "get", produces = "application/json", accessLevel = AccessLevel.ADMIN)
	public SyncPropertiesResponse getAllSyncProperties(@RequestParam("osync_id") String osyncId, @RequestParam("integ_id") String integId) throws Exception {
		
		CommonUtil.logOsyncInfo(short_name, "/api/v1/sync-props  ::: osync_id >"+ osyncId+":: integ_id >>"+integId);
		
		SyncProps syncProp = new SyncProps(osyncId,integId);
		return new SyncPropertiesResponse(syncProp);
	}
	
}
