package xyz.oapps.osync;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

public class OsyncException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5014223538420619593L;

	public enum Code {
		API_ERROR, API_LIMIT_ERROR, FIELD_FORMAT_ERROR, INT_PROPS_FIELD_MAP_NOT_ADDED, INT_PROPS_NULL,
		INT_STATUS_PROPS_NULL, INT_STATUS_FIELD_MAP_NOT_ADDED, INT_STATUS_LEFT_SERVICE_AUTH_NULL, INT_STATUS_RIGHT_SERVICE_AUTH_NULL, INT_STATUS_LEFT_ACCESS_TOKEN_NULL, INT_STATUS_RIGHT_ACCESS_TOKEN_NULL, FIELD_MAP_MANDATORY_VALUE_MISSING, SYNC_INTEG_ID_NOT_PRESENT, SYNC_IN_PROGRESS, SYNC_OLD_SYNC_ERROR, SYNC_INVALID_STATUS, SYNC_PAUSED, INVOKER_AUTH_FAILED, INVALID_CREDENTIALS
	}

	private Code code;

	public OsyncException(Code code) {
		super(code.toString());
		this.setCode(code);
	}

	public OsyncException(Code code, String message) {
		super(message);
		this.setCode(code);
	}

	@Override
	public String toString() {
		return "OsyncException [" + getCode().toString() + "] " + (super.getMessage() == null ? "" : super.getMessage());
	}

	public Code getCode() {
		return code;
	}

	public void setCode(Code code) {
		this.code = code;
	}
	
	public HashMap<String, String> getError() {
		HashMap<String, String> error = new HashMap<String, String>();
		error.put("message", super.getMessage());
		error.put("code", code.toString());
		return error;
	}

}
