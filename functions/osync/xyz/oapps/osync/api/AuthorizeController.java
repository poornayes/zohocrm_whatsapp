package xyz.oapps.osync.api;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jooq.tools.json.JSONObject;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import xyz.oapps.osync.CurrentContext;
import xyz.oapps.osync.SyncHandlerRepo;
import xyz.oapps.osync.User;
import xyz.oapps.osync.annotation.RequestMapping;
import xyz.oapps.osync.annotation.RequestObject;
import xyz.oapps.osync.annotation.RequestParam;
import xyz.oapps.osync.api.OsyncConstants.AccessLevel;
import xyz.oapps.osync.entity.ServiceAuthInfoEntity;
import xyz.oapps.osync.entity.ServiceInfoEntity;
import xyz.oapps.osync.repo.ServiceAuthInfoRepository;
import xyz.oapps.osync.repo.ServiceInfoRepository;
import xyz.oapps.osync.util.AuthorizeParams;
import xyz.oapps.osync.util.AuthorizerUtil;

public class AuthorizeController {

	private static Logger log = Logger.getLogger(AuthorizeController.class.getName());
	ServiceInfoRepository serviceRepo = new ServiceInfoRepository();
	ServiceAuthInfoRepository serviceAuthInfoRepo = new ServiceAuthInfoRepository();

	ObjectMapper mapper = new ObjectMapper();


	@RequestMapping(path = "/api/v1/redirect", method = "get", produces = "text/html", accessLevel = AccessLevel.PUBLIC)

	public String redirectHandler(@RequestObject AuthorizeParams authParams) {
		String response = "";
		
		JSONObject succesJson = new JSONObject();
		try {

			log.log(Level.INFO," authParams redirectHandler>>>>>>"+authParams);
			log.log(Level.INFO," authParams getCode redirectHandler>>>>>>"+authParams.getCode());
			log.log(Level.INFO," authParams  redirectHandler>>>>>>"+authParams.toString());
			
			String sendCodeToAuthorizationService = AuthorizerUtil.getAccessToken(authParams ,serviceRepo);

			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			AuthorizeParams newAuthParamObj = mapper.readValue(sendCodeToAuthorizationService, AuthorizeParams.class);

			ServiceAuthInfoEntity serviceAuth = saveToken(newAuthParamObj, authParams.getState());
			
			JSONObject respJson = new JSONObject();
			if(serviceAuth != null) {
				respJson.put("osyncId", serviceAuth.getOsyncId());
				respJson.put("serviceId", serviceAuth.getServiceId());
				respJson.put("integId", serviceAuth.getIntegId());
				respJson.put("userEmail", serviceAuth.getUserEmail());
				
				succesJson.put("status", true);
				succesJson.put("data", respJson);
			} else {
				succesJson.put("status", false);
				succesJson.put("data", respJson);
			}
			
			
			response = succesJson.toString();
			log.log(Level.INFO," response redirectHandler>>>>>>"+response);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		String returnScript  = "<script>var openerWindow = window.opener;openerWindow.postMessage(JSON.stringify("+response+"), '*');window.close();</script>";
		String returnScript  = "<script>var openerWindow = window.opener;openerWindow.postMessage(\'"+response+"\', '*');window.close();</script>";
		log.log(Level.INFO," returnScript redirectHandler>>>>>>"+returnScript);
		return returnScript;
	}
	
	@RequestMapping(path = "/api/v1/saveApiKey", method = "post", produces = "application/json", accessLevel = AccessLevel.ADMIN)

	public String saveApiKey(@RequestObject AuthorizeParams authParams) {
		String response = "";
		
		JSONObject succesJson = new JSONObject();
		JSONObject respJson = new JSONObject();
		try {

			log.log(Level.INFO," authParams  redirectHandler>>>>>>"+authParams.toString());
			log.log(Level.INFO," authParams  state>>>>>>"+authParams.getState());
			log.log(Level.INFO," authParams  access_Token>>>>>>"+authParams.getAccess_token());
			
			ServiceAuthInfoEntity serviceAuth = saveToken(authParams, authParams.getState());
			
			if(serviceAuth != null) {
				respJson.put("osyncId", serviceAuth.getOsyncId());
				respJson.put("serviceId", serviceAuth.getServiceId());
				respJson.put("integId", serviceAuth.getIntegId());
				respJson.put("userEmail", serviceAuth.getUserEmail());
				
				succesJson.put("status", true);
				succesJson.put("data", respJson);
			} else {
				succesJson.put("status", false);
				succesJson.put("data", respJson);
			}
			
			
			response = succesJson.toString();
			log.log(Level.INFO," response redirectHandler>>>>>>"+response);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//return respJson;
		return response;
	}

//	String returnScript  = "<script>var openerWindow = window.opener;openerWindow.postMessage(JSON.stringify({status : true, authType : 'oauth'}), '*');window.close();</script>";
	
	public ServiceAuthInfoEntity saveToken(AuthorizeParams authParams, String state) throws Exception {
		log.log(Level.INFO," authParams ServiceAuthInfoEntity >>>>>>"+authParams.toString());
		String[] decryptedArray = state.split("::");

		String osyncId = decryptedArray[0];
		CurrentContext.setCurrentContext(osyncId);
		String serviceId = decryptedArray[1];
		String integId = decryptedArray[2];
		boolean isLeft = "true".equalsIgnoreCase(decryptedArray[3]);
		ServiceAuthInfoEntity entityObj = new ServiceAuthInfoEntity();

		ServiceAuthInfoEntity findTopByOsyncIdAndServiceId = isLeft
				? serviceAuthInfoRepo.findLeftServiceAuthInfo(osyncId, serviceId)
				: serviceAuthInfoRepo.findRightServiceAuthInfo(osyncId, serviceId);
		if(findTopByOsyncIdAndServiceId != null) {
			entityObj.setAuthId(findTopByOsyncIdAndServiceId.getAuthId());
		}
		entityObj.setAccessToken(authParams.getAccess_token());
		entityObj.setOsyncId(osyncId);
		entityObj.setIntegId(integId);
		entityObj.setLeftService(isLeft);
		entityObj.setRefreshToken(authParams.getRefresh_token());
		entityObj.setTokenType("org");
		entityObj.setServiceId(serviceId);
		
		ServiceAuthInfoEntity serviceAuthInfoObj = serviceAuthInfoRepo.save(entityObj);
		
		ServiceInfoEntity serviceInfo = serviceRepo.findByServiceId(serviceId);
		try {
			User currentUser = SyncHandlerRepo.getInstance(serviceInfo, null, osyncId, integId, isLeft).getCurrentUser();
			
			log.log(Level.INFO," serviceInfo changes currentUser >>>>>>"+currentUser.toString());
			if(currentUser != null) {
				
				serviceAuthInfoObj.setUserEmail(currentUser.getEmail());
				serviceAuthInfoObj.setUserUniqueId(currentUser.getUniqueUserId());
				serviceAuthInfoObj.setUserFullName(currentUser.getFullName());
				
				serviceAuthInfoObj = serviceAuthInfoRepo.update(serviceAuthInfoObj.getAuthId(),serviceAuthInfoObj);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.log(Level.INFO," ERROR_HAPPENED while getting currentUser details >>>>>>");
		}
		return serviceAuthInfoObj;

	}

	@RequestMapping(path = "/api/v1/revoke", method = "delete", accessLevel = AccessLevel.ADMIN)
	public boolean revoke(@RequestParam("service_id") String serviceId, @RequestParam("integ_id") String integId,
			@RequestParam("left_service") boolean isLeft) throws Exception {
		if(serviceId != null && integId != null ) {
			System.out.println("serviceId >>>>>>>"+serviceId);
			System.out.println("integId ------>"+integId );
			serviceAuthInfoRepo.delete(serviceId, integId, isLeft);
		}
		return true;
	}
}