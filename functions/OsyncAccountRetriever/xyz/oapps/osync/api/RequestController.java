package xyz.oapps.osync.api;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;

import xyz.oapps.osync.CurrentContext;
import xyz.oapps.osync.annotation.RequestMapping;
import xyz.oapps.osync.api.OsyncConstants.AccessLevel;
import xyz.oapps.osync.entity.AuthorizationEntity;
import xyz.oapps.osync.handler.salesLoft.SalesLoftTestHandler;
import xyz.oapps.osync.repo.AuthorizationRepo;
import xyz.oapps.osync.util.CommonUtil;

public class RequestController {

	private static Logger log = Logger.getLogger(RequestController.class.getName());
	private static final String short_name = "rq_cn";
	public static Object getController(String name, String requestUri) {
		if ("admin".equals(name)) {
			return new AdminController();
		} else if ("integ".equals(name)) {
			return new IntegrationController();
		} else if ("test".equals(name)) {
			return new TestController();
		} else if ("module".equals(name)) {
			return new ModuleController();
		} else if ("cron".equals(name)) {
			return new CronController();
		} else if ("cleanup".equals(name)) {
			return new CleanupController();
		} else if ("sync".equals(name)) {
			return new SyncController();
		} else if (name == null) {
			return new AuthorizeController();
		} else if ("saleslofttest".equals(name)) {
			return new SalesLoftTestHandler();
		} else {
			CommonUtil.logOsyncInfo(short_name,"No matching handler for :" + name + ":::" + requestUri);
		}
		return null;
	}

	public static ObjectMapper mapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		return mapper;
	}

	public static ObjectMapper revmapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
		// mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return mapper;
	}

	public static String getUUID() {
		return UUID.randomUUID().toString();
	}

	public static DSLContext query() {
		return DSL.using(SQLDialect.SQLITE);
	}

	public static boolean authenticate(HttpServletRequest request, HttpServletResponse response, Method method2,
			RequestMapping reqMap) throws Exception {
		String header = request.getHeader("Osync-Authorization");
		boolean isFromCookie = false;
		
		if(header == null) {
			header = getCookie(request, "_osat");
			if (header != null) {
				isFromCookie = true;
			}
		}
		int allowedLevel = AccessLevel.OSYNC_ADMIN; // highest level
		int userLevel = AccessLevel.PUBLIC; // lowest level

		AuthorizationRepo authRepo = new AuthorizationRepo();

		CurrentContext.createOrGetContext().setDisableDBCheck(Boolean.TRUE);
		AuthorizationEntity authEntity = authRepo.findByToken(header);
		CurrentContext.createOrGetContext().setDisableDBCheck(Boolean.FALSE);

		if (authEntity != null) {
			if (authEntity.isAdmin()) {
				userLevel = AccessLevel.OSYNC_ADMIN;
			} else {
				userLevel = AccessLevel.ADMIN;
			}
			CurrentContext.setCurrentContext(authEntity.getOsyncId(), authEntity, userLevel);
		}

		allowedLevel = reqMap.accessLevel();
		
		if(allowedLevel == AccessLevel.PUBLIC) {
			return true;
		}
		
		if(isFromCookie && !(allowedLevel == AccessLevel.OSYNC_ADMIN)) {
			CommonUtil.logOsyncInfo(short_name,"cookie is not allowed for normal users as there is no CSRF protection");
			return false;
		}

		if (userLevel >= allowedLevel) {
			return true;
		}

		return false;
	}

	public static void addAuthCookie(HttpServletResponse response, AuthorizationEntity authEnt) {
		Cookie cookie = new Cookie("_osat", authEnt.getToken());
		cookie.setMaxAge(24 * 60 * 60);
		cookie.setPath("/");
		response.addCookie(cookie);
	}
	
	private static String getCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if(cookies != null) {
			for (Cookie cookie : cookies) {
				if(cookie.getName().equals(name)) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

}
