package xyz.oapps.osync;

import xyz.oapps.osync.entity.AuthorizationEntity;
import xyz.oapps.osync.util.CommonUtil;

public class CurrentContext {

	private String osyncId;
	private AuthorizationEntity token;
	private int userAccessLevel;
	private String uniqueLogId;

	private boolean disableDBCheck = false;

	static ThreadLocal<CurrentContext> currentContext = new ThreadLocal<CurrentContext>();

	private CurrentContext() {

	}

	public static void setCurrentContext(String osyncId, AuthorizationEntity token, int userAccessLevel) {
		CurrentContext context = createOrGetContext();
		context.osyncId = osyncId;
		context.token = token;
		context.userAccessLevel = userAccessLevel;
		context.uniqueLogId = CommonUtil.getRandomString();
	}
	
	public static void setCurrentContext(String osyncId) {
		CurrentContext context = createOrGetContext();
		context.osyncId = osyncId;
	}

	public static CurrentContext createOrGetContext() {
		CurrentContext currentContext2 = getCurrentContext();
		if (currentContext2 == null) {
			currentContext2 = new CurrentContext();
			currentContext.set(currentContext2);
		}
		return currentContext2;
	}

	public static CurrentContext getCurrentContext() {
		return currentContext.get();
	}

	public static void clear() {
		currentContext.set(null);
	}

	public void setDisableDBCheck(Boolean bool) {
		this.disableDBCheck = bool.booleanValue();
	}

	public boolean isDBCheckDisabled() {
		return disableDBCheck;
	}

	public String getOsyncId() {
		return osyncId;
	}

	public AuthorizationEntity getToken() {
		return token;
	}

	public int getUserAccessLevel() {
		return userAccessLevel;
	}

	public static String getCurrentOsyncId() {
		return getCurrentContext().getOsyncId();
	}

	public String getUniqueLogId() {
		return uniqueLogId;
	}

	public void setUniqueLogId(String uniqueLogId) {
		this.uniqueLogId = uniqueLogId;
	}

}
