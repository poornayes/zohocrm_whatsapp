package xyz.oapps.osync.db;

import static org.jooq.impl.DSL.field;
import static xyz.oapps.osync.api.RequestController.query;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

public class OsyncConfig {
	static HashMap<String, String> config_map = null;
	static Logger logger = Logger.getLogger(OsyncConfig.class.getName());

	public static String getConfig(String config) {
		return getConfig(config, null);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getConfig(String config, T defaultValue) {
		String value = getConfig(config, defaultValue.toString());
		if (defaultValue instanceof Long) {
			return (T) Long.valueOf(value);
		} else if (defaultValue instanceof Integer) {
			return (T) Integer.valueOf(value);
		} else if (defaultValue instanceof Boolean) {
			return (T) Boolean.valueOf(value);
		}
		return (T) value;
	}

	public static String getConfig(String config, String defaultValue) {
		try {
			if (config_map == null) {
				synchronized ("osync_config") {
					if (config_map == null) {
						config_map = new HashMap<String, String>();
						List<JSONObject> configs = OsyncDB.get()
								.executeSystemQuery(query().select(field("*")).from("OsyncConfiguration").getQuery());
						if (configs != null) {
							for (JSONObject jsonObject : configs) {
								System.out.println(jsonObject);
								config_map.put(jsonObject.get("name").toString(), jsonObject.get("value").toString());
							}
						}
					}
				}
			}
			String val = config_map.get(config);
			if (val == null) {
				val = defaultValue;
			}
			return val;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error while loading configs", e);
		}
		return defaultValue;
	}

	public static <T> T getConfigFromDB(String configName, T defaultValue) throws Exception {
		List<JSONObject> configs = OsyncDB.get().executeSystemQuery(
				query().select(field("*")).from("OsyncConfiguration").where(field("name").eq(configName)).getQuery());
		T result = defaultValue;
		if (configs != null && configs.size() == 1) {
			String value = configs.get(0).getString("value");
			if (value != null) {
				if (defaultValue instanceof Long) {
					result = (T) Long.valueOf(value);
				} else if (defaultValue instanceof Integer) {
					result = (T) Integer.valueOf(value);
				} else if (defaultValue instanceof Boolean) {
					result = (T) Boolean.valueOf(value);
				} else {
					result = (T) value;
				}
			}
		}
		return result;
	}
}
