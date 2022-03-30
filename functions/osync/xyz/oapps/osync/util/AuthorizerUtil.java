package xyz.oapps.osync.util;

import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import xyz.oapps.osync.db.OsyncConfig;
import xyz.oapps.osync.entity.ServiceAuthInfoEntity;
import xyz.oapps.osync.entity.ServiceInfoEntity;
import xyz.oapps.osync.invoker.Invoker;
import xyz.oapps.osync.repo.ServiceInfoRepository;

public class AuthorizerUtil {

	public static final String redirectUrl = "https://osync-api.oapps.xyz/server/osync/api/v1/redirect";
	public static String getRedirectUrl() {
		return OsyncConfig.getConfig("osync.redirect.url", redirectUrl);
	}


	public static String getAccessToken(AuthorizeParams authParams , ServiceInfoRepository serviceInfoRepo) throws Exception {


		Client client = Client.create();
		//		String stateParam = intInfoObj.getOsyncId()+"::"+serviceAuthObj.getServiceId()+"::"+intInfoObj.getIntegId();

		System.out.println("respJson >>>>>>>>>>>>>"+authParams.getState());
		String state = authParams.getState();

		String[] decryptedArray = state.split("::");

		String serviceId = decryptedArray[1];


		ServiceInfoEntity serviceInfo = serviceInfoRepo.findByServiceId(serviceId);

		String tokenUrl = serviceInfo.getTokenUrl();
		String clientId = serviceInfo.getClientId();
		String clientSecret = serviceInfo.getClientSecret();

		WebResource webRes = client.resource(tokenUrl);

		webRes = webRes.queryParam("client_id", clientId);
		webRes = webRes.queryParam("client_secret", clientSecret);
		webRes = webRes.queryParam("redirect_uri", getRedirectUrl());

		webRes = webRes.queryParam("code", authParams.getCode());
		webRes = webRes.queryParam("state",  authParams.getState());
		if(authParams.getLocation() != null) {
			webRes = webRes.queryParam("location",  authParams.getLocation());
		}
		if(authParams.getContext() != null) {
			webRes = webRes.queryParam("context",  authParams.getContext());
		}
		if(authParams.getScope() != null) {
			webRes = webRes.queryParam("scope",  authParams.getScope());
		} else {
			if( serviceInfo.getAuthScopes() != null &&  serviceInfo.getAuthScopes().isEmpty()) {
				webRes = webRes.queryParam("scope",  serviceInfo.getAuthScopes());
			}
		}
		webRes = webRes.queryParam("grant_type",  "authorization_code");
		webRes = webRes.queryParam("accounts-server",  "https://accounts.zoho.com");

		ClientResponse clientResponse = webRes.post(ClientResponse.class);
		String authResponse = clientResponse.getEntity(String.class);

		System.out.println("respJson >>>>>>>>>>>>>"+authResponse);

		return authResponse;

	}

	public static String getAccessTokenUsingRefreshToken(ServiceInfoEntity serviceInfo , ServiceAuthInfoEntity serviceAuthInfo , boolean isAdminOperation , String osyncId) throws Exception {
		Invoker invokerObj = new Invoker(serviceAuthInfo.getServiceId(), false, serviceAuthInfo.isLeftService(),isAdminOperation,osyncId);

		JSONObject queryParamsJson = new JSONObject();

		queryParamsJson.put("client_id", serviceInfo.getClientId());
		queryParamsJson.put("client_secret", serviceInfo.getClientSecret());
		queryParamsJson.put("grant_type", "refresh_token");
		queryParamsJson.put("refresh_token", serviceAuthInfo.getRefreshToken());

		return invokerObj.post(serviceInfo.getRefreshTokenUrl(), null, queryParamsJson, null);

	}


}
