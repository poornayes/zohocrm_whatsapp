import static xyz.oapps.osync.api.RequestController.mapper;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.catalyst.advanced.CatalystAdvancedIOHandler;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;


import xyz.oapps.osync.CurrentContext;
import xyz.oapps.osync.OsyncException;
import xyz.oapps.osync.OsyncLogHandler;
import xyz.oapps.osync.annotation.PathVariable;
import xyz.oapps.osync.annotation.RequestBody;
import xyz.oapps.osync.annotation.RequestMapping;
import xyz.oapps.osync.annotation.RequestObject;
import xyz.oapps.osync.annotation.RequestParam;
import xyz.oapps.osync.api.AdminController;
import xyz.oapps.osync.api.RequestController;
import xyz.oapps.osync.entity.AccountInfoEntity;

public class OsyncRequestHandler implements CatalystAdvancedIOHandler {

	private static final OsyncLogHandler LOG_HANDLER = new OsyncLogHandler();
	private static boolean addedLogHandler = false; 

	private static final Logger LOGGER = Logger.getLogger(OsyncRequestHandler.class.getName());

	@Override
	public void runner(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			LOG_HANDLER.setCurrentRequestId(RequestController.getUUID());
			Logger rootLogger = LogManager.getLogManager().getLogger("");
			if(!addedLogHandler) {
				rootLogger.addHandler(LOG_HANDLER);
				addedLogHandler = true;
			}
			debugRequest(request);
			
			
			LOG_HANDLER.setCurrentRequest(request);
			LOG_HANDLER.setCurrentResponse(response);
			LOG_HANDLER.publishRequest();
			((HttpServletResponse) response).addHeader("Access-Control-Allow-Origin", "*");
			((HttpServletResponse) response).addHeader("Access-Control-Allow-Methods",
					"GET, OPTIONS, HEAD, PUT, POST,DELETE");
			((HttpServletResponse) response).addHeader("Access-Control-Allow-Headers", "*");
			((HttpServletResponse) response).addHeader("X-Frame-Options", "ALLOW-FROM crm.zoho.com");

			// For HTTP OPTIONS verb/method reply with ACCEPTED status code -- per CORS
			// handshake
			if (request.getMethod().equals("OPTIONS")) {
				response.setStatus(HttpServletResponse.SC_ACCEPTED);
				return;
			}

			String moduleName = (String) request.getParameter("module");
			String method = request.getMethod().toLowerCase();
			LOGGER.log(Level.INFO,
					"Invoked name : " + moduleName + ", method: " + method + ", path: " + request.getRequestURI());
			response.setStatus(200);

			Object ctrlObject = RequestController.getController(moduleName, request.getRequestURI());
			if (ctrlObject == null) {
				response.getWriter().print("{ \"code\" : \"Not found\" }");
				response.setStatus(404);
				return;
			}
			boolean matchFound = false;
			Method[] methods = ctrlObject.getClass().getMethods();
			for (Method method2 : methods) {
				RequestMapping[] requestMappings = method2.getDeclaredAnnotationsByType(RequestMapping.class);

				if (requestMappings.length > 0) {
					RequestMapping reqMap = requestMappings[0];
					if (reqMap.method().toLowerCase().equals(method)
							&& isUrlMatches(request, reqMap.path(), request.getRequestURI())) {
						matchFound = true;
						boolean isAllowed = RequestController.authenticate(request, response, method2, reqMap);
						
						System.out.println("isAllowed >>>>>>>>"+isAllowed);
						if (!isAllowed && !moduleName.equals("tool") && !moduleName.equals("cron")) {
							response.getWriter().print("{ \"code\" : \"Not authorized\" }");
							response.setStatus(401);
							return;
						}
						Parameter[] parameters = method2.getParameters();
						Object[] args = new Object[method2.getAnnotatedParameterTypes().length];
						int i = 0;
						HashMap<String, String> map = (HashMap<String, String>) request
								.getAttribute("path_variables_param_map");
						for (Parameter parameter : parameters) {
							RequestBody rb = parameter.getDeclaredAnnotation(RequestBody.class);
							RequestParam rp = parameter.getDeclaredAnnotation(RequestParam.class);
							PathVariable pv = parameter.getDeclaredAnnotation(PathVariable.class);
							RequestObject ro = parameter.getDeclaredAnnotation(RequestObject.class);
							if (rb != null) {
								String body = getRequestBody(request, method);
								args[i] = cast(body, parameter.getType());
							} else if (rp != null) {
								args[i] = cast(request.getParameter(rp.value()), parameter.getType());
							} else if (pv != null) {
								args[i] = cast(map.get(pv.value()), parameter.getType());
							} else if (ro != null) {
								String body = getRequestBody(request, method);
								if(body != null && !body.isEmpty()) {
									args[i] = cast(body, parameter.getType());
								} else {
									args[i] = cast(getParamMap(request), parameter.getType());
								}
							} else {
								args[i] = null;
							}
							i++;
						}
						Object result = null;
						try {
							result = method2.invoke(ctrlObject, args);
						}
						catch(Exception e) {
							if(e.getCause() instanceof OsyncException) {
								OsyncException oe = (OsyncException) e.getCause();
								result = oe.getError();
							} else {
								LOGGER.log(Level.SEVERE, "Exception in OsyncRequestHandler", e);
								LOGGER.log(Level.SEVERE, "Exception in OsyncRequestHandler", e.getCause());
								HashMap<String, String> map1 = new HashMap<String, String>();
								map1.put("error", e.getMessage());
								map1.put("code", "500");
								result = map1;
							}
						}
						if (result == null) {
							HashMap<String, String> map1 = new HashMap<String, String>();
							map1.put("error", "Null response");
							map1.put("code", "Null response");
							result = map1;
						}
						String responseStr = mapper().writeValueAsString(result);
						response.setContentType(reqMap.produces());
						response.getWriter().write(responseStr);
						break;
					}
				}
			}

			if (matchFound == false) {
				HashMap<String, String> map1 = new HashMap<String, String>();
				map1.put("module", moduleName);
				map1.put("http_method", method);
				map1.put("request_uri", request.getRequestURI());
				map1.put("error", "Implementation not found");
				response.setContentType("application/json");
				String responseStr = mapper().writeValueAsString(map1);
				response.getWriter().write(responseStr);
			}

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exception in OsyncRequestHandler", e);
			LOGGER.log(Level.SEVERE, "Exception in OsyncRequestHandler", e.getCause());
			response.setStatus(500);
		} finally {
			try { CurrentContext.clear(); } catch(Exception e) {}
			try { LOG_HANDLER.clear(); } catch(Exception e) {}
		}
		// response.getWriter().write("Hello From OsyncRequestHandler.java");
	}

	private void debugRequest(HttpServletRequest request) {
		if (request.getParameter("debugreq") != null) {
			Enumeration<String> attributeNames = request.getAttributeNames();
			while (attributeNames.hasMoreElements()) {
				String nextElement = attributeNames.nextElement();
				System.out.println("Attributes:::" + nextElement + ":::" + request.getAttribute(nextElement));
			}

			Enumeration<String> params = request.getParameterNames();
			while (params.hasMoreElements()) {
				String nextElement = params.nextElement();
				System.out.println("Params:::" + nextElement + ":::" + request.getParameter(nextElement));
			}

			Enumeration<String> headers = request.getHeaderNames();
			while (headers.hasMoreElements()) {
				String nextElement = headers.nextElement();
				System.out.println("Headers:::" + nextElement + ":::" + request.getHeader(nextElement));
			}
		}
	}

	private static boolean isUrlMatches(HttpServletRequest request, String methodPath, String reqPath) {
		methodPath = removeTrailingSlash(methodPath);
		reqPath = removeTrailingSlash(reqPath);
		if (!methodPath.contains("{") && methodPath.equalsIgnoreCase(reqPath)) {
			return true;
		}

		if (methodPath.contains("{")) {
			HashMap<String, String> map = new HashMap<String, String>();
			String[] pathTokens = methodPath.split("/");
			String[] requestUriTokens = reqPath.split("/");
			if (pathTokens.length != requestUriTokens.length) {
				return false;
			}
			for (int i = 0; i < pathTokens.length; i++) {
				String pathToken = pathTokens[i];
				String requestToken = requestUriTokens[i];
				if (pathToken.startsWith("{") && pathToken.endsWith("}")) {
					map.put(pathToken.substring(1, pathToken.length() - 1), requestUriTokens[i]);
				} else if (!pathToken.matches(requestToken)) {
					return false;
				}
			}
			if (request != null) {
				request.setAttribute("path_variables_param_map", map);
			}
			return true;
		}
		return false;
	}

	private static String removeTrailingSlash(String reqPath) {
		reqPath = reqPath.trim();
		return reqPath.endsWith("/") ? reqPath.substring(0, reqPath.length() - 1) : reqPath;
	}

	private static Object cast(Map map, Class<?> class1)
			throws JSONException, JsonParseException, JsonMappingException, IOException {
		return RequestController.mapper().convertValue(map, class1);
	}

	private static Object cast(String value, Class<?> class1)
			throws JSONException, JsonParseException, JsonMappingException, IOException {
		if (value == null || "".equals(value.trim())) {
			return null;
		}
		if (class1.getName().equals(Long.class.getName())) {
			return Long.valueOf(value);
		} else if (class1.getName().equals(Integer.class.getName())) {
			return Integer.valueOf(value);
		} else if (class1.getName().equals(Double.class.getName())) {
			return Double.valueOf(value);
		} else if (class1.getName().equals(Boolean.class.getName())) {
			return Boolean.valueOf(value);
		} else if (class1.getName().equals(JSONObject.class.getName())) {
			return new JSONObject(value);
		} else if (class1.getName().equals(JSONArray.class.getName())) {
			return new JSONArray(value);
		} else if (class1.getName().equals(String.class.getName())) {
			return value;
		} else {
			return RequestController.mapper().readValue(value, class1);
		}
	}

	public String getRequestBody(HttpServletRequest request, String method) throws IOException {
		if ("DELETE".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method) || "put".equalsIgnoreCase(method)) {
			return request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		}
		return null;
	}

	public static void main3(String[] args) {
		Method[] methods = AdminController.class.getMethods();
		for (Method method2 : methods) {
			System.out.println("Method ::::" + method2.getName() + "::::" + method2.getDeclaredAnnotations().length);
			for (Annotation annotation : method2.getDeclaredAnnotations()) {
				System.out.println(annotation.getClass().getName());
			}
			RequestMapping[] requestMappings = method2.getDeclaredAnnotationsByType(RequestMapping.class);
			System.err.println(requestMappings.length);
		}
	}

	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException, JSONException {
		String stringValue = "string asdf asfd";
		String longValue = "1000000000000000";
		String intValue = "100";
		String doubleValue = "100.12";
		String json = "{'name' : 'value' }";
		String jsonArray = "[ {'name1' : 'value1' }, {'name2' : 'value2' }]";
		String accountInfoEntity = "{\n" + "\"osync_id\" : 100, \n" + "\"service_id\" : 100,\n"
				+ "\"remote_identifier\" : \"Remotely Identified value\",\n" + "\"name\" : \"Name of the entity\",\n"
				+ "\"email\" : \"osync@oapps.xyz\",\n" + "\"plan_name\" : \"Professional\" \n" + "}";

		Object cast1 = cast(stringValue, String.class);
		checkCast(cast1, String.class);

		Object result = cast(longValue, Long.class);
		checkCast(result, Long.class);

		result = cast(intValue, Integer.class);
		checkCast(result, Integer.class);

		result = cast(doubleValue, Double.class);
		checkCast(result, Double.class);

		result = cast(json, JSONObject.class);
		checkCast(result, JSONObject.class);

		result = cast(jsonArray, JSONArray.class);
		checkCast(result, JSONArray.class);

		result = cast(accountInfoEntity, AccountInfoEntity.class);
		checkCast(result, AccountInfoEntity.class);

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("client_id", "thisisosyncid");
		map.put("service_id", "thisisserviceid");
		map.put("remote_identifier", "thisisremoteidentifier");
		map.put("name", "vijay calling");
		map.put("email", "vijay@calling.com");
		map.put("planName", "new plan name");
//		AuthorizeParams aie = (AuthorizeParams) cast(map, AuthorizeParams.class);
//		System.out.println("cloet:::" + aie.getClient_id());

	}

	private static Map getParamMap(HttpServletRequest request) {
		Enumeration<String> paramNames = request.getParameterNames();
		HashMap<String, String> map = new HashMap<String, String>();
		while (paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			map.put(paramName, request.getParameter(paramName));
		}
		return map;
	}

	private static void checkCast(Object cast1, Class<?> class1) {
		String result = class1.getName() + "::::" + cast1 + "::::" + cast1.getClass().getName();
		if (cast1.getClass().getName().equals(class1.getName())) {
			result = "Success ::::" + result;
		} else {
			result = "Failure ::::" + result;
		}
		System.out.println(result);
	}

	public static void main2(String[] args) {
		String path = "/api/integ_id/path/123/";
		System.out.println(removeTrailingSlash(path));
		// String requestUri = "/api/1233/path/123";
		// System.out.println(isUrlMatches(null, path, requestUri));
	}

}