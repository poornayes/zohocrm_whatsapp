package xyz.oapps.osync.handler.salesLoft;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import xyz.oapps.osync.OsyncException;
import xyz.oapps.osync.SyncHandler;
import xyz.oapps.osync.User;
import xyz.oapps.osync.fields.Fields;
import xyz.oapps.osync.fields.Record;
import xyz.oapps.osync.fields.RecordSet;
import xyz.oapps.osync.util.CommonUtil;

public class SalesLoftHandler extends SyncHandler {

	private static final String CF_NAME = "custom_fields";

	private static final String CF_PREFIX = "custom_fields_";

	private static final Logger log = Logger.getLogger(SalesLoftHandler.class.getName());

	public static final String ACCOUNT_MODULE = "Accounts";
	public static final String PEOPLE_MODULE = "People";

	private static final String ACCOUNT_URL = "https://api.salesloft.com/v2/accounts.json";

	private static final String PEOPLE_URL = "https://api.salesloft.com/v2/people.json";

	private static final String CURRENT_USER_URL = "https://api.salesloft.com/v2/me.json";
	
	private static final String short_name = "sl_cn";

	public Fields getFields() throws OsyncException {

		Fields field = Fields.of();
		if ((ACCOUNT_MODULE).equals(getModuleName())) {
			field = getAccountFields(field);
			field = fetchCustomFields(field, "company");
		} else if (PEOPLE_MODULE.equals(getModuleName())) {
			field = getPeopleFields(field);
			field = fetchCustomFields(field, "person");
		}
		return field;
	}

	private Fields getAccountFields(Fields fields) {
		fields.text("name", "Name", true);
		fields.text("domain", "Domain", true);
		fields.text("conversational_name", "Conversational Name", false);
		fields.text("description", "Description", false);
		fields.text("phone", "Phone", false);
		fields.text("website", "Website", false);
		fields.text("linkedin_url", "Linkedin URL", false);
		fields.text("twitter_handle", "Twitter Handle", false);
		fields.text("street", "Street", false);
		fields.text("city", "City", false);
		fields.text("state", "State", false);
		fields.text("postal_code", "Postal Code", false);
		fields.text("country", "Country", false);
		fields.text("locale", "Locale", false);
		fields.text("industry", "Industry", false);
		fields.text("company_type", "Company Type", false);
		fields.text("founded", "Founded", false);
		fields.text("revenue_range", "Revenue Range", false);
		fields.text("size", "size", false);
		fields.bool("do_not_contact", "Do Not Contact", false);
		return fields;
	}

	private Fields getPeopleFields(Fields fields) {
		fields.text("first_name", "First Name", true);
		fields.text("last_name", "Last Name", true);
		fields.text("display_name", "Display Name", false);
		fields.text("email_address", "Email", true);
//		fields.text("secondary_email_address", "Secondary Email", false);
//		fields.text("personal_email_address", "Personal Email", false);
		fields.text("phone", "Phone", true);
		fields.text("phone_extension", "Phone Extension", false);
		fields.text("home_phone", "Home phone", false);
		fields.text("mobile_phone", "Mobile Phone", false);
		fields.text("linkedin_url", "Linked In URL", false);
		fields.text("title", "Title", false);
		fields.text("city", "City", false);
		fields.text("state", "State", false);
		fields.text("country", "Country", false);
		fields.text("work_city", "Work City", false);
		fields.text("work_state", "Work State", false);
		fields.text("work_country", "Work Country", false);
		fields.text("person_company_name", "Person Company Name", false);
		fields.text("person_company_website", "Person Company Website", false);
		fields.text("person_company_industry", "Person Company Industry", false);
		fields.bool("do_not_contact", "Do Not Contact", false);
		fields.text("personal_website", "Personal WebSite", false);
		fields.text("twitter_handle", "Twitter", false);
		fields.text("job_seniority", "Job Seniority", false);
		return fields;
	}

	public RecordSet fetchRecords(int startPage, int totalRecords, Long lastSyncTime)  throws OsyncException {
		String url = getModuleUrl(getModuleName());
		try {
			// lastSyncTime= Long.parseLong("2020-01-01T00:00:00.000000-05:00");
			JSONObject updatedtime = new JSONObject();
			if (lastSyncTime.longValue() != -1) {
				updatedtime.put("updated_at[gt]", toISO8601Format(lastSyncTime));
			}
			updatedtime.put("page", startPage);
			String response = invoker().get(url, null, updatedtime, null);
			CommonUtil.logOsyncInfo(short_name,"Response fetch salesloft People==>" + response);
			JSONObject json = new JSONObject(response);
			JSONArray accountArrays = json.getJSONArray("data");

			RecordSet recordSet = RecordSet.init("salesloft-people", "id");
			for (int i = 0; i < accountArrays.length(); i++) {
				JSONObject accountObj = accountArrays.optJSONObject(i);
				addPeopleRecord(recordSet, accountObj);
			}
			return recordSet;

		} catch (OsyncException e) {
			throw e;
		} catch (Exception e) {
			CommonUtil.logOsyncInfo(short_name,"Exception>>>" + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	private String getModuleUrl(String moduleName) {
		String url = PEOPLE_URL;
		if (ACCOUNT_MODULE.equals(moduleName)) {
			url = ACCOUNT_URL;
		} else if (PEOPLE_MODULE.equals(moduleName)) {
			url = PEOPLE_URL;
		}
		return url;
	}

	private String getModuleUniqueCol(String moduleName) {
		return "email_addresses";
	}

	private void addPeopleRecord(RecordSet recordSet, JSONObject peopleObj) throws ParseException, JSONException {
		Long modifiedTime = getModifiedTime(peopleObj);
		Record record = recordSet.add(peopleObj.optString("id"), modifiedTime);
		Iterator<String> iterator = peopleObj.keys();
		while (iterator.hasNext()) {
			String key = iterator.next();
			if (key.equals(CF_NAME)) {
				JSONObject customFields = peopleObj.getJSONObject(key);
				Iterator<String> cfIt = customFields.keys();
				while (cfIt.hasNext()) {
					String cfKey = cfIt.next();
					record.addOrUpdateValue(CF_PREFIX + cfKey, customFields.get(cfKey));
				}
			} else {
				record.addOrUpdateValue(key, peopleObj.get(key));
			}
		}
	}

	private Long getModifiedTime(JSONObject accountObj) throws ParseException {
		String date = accountObj.optString("updated_at");
		// replacing nano second as it is not supported in simpledateformat
		// 2020-07-24T11:36:14.415758-04:00
		int indexOf = date.indexOf('.');
		String substring = date.substring(0, indexOf);
		String substring2 = date.substring(indexOf + 7, date.length());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		Date date2 = sdf.parse(substring + substring2);
		return date2.getTime();
	}

	public HashMap<String, String> createNewRecords(RecordSet recordSet, String syncFrom) throws OsyncException {

		String url = getModuleUrl(getModuleName());
		HashMap<String, String> uvMap = new HashMap<String, String>();
		for (Record record : recordSet) {
			try {
				String response = "";
				JSONObject peopleJson = record.columnValuesAsJson();
				if (peopleJson != null) {
					addCFjson(recordSet.getSourceModule(), record, peopleJson);
					CommonUtil.logOsyncInfo(short_name,"creating this: " + peopleJson.toString());
					response = invoker().post(url, null, null, peopleJson);
					CommonUtil.logOsyncInfo(short_name,"Response==>." + response);
					String id = getRecordId(response);
					if (id != null) {
						uvMap.put(record.getMappedRecordUniqueValue(), id);
					}
				} else {
					log.severe("Error while creating contact : " + response);
				}

			} catch (OsyncException e) {
				throw e;
			}  catch (Exception e) {
				log.log(Level.SEVERE, "Exception while creating new records to salesLoft - " + syncFrom, e);
				CommonUtil.logOsyncInfo(short_name,"Continuing the next record..");
				e.printStackTrace();
			}

		}
		return uvMap;

	}

	private void addCFjson(String srcModuleName, Record record, JSONObject peopleJson) throws JSONException {
		Iterator<String> keys = peopleJson.keys();
		JSONObject cfJson = new JSONObject();
		List<String> keysToRemove = new ArrayList<String>();
		while (keys.hasNext()) {
			String key = keys.next();
			if (key.startsWith(CF_PREFIX)) {
				cfJson.put(key.replace(CF_PREFIX, ""), peopleJson.get(key));
				keysToRemove.add(key);
			}
		}
		if (record.getMappedRecordUniqueValue() != null) {
			peopleJson.put("crm_id", record.getMappedRecordUniqueValue());
		}
		if (srcModuleName != null) {
			peopleJson.put("crm_object_type", srcModuleName);
		}
		for (String keyToRemove : keysToRemove) {
			cfJson.remove(keyToRemove);
		}
		if (cfJson.length() > 0) {
			peopleJson.put(CF_NAME, cfJson);
		}
	}

	private String getRecordId(String response) {
		try {
			JSONObject accountObj = new JSONObject(response);
			if (accountObj.has("data")) {
				return accountObj.optJSONObject("data").optString("id");
			} else {
				log.severe("error on create record : " + accountObj);
			}
		} catch (JSONException e) {
			log.severe("response on create record : " + response);
			e.printStackTrace();
		}
		return null;
	}

	public void updateRecords(RecordSet recordSet, String syncFrom) throws OsyncException {
		for (Record record : recordSet) {
			try {
				String response = "";
				JSONObject json = record.columnValuesAsJson();

				if (json != null) {
					addCFjson(recordSet.getSourceModule(), record, json);
					CommonUtil.logOsyncInfo(short_name,"creating this: " + json.toString());
					response = invoker().put("https://api.salesloft.com/v2/" + getModuleName().toLowerCase() + "/"
							+ record.getUniqueValue() + ".json", null, null, json);
					CommonUtil.logOsyncInfo(short_name,"Response==>>>>>>>>>>>>>>>>>>>" + response);
				} else {
					log.severe("Error while updating People : " + response);
				}
			} catch (OsyncException e) {
				throw e;
			}  catch (Exception e) {
				log.log(Level.SEVERE, "Exception while updating new records to salesLoft - " + syncFrom, e);
				e.printStackTrace();
			}
		}
	}

	public RecordSet getMatchedRecordsById(List<String> recordsToFetchRemote) throws OsyncException {
		try {
			String response = "";
			RecordSet recordSet = RecordSet.init("SalesLoft", "id");
			StringBuilder queryBuilder = new StringBuilder();
			int i = 0;
			List<String> list = new ArrayList<String>();
			for (String entityId : recordsToFetchRemote) {
				queryBuilder.append("ids[]=").append(entityId).append("&");
				if (i != 0 && (i % 5) == 0) {
					list.add(queryBuilder.toString());
					queryBuilder = new StringBuilder();
				}
				i++;
			}
			String queryStr = queryBuilder.toString();
			if (!queryStr.isEmpty()) {
				list.add(queryStr);
			}
			for (String query : list) {
				String targetUrl = "https://api.salesloft.com/v2/" + getModuleName().toLowerCase() + ".json?" + query;
				response = invoker().get(targetUrl, null, null, null);
				if (response != null) {
					JSONObject json = new JSONObject(response);
					JSONArray accountJSONArray = json.getJSONArray("data");
					for (int j = 0; j < accountJSONArray.length(); j++) {
						addPeopleRecord(recordSet, accountJSONArray.getJSONObject(j));
					}
				}
			}
			return recordSet;

		} catch (OsyncException e) {
			throw e;
		} catch (Exception e) {
			CommonUtil.logOsyncInfo(short_name,"Exception updating IDS" + e);
			e.printStackTrace();

		}

		return null;
	}

	public RecordSet getMatchedRecordsByUniqueColumn(Collection<String> values) throws OsyncException {
		String uniqueCol = getModuleUniqueCol(getModuleName());
		try {
			String response = "";
			RecordSet recordSet = RecordSet.init("SalesLoft", "id");
			StringBuilder queryBuilder = new StringBuilder();
			int i = 0;
			List<String> list = new ArrayList<String>();
			for (String uniqueVal : values) {
				queryBuilder.append(uniqueCol).append("[]=").append(uniqueVal).append("&");
				if (i != 0 && (i % 5) == 0) {
					list.add(queryBuilder.toString());
					queryBuilder = new StringBuilder();
				}
				i++;
			}
			String queryStr = queryBuilder.toString();
			if (!queryStr.isEmpty()) {
				list.add(queryStr);
			}
			for (String query : list) {
				String targetUrl = "https://api.salesloft.com/v2/" + getModuleName().toLowerCase() + ".json?" + query;
				response = invoker().get(targetUrl, null, null, null);
				if (response != null) {
					JSONObject json = new JSONObject(response);
					JSONArray accountJSONArray = json.getJSONArray("data");
					for (int j = 0; j < accountJSONArray.length(); j++) {
						addPeopleRecord(recordSet, accountJSONArray.getJSONObject(j));
					}
				}
			}
			return recordSet;

		} catch (OsyncException e) {
			throw e;
		} catch (Exception e) {
			CommonUtil.logOsyncInfo(short_name,"Exception updating IDS" + e);
			e.printStackTrace();

		}

		return null;
	}

	private Fields fetchCustomFields(Fields field, String module) throws OsyncException {
		try {
			boolean hasNextPage = true;
			int page = 1;

			while (hasNextPage) {
				JSONObject params = new JSONObject();
				params.put("field_type", module);
				params.put("per_page", 100);
				params.put("page", page);
				String customResponse = invoker().get("https://api.salesloft.com/v2/custom_fields.json", null, params,
						null);

				JSONObject customObject = new JSONObject(customResponse);
				JSONArray customArrays = customObject.getJSONArray("data");
				for (int i = 0; i < customArrays.length(); i++) {
					JSONObject customObj = customArrays.optJSONObject(i);
					String field_type = customObj.get("field_type").toString();
					String value_type = customObj.get("value_type").toString();
					String name = customObj.get("name").toString();
					CommonUtil.logOsyncInfo(short_name,"field_type==>>" + field_type);
					CommonUtil.logOsyncInfo(short_name,"value_type==>>" + field_type);
					CommonUtil.logOsyncInfo(short_name,"DisplayName==>>" + name.toString());
					String id = CF_PREFIX + name;
					if (("person").equals(field_type)) {
						if (("text").equals(value_type)) {
							field.text(id, name, false);
						}
						else if (("date").equals(value_type)) {
							field.date(id, name, false, "yyyy-MM-dd");
						}
					} else if (("company").equals(field_type)) {
						if (("text").equals(value_type)) {
							field.text(id, name, false);
						}
						else if (("date").equals(value_type)) {
							field.date(id, name, false, "yyyy-MM-dd");
						}
					}
				}
				try {
					Object nextPage = customObject.getJSONObject("metadata").getJSONObject("paging").get("next_page");
					String pages = nextPage.toString();

					// JSONObject jsonObject = (JSONObject) nextPage;
					if (!pages.equals("null")) {
						page = Integer.valueOf(nextPage.toString());
					} else {
						hasNextPage = false;
					}

				} catch (JSONException e) {
					System.out.println("Error while fetching next page: " + e.getMessage());
					e.printStackTrace();
					hasNextPage = false;
				}
			}
			return field;
		} catch (OsyncException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public User getCurrentUser() throws OsyncException {
		try {
			JSONObject param = new JSONObject();
			param.put("type", "CurrentUser");
			String response = invoker().get(CURRENT_USER_URL, null, param, null);
			JSONObject userObject = (new JSONObject(response)).getJSONObject("data");
			String email = userObject.getString("email");
			String fullName = userObject.getString("name");
			String uniquId = userObject.getString("guid");
			User user = new User(email);
			user.setUniqueUserId(uniquId);
			user.setFullName(fullName);
			user.setEmail(email);
			user.setFullJson(userObject);
			return user;
		} catch (OsyncException e) {
			throw e;
		}  catch (Exception e) {
			log.log(Level.SEVERE, null, e);
		}
		return null;
	}
}