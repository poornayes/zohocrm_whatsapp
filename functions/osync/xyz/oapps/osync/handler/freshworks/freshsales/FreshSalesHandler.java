package xyz.oapps.osync.handler.freshworks.freshsales;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import xyz.oapps.osync.SyncHandler;
import xyz.oapps.osync.User;
import xyz.oapps.osync.fields.Fields;
import xyz.oapps.osync.fields.Record;
import xyz.oapps.osync.fields.RecordSet;
import xyz.oapps.osync.util.CommonUtil;
import xyz.oapps.osync.util.HttpUtil;

public class FreshSalesHandler extends SyncHandler {

	private static Logger log = Logger.getLogger(FreshSalesHandler.class.getName());
	private static final String short_name = "fs_hl";

	@Override
	public Fields getFields() {
		try {
			String response = HttpUtil.get("https://oapps-xyz.freshsales.io/api/settings/contacts/fields",
					getAuthMap());
			JSONObject json = new JSONObject(response);
			JSONArray array = json.optJSONArray("fields");
			if (array != null) {
				Fields field = Fields.of();
				for (int i = 0; i < array.length(); i++) {
					JSONObject fieldObj = array.optJSONObject(i);
					if (fieldObj != null) {
						String id = fieldObj.optString("name");
						String displayName = fieldObj.optString("label");
						String type = fieldObj.optString("type");
						boolean isDefault = fieldObj.optBoolean("default", true);
						if (!isDefault) {
							id = "custom_field." + id;
						}
						switch (type) {
						case "text":
							field.text(id, displayName, false);
							break;
						case "checkbox":
							field.bool(id, displayName, false);
							break;
						case "number":
							field.number(id, displayName, false);
							break;
						default:
							System.out.println("unsupported-type: " + type + ":" + id + ":" + displayName);
						}
					}
				}
				return field;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private HashMap<String, String> getAuthMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("Authorization", "Token token=OzGETP2wFm-MAfGpnkzlcA");
		map.put("Content-Type", "application/json");
		return map;
	}

	@Override
	public HashMap<String, String> createNewRecords(RecordSet recordSet, String syncFrom) {

		HashMap<String, String> uvMap = new HashMap<String, String>();
		for (Record record : recordSet) {
			try {
				JSONObject json = record.columnValuesAsJson();
				Iterator<String> keys = json.keys();
				JSONObject cfJson = new JSONObject();
				List<String> keysToRemove = new ArrayList<String>();
				while (keys.hasNext()) {
					String key = keys.next();
					if (key.startsWith("custom_field")) {
						cfJson.put(key.replaceFirst("custom_field.", ""), json.get(key));
						// json.remove(key);
						keysToRemove.add(key);
					}
				}
				for (String key : keysToRemove) {
					json.remove(key);
				}
				json.put("custom_field", cfJson);

				JSONObject contactJson = new JSONObject();
				contactJson.put("contact", json);
				CommonUtil.logOsyncInfo(short_name,"posting this: " + json.toString());
				String response = HttpUtil.post("https://oapps-xyz.freshsales.io/api/contacts", contactJson.toString(),
						getAuthMap());
				JSONObject responseJson = new JSONObject(response);
				// TODO: contact may not be present
				JSONObject contactResJson = responseJson.optJSONObject("contact");
				if (contactResJson != null) {
					Long responseId = contactResJson.optLong("id");
					uvMap.put(responseId.toString(), record.getMappedRecordUniqueValue());
				} else {
					log.severe("Error while creating contact : " + response);
				}
			} catch (Exception e) {
				log.log(Level.SEVERE, "Exception while creating new records to Freshsales - " + syncFrom, e);
				CommonUtil.logOsyncInfo(short_name,"Continuing the next record..");
			}
		}

		return uvMap;

	}

	@Override
	public void updateRecords(RecordSet recordSet, String syncFrom) {
		for (Record record : recordSet) {
			try {
				JSONObject json = record.columnValuesAsJson();
				Iterator<String> keys = json.keys();
				JSONObject cfJson = new JSONObject();

				List<String> keysToRemove = new ArrayList<String>();
				while (keys.hasNext()) {
					String key = keys.next();
					if (key.startsWith("custom_field")) {
						cfJson.put(key.replaceFirst("custom_field.", ""), json.get(key));
						// json.remove(key);
						keysToRemove.add(key);
					}
				}
				for (String key : keysToRemove) {
					json.remove(key);
				}

				json.put("custom_field", cfJson);

				JSONObject contactJson = new JSONObject();
				contactJson.put("contact", json);

				CommonUtil.logOsyncInfo(short_name,"updating this: " + json.toString());
				String response = HttpUtil.put(
						"https://oapps-xyz.freshsales.io/api/contacts/" + record.getUniqueValue(),
						contactJson.toString(), getAuthMap());
				CommonUtil.logOsyncInfo(short_name,"Sync response " + response);
			} catch (Exception e) {
				log.log(Level.SEVERE, "Exception while creating new records to Freshsales - " + syncFrom, e);
				CommonUtil.logOsyncInfo(short_name,"Continuing the next record..");
			}
		}

	}

	public static void main(String[] args) {
		// FreshSalesController freshSalesController = new FreshSalesController();
		// Fields fields = freshSalesController.getFields(null);
//		System.out.println(fields);

//		RecordSet recordSet = freshSalesController.fetchUpdatedRecords(null, null);
//		for (Record record : recordSet) {
//			System.out.println(record.getUniqueValue() + "::::" + record);
//		}
	}

	public String getUniqueColumnName(String destination, String syncController) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPrimaryKey(String destination) {
		return "id";
	}

	@Override
	public RecordSet fetchRecords(int startPage, int totalRecords, Long startTime) {
		try {
			String response = HttpUtil.get("https://oapps-xyz.freshsales.io/api/contacts/view/13001276804?per_page="
					+ totalRecords + "&page=" + startPage, getAuthMap());
			JSONObject json = new JSONObject(response);
			JSONArray contactsArray = json.optJSONArray("contacts");
			RecordSet recordSet = RecordSet.init("freshsales-contacts", "id");
			for (int i = 0; i < contactsArray.length(); i++) {
				JSONObject contactObj = contactsArray.optJSONObject(i);
				Record record = recordSet.add(contactObj.optString("id"), null);
				Iterator<String> iterator = contactObj.keys();
				while (iterator.hasNext()) {
					String key = iterator.next();
					if ("custom_field".equals(key)) {
						JSONObject customField = contactObj.optJSONObject(key);
						Iterator<String> cfKeys = customField.keys();
						while (cfKeys.hasNext()) {
							String cfKey = cfKeys.next();
							record.addOrUpdateValue(key + "." + cfKey, customField.get(cfKey));
						}
					} else {
						record.addOrUpdateValue(key, contactObj.get(key));
					}
				}
			}
			CommonUtil.logOsyncInfo(short_name,"Records fetched from freshsales :" + recordSet.count());
			return recordSet;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public RecordSet getMatchedRecordsById(List<String> recordsToFetchRemote) {
		RecordSet recordSet = RecordSet.init("freshsales-contacts", "id");
		for (String uniqueId : recordsToFetchRemote) {
			try {
				String response = HttpUtil.get("https://oapps-xyz.freshsales.io/api/contacts/" + uniqueId.trim(),
						getAuthMap());
				JSONObject json = new JSONObject(response);
				JSONObject contactObj = json.optJSONObject("contact");
				Record record = recordSet.add(contactObj.optString("id"), null);
				Iterator<String> iterator = contactObj.keys();
				while (iterator.hasNext()) {
					String key = iterator.next();
					if ("custom_field".equals(key)) {
						JSONObject customField = contactObj.optJSONObject(key);
						Iterator<String> cfKeys = customField.keys();
						while (cfKeys.hasNext()) {
							String cfKey = cfKeys.next();
							record.addOrUpdateValue(key + "." + cfKey, customField.get(cfKey));
						}
					} else {
						record.addOrUpdateValue(key, contactObj.get(key));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		CommonUtil.logOsyncInfo(short_name,"Matched Records fetched from freshsales :" + recordSet.count());
		return recordSet;
	}

	@Override
	public RecordSet getMatchedRecordsByUniqueColumn(Collection<String> values) {
		try {

			JSONObject searchJson = new JSONObject();
			JSONArray filterRule = new JSONArray();

			JSONObject filterObject = new JSONObject();
			filterObject.put("attribute", "contact_email.email");
			filterObject.put("operator", "is_in");
			filterObject.put("value", values);
			filterRule.put(filterObject);

			searchJson.put("filter_rule", filterRule);

			String response = HttpUtil.post("https://oapps-xyz.freshsales.io/api/filtered_search/contact",
					searchJson.toString(), getAuthMap());
			JSONObject json = new JSONObject(response);
			JSONArray contactsArray = json.optJSONArray("contacts");
			RecordSet rs = RecordSet.init("freshsales-contacts", "id");
			for (int i = 0; i < contactsArray.length(); i++) {
				JSONObject contactObj = contactsArray.optJSONObject(i);
				Record record = rs.add(contactObj.optString("id"), null);
				Iterator<String> iterator = contactObj.keys();
				while (iterator.hasNext()) {
					String key = iterator.next();
					if ("custom_field".equals(key)) {
						JSONObject customField = contactObj.optJSONObject(key);
						Iterator<String> cfKeys = customField.keys();
						while (cfKeys.hasNext()) {
							String cfKey = cfKeys.next();
							record.addOrUpdateValue(key + "." + cfKey, customField.get(cfKey));
						}
					} else {
						record.addOrUpdateValue(key, contactObj.get(key));
					}
				}
			}
			CommonUtil.logOsyncInfo(short_name,"Matched Records fetched from freshsales :" + rs.count());
			return rs;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public User getCurrentUser() {
		// TODO Auto-generated method stub
		return null;
	}

}
