package xyz.oapps.osync.invoker;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

import xyz.oapps.osync.OsyncException;
import xyz.oapps.osync.api.OsyncConstants.IntegrationStatus;
import xyz.oapps.osync.entity.IntegrationPropsEntity;
import xyz.oapps.osync.entity.IntegrationStatusEntity;
import xyz.oapps.osync.entity.ServiceAuthInfoEntity;
import xyz.oapps.osync.entity.ServiceInfoEntity;
import xyz.oapps.osync.repo.IntegrationPropsRepository;
import xyz.oapps.osync.repo.IntegrationStatusRepository;
import xyz.oapps.osync.repo.ServiceAuthInfoRepository;
import xyz.oapps.osync.repo.ServiceInfoRepository;
import xyz.oapps.osync.util.AuthorizerUtil;
import xyz.oapps.osync.util.CommonUtil;
import xyz.oapps.osync.util.ErrorEmailSender;

public class Invoker {
	private static final Logger log = Logger.getLogger(Invoker.class.getName());
	private static final String short_name = "in_vr";
	ServiceInfoRepository serviceInfoRepo = new ServiceInfoRepository();

	ServiceAuthInfoRepository serviceAuthInfoRepo = new ServiceAuthInfoRepository();

	IntegrationPropsRepository intPropsRepo = new IntegrationPropsRepository();
	
	
	private final int MAXIMUM_TRIES=3;
	private Client client;
	private String serviceId;
	private boolean isLeft;
	private boolean isAdminOperation;
	private String osyncId;
	private String authType;

	public Invoker(String service, boolean isServiceName, boolean isLeft , boolean isAdminOperation , String osyncId) throws Exception {
		super();
		this.client = Client.create();
		this.isAdminOperation = isAdminOperation;
		this.osyncId = osyncId;
		if(isServiceName) {
			ServiceInfoEntity findByName = serviceInfoRepo.findByName(service);
			this.serviceId = findByName.getServiceId();
			this.authType = findByName.getAuthType();
		} else {
			this.serviceId = service;
			this.authType =  serviceInfoRepo.findByServiceId(this.serviceId).getAuthType();
		}
		this.isLeft = isLeft;
	}
	
	public String get(String targetUrl ,JSONObject headerJson,JSONObject queryParams, JSONObject payLoadJson) throws Exception{
		return get(targetUrl, headerJson, queryParams, payLoadJson,0);
	}
	
	public String get(String targetUrl ,JSONObject headerJson,JSONObject queryParams, JSONObject payLoadJson,int tryLimit) throws Exception{

		CommonUtil.logOsyncInfo(short_name,"get :: targetUrl >>>>>>"+targetUrl +" :: tryLimit >>>"+tryLimit +":::headerJson >> "+headerJson+"::  queryParams >>"+queryParams+":: payLoadJson >>"+payLoadJson);
		
		String response = "";
		WebResource webRes = this.client.resource(targetUrl);
		ServiceAuthInfoEntity serviceAuth = null;

		if(queryParams != null && queryParams.length() > 0) {
			Iterator<String> keys = queryParams.keys();
			while(keys.hasNext()) {
				String key = keys.next();
				String value = queryParams.get(key).toString();
				webRes = webRes.queryParam(key, value);
			}
		}

		Builder requestBuilder = webRes.getRequestBuilder();
		if(headerJson != null && headerJson.length() > 0) {
			Iterator<String> keys = headerJson.keys();
			while(keys.hasNext()) {
				String key = keys.next();
				String value = headerJson.get(key).toString();
				requestBuilder = requestBuilder.header(key, value);
			}
		}


		serviceAuth = setAuthorization(serviceAuth, requestBuilder, this.isAdminOperation, this.osyncId);

		ClientResponse clientResponse = payLoadJson == null ? requestBuilder.get(ClientResponse.class)
				: requestBuilder.method("GET", ClientResponse.class, payLoadJson.toString());


		if(clientResponse.getStatus() == 401 && serviceAuth != null) {
			response = refreshTokenProcess(targetUrl, headerJson, queryParams, payLoadJson, tryLimit, response, serviceAuth,
					clientResponse,"get",null);
		} else if(serviceAuth == null) {
			throw new OsyncException(OsyncException.Code.INVOKER_AUTH_FAILED);
		} else {
			response = clientResponse.getEntity(String.class);
		}
		CommonUtil.logOsyncInfo(short_name,"get :: response >>>"+response +":: status >>>>"+clientResponse.getStatus());
		return response;
	}

	private String refreshTokenProcess(String targetUrl, JSONObject headerJson, JSONObject queryParams, JSONObject payLoadJson,
			int tryLimit, String response, ServiceAuthInfoEntity serviceAuth, ClientResponse clientResponse , String method,JSONArray queryParamsArray)
			throws Exception, JSONException {
		
		CommonUtil.logOsyncInfo(short_name,"refreshTokenProcess :: targetUrl >>>>>>"+targetUrl +" :: tryLimit >>>"+tryLimit +":::headerJson >> "+headerJson+"::  queryParams >>"+queryParams+":: payLoadJson >>"+payLoadJson);
		
		tryLimit++;
		CommonUtil.logOsyncInfo(short_name,"401 error occured.." + clientResponse.getEntity(String.class));
		if(tryLimit <= MAXIMUM_TRIES) {
			ServiceInfoEntity serviceInfo = serviceInfoRepo.findByServiceId(serviceAuth.getServiceId());

			String refreshTokenResp = AuthorizerUtil.getAccessTokenUsingRefreshToken(serviceInfo, serviceAuth,this.isAdminOperation,this.osyncId);
			JSONObject refreshTokenObj = new JSONObject(refreshTokenResp);
			if (refreshTokenObj.has("access_token")) {
				serviceAuth.setAccessToken(refreshTokenObj.optString("access_token"));
				serviceAuthInfoRepo.save(serviceAuth);
			}

			if(method.equals("get")) {
				response = get(targetUrl, headerJson, queryParams, payLoadJson,tryLimit);
			} else if(method.equals("getRequestWithPayLoad")) {
				response = getRequestWithPayLoad(targetUrl, headerJson, queryParamsArray, payLoadJson,tryLimit);
			} else if(method.equals("delete")) {
				response = delete(targetUrl, headerJson, queryParams, payLoadJson,tryLimit);
			} else if(method.equals("put") || method.equals("patch") || method.equals("post")) {
				response = postOrPut(targetUrl, headerJson, queryParams, payLoadJson, method,tryLimit);
			}
		} else if(tryLimit > MAXIMUM_TRIES) {
			try {
				serviceAuthInfoRepo.deleteByEntity(serviceAuth);
			} catch (Exception e) {
				log.log(Level.WARNING, "max retry failed for token renewal", e);
			}
			throw new OsyncException(OsyncException.Code.INVOKER_AUTH_FAILED);
		}
		return response;
	}

	public String getRequestWithPayLoad(String targetUrl ,JSONObject headerJson,JSONArray queryParamsArray, JSONObject payLoadJson,int tryLimit) throws Exception{
		CommonUtil.logOsyncInfo(short_name,"getRequestWithPayLoad :: targetUrl >>>>>>"+targetUrl +" :: tryLimit >>>"+tryLimit +":::headerJson >> "+headerJson+"::  queryParamsArray >>"+queryParamsArray+":: payLoadJson >>"+payLoadJson);
		
		String response = "";
		WebResource webRes = this.client.resource(targetUrl);
		ServiceAuthInfoEntity serviceAuth = null;

		if(queryParamsArray != null) {
			int length = queryParamsArray.length() ;

			for(int i=0;i<length; i++) {
				JSONObject paramJson = queryParamsArray.getJSONObject(i);
				if(paramJson.has("param_key") && paramJson.has("param_value")) {
					String key = paramJson.getString("param_key");
					String value = paramJson.getString("param_value");
					webRes = webRes.queryParam(key, value);
				}
			}

		}

		Builder requestBuilder = webRes.getRequestBuilder();
		if(headerJson != null && headerJson.length() > 0) {
			Iterator<String> keys = headerJson.keys();
			while(keys.hasNext()) {
				String key = keys.next();
				String value = headerJson.get(key).toString();
				requestBuilder = requestBuilder.header(key, value);
			}
		}

		serviceAuth = setAuthorization(serviceAuth, requestBuilder,this.isAdminOperation,this.osyncId);

		ClientResponse clientResponse = payLoadJson == null ? requestBuilder.get(ClientResponse.class)
				: requestBuilder.method("GET", ClientResponse.class, payLoadJson.toString());
		
		
		
		if(clientResponse.getStatus() == 401 && serviceAuth != null) {
			response = refreshTokenProcess(targetUrl, headerJson, null, payLoadJson, tryLimit, response, serviceAuth,
					clientResponse,"getRequestWithPayLoad",queryParamsArray);
		} else if(serviceAuth == null) {
			throw new OsyncException(OsyncException.Code.INVOKER_AUTH_FAILED);
		} else {
			response = clientResponse.getEntity(String.class);
		}
		CommonUtil.logOsyncInfo(short_name,"getRequestWithPayLoad :: final response >>>"+response +":: status >>>>"+clientResponse.getStatus());
		return response;
	}

	private ServiceAuthInfoEntity setAuthorization(ServiceAuthInfoEntity serviceAuth, Builder requestBuilder,boolean isAdminOperation , String adminOsyncId)
			throws Exception {
		if(this.osyncId != null && this.serviceId != null) {
			
			if(isAdminOperation) {
				osyncId = adminOsyncId;
			} 
			
			serviceAuth = isLeft ? serviceAuthInfoRepo.findLeftServiceAuthInfo(osyncId, this.serviceId)
					: serviceAuthInfoRepo.findRightServiceAuthInfo(osyncId, this.serviceId);
			
			if(serviceAuth != null && serviceAuth.getAccessToken() != null && !serviceAuth.getAccessToken().isEmpty()) {
				
				requestBuilder = requestBuilder.header("Authorization", "Bearer "+serviceAuth.getAccessToken());

			}
		}
		return serviceAuth;
	}


	public String put(String targetUrl ,JSONObject headerJson,JSONObject queryParams,JSONObject payLoadJson) throws Exception{
		return postOrPut(targetUrl, headerJson, queryParams, payLoadJson, "put",0);
	}

	public String post(String targetUrl ,JSONObject headerJson,JSONObject queryParams,JSONObject payLoadJson) throws Exception{
		return postOrPut(targetUrl, headerJson, queryParams, payLoadJson, "post",0);
	}
	public String patch(String targetUrl ,JSONObject headerJson,JSONObject queryParams,JSONObject payLoadJson) throws Exception{
		return postOrPut(targetUrl, headerJson, queryParams, payLoadJson, "patch",0);
	}

	private String postOrPut(String targetUrl ,JSONObject headerJson,JSONObject queryParams,JSONObject payLoadJson, String method,int tryLimit) throws Exception{
		
		CommonUtil.logOsyncInfo(short_name,"postOrPut :: targetUrl >>>>>>"+targetUrl +" :: tryLimit >>>"+tryLimit +":::headerJson >> "+headerJson+"::  queryParams >>"+queryParams+":: payLoadJson >>"+payLoadJson);
		WebResource webRes = this.client.resource(targetUrl);
		ServiceAuthInfoEntity serviceAuth = null;

		String response = "";

		if(queryParams != null && queryParams.length() > 0) {
			Iterator<String> keys = queryParams.keys();
			while(keys.hasNext()) {
				String key = keys.next();
				String value = queryParams.getString(key);
				webRes = webRes.queryParam(key, value);
			}
		}

		Builder requestBuilder = webRes.getRequestBuilder();
		if(headerJson != null && headerJson.length() > 0) {
			Iterator<String> keys = headerJson.keys();
			while(keys.hasNext()) {
				String key = keys.next();
				String value = headerJson.getString(key);
				requestBuilder = requestBuilder.header(key, value);
			}
		}
		serviceAuth = setAuthorization(serviceAuth, requestBuilder,this.isAdminOperation,this.osyncId);

		String payload = payLoadJson == null ? "" : payLoadJson.toString();
		
		if(!"".equals(payload)) {
			requestBuilder.header("Content-Type", "application/json");
		}

		ClientResponse clientResponse = null;
		if("put".equalsIgnoreCase(method)) {
			clientResponse = requestBuilder.put(ClientResponse.class, payload);
		} else {
			if("patch".equalsIgnoreCase(method)) {
				requestBuilder = requestBuilder.header("X-HTTP-Method-Override", "PATCH");
			}
			clientResponse = requestBuilder.post(ClientResponse.class, payload);
		}
		
		if(clientResponse.getStatus() == 401 && serviceAuth != null) {
			response = refreshTokenProcess(targetUrl, headerJson, queryParams, payLoadJson, tryLimit, response, serviceAuth,
					clientResponse,method,null);
		} else if(serviceAuth == null) {
			throw new OsyncException(OsyncException.Code.INVOKER_AUTH_FAILED);
		} else {
			response = clientResponse.getEntity(String.class);
		}
		CommonUtil.logOsyncInfo(short_name,"postOrPut :: response >>>"+response +":: status >>>>"+clientResponse.getStatus());
		
		return response;
	}


	public String delete(String targetUrl ,JSONObject headerJson,JSONObject queryParams,JSONObject payLoadJson,int tryLimit) throws Exception{
		
		CommonUtil.logOsyncInfo(short_name,"delete :: targetUrl >>>>>>"+targetUrl +" :: tryLimit >>>"+tryLimit +":::headerJson >> "+headerJson+"::  queryParams >>"+queryParams+":: payLoadJson >>"+payLoadJson);
		
		WebResource webRes = this.client.resource(targetUrl);
		ServiceAuthInfoEntity serviceAuth = null;

		String response = "";

		if(queryParams != null && queryParams.length() > 0) {
			Iterator<String> keys = queryParams.keys();
			while(keys.hasNext()) {
				String key = keys.next();
				String value = queryParams.getString(key);
				webRes = webRes.queryParam(key, value);
			}
		}

		Builder requestBuilder = webRes.getRequestBuilder();
		if(headerJson != null && headerJson.length() > 0) {
			Iterator<String> keys = headerJson.keys();
			while(keys.hasNext()) {
				String key = keys.next();
				String value = headerJson.getString(key);
				requestBuilder = requestBuilder.header(key, value);
			}
		}
		serviceAuth = setAuthorization(serviceAuth, requestBuilder,this.isAdminOperation,this.osyncId);

		String payload = payLoadJson == null ? "" : payLoadJson.toString();
		if(!"".equals(payload)) {
			requestBuilder.header("Content-Type", "application/json");
		}

		ClientResponse clientResponse = null;

		if(serviceAuth != null) {
			clientResponse = requestBuilder.delete(ClientResponse.class, payload);
			
			CommonUtil.logOsyncInfo(short_name,"delete :: response >>>"+response +":: status >>>>"+clientResponse.getStatus());
			
			if(clientResponse.getStatus() == 401 && serviceAuth != null) {
				response = refreshTokenProcess(targetUrl, headerJson, queryParams, payLoadJson, tryLimit, response, serviceAuth,
						clientResponse,"delete",null);
			} else if(serviceAuth == null) {
				throw new OsyncException(OsyncException.Code.INVOKER_AUTH_FAILED);
			} else {
				response = clientResponse.getEntity(String.class);
			}
			
			
			CommonUtil.logOsyncInfo(short_name,"response >>>>>>"+response);
		}
		return response;
	}
}
