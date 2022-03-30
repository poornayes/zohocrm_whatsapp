package xyz.oapps.osync.handler.zoho.crm;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

public class ZohoCRMHandler extends SyncHandler {

	private static final String ISO_CRM_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
	private static final String short_name = "zc_hd";
	private static Logger log = Logger.getLogger(ZohoCRMHandler.class.getName());

	public static final String ACCOUNT_MODULE = "Accounts";
	public static final String CONTACTS_MODULE = "Contacts";
	public static final String LEADS_MODULE = "Leads";

	public static final int firstElement_Array = 0;

	public static final String ACCOUNT_URL = "https://www.zohoapis.com/crm/v2/Accounts";

	public static final String USERS_URL = "https://www.zohoapis.com/crm/v2/users";

	public static final String CONTACTS_URL = "https://www.zohoapis.com/crm/v2/Contacts";

	public static final String LEADS_URL = "https://www.zohoapis.com/crm/v2/Leads";

	public static final String CONTACTS_FIELDS_URL = "https://www.zohoapis.com/crm/v2/settings/fields?module=Contacts";

	public static final String ACCOUNTS_FIELDS_URL = "https://www.zohoapis.com/crm/v2/settings/fields?module=Accounts";

	public static final String LEADS_FIELDS_URL = "https://www.zohoapis.com/crm/v2/settings/fields?module=Leads";

	public Fields getFields() throws OsyncException {
		String url = getModuleFieldsUrl(getModuleName());
		try {
			String response = invoker().get(url, null, null, null);
			JSONObject jsonObject = new JSONObject(response);
			JSONArray values = jsonObject.getJSONArray("fields");
			Fields field = ZohoCRMHandler.getModuleFields(values);
			return field;
		} catch (OsyncException e) {
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			CommonUtil.logOsyncInfo(short_name,"Exception>>>3" + e.getMessage());

		} catch (Exception e) {
			e.printStackTrace();
			CommonUtil.logOsyncInfo(short_name,"Exception>>>4" + e.getMessage());

		}

		return null;

	}

	private String getModuleFieldsUrl(String moduleName) {
		if (moduleName.equals(ACCOUNT_MODULE)) {
			return ACCOUNTS_FIELDS_URL;
		} else if (moduleName.equals(CONTACTS_MODULE)) {
			return CONTACTS_FIELDS_URL;
		} else if (moduleName.equals(LEADS_MODULE)) {
			return LEADS_FIELDS_URL;
		}
		return CONTACTS_FIELDS_URL;
	}

	private String getModuleUrl(String moduleName) {
		if (moduleName.equals(ACCOUNT_MODULE)) {
			return ACCOUNT_URL;
		} else if (moduleName.equals(CONTACTS_MODULE)) {
			return CONTACTS_URL;
		} else if (moduleName.equals(LEADS_MODULE)) {
			return LEADS_URL;
		}
		return CONTACTS_URL;
	}

	private static Fields getModuleFields(JSONArray values) throws Exception {
		Fields fields = Fields.of();
		for (int i = 0; i < values.length(); i++) {
			JSONObject fieldObj = values.getJSONObject(i);
			String dataType = fieldObj.getString("data_type");
			String id = fieldObj.getString("api_name");
			String displayName = fieldObj.getString("display_label");
			boolean mandatory = fieldObj.getBoolean("system_mandatory");
			boolean readOnly = fieldObj.getBoolean("read_only") || fieldObj.getBoolean("field_read_only");
			boolean nonEditable = true;
			// view_type
			JSONObject viewType = fieldObj.optJSONObject("view_type");
			if (viewType != null) {
				nonEditable = viewType.getBoolean("edit") == false || viewType.getBoolean("create") == false;
			}
			if (readOnly || nonEditable) {
				CommonUtil.logOsyncInfo(short_name,"Skipping field type :" + dataType + ",:: Field Name :" + displayName + ",:: Readonly :"
						+ readOnly + ",:: Non Editable :" + nonEditable + ",:: mandatory :" + mandatory);
				continue;
			}
			switch (dataType) {
			case "text":
			case "email":
			case "phone":
			case "textarea":
			case "website":
				fields.text(id, displayName, mandatory);
				break;
			case "boolean":
				fields.bool(id, displayName, mandatory);
				break;
			case "double":
			case "currency":
				fields.doubleField(id, displayName, mandatory);
				break;
			case "integer":
			case "bigint":
				fields.number(id, displayName, mandatory);
				break;
			case "date":
				fields.date(id, displayName, mandatory, "yyyy-MM-dd");
				break;
			case "datetime":
				fields.datetime(id, displayName, mandatory, ISO_CRM_8601_FORMAT);
				break;
			default:
				CommonUtil.logOsyncInfo(short_name,"Unsupported field type :" + dataType + ",:: Field Name :" + displayName + ",:: mandatory :"
						+ mandatory);
			}
		}
		return fields;
	}

	public RecordSet fetchRecords(int startPage, int totalRecords, Long lastSyncTime)  throws OsyncException {
		System.out.println("Fetching records :" + startPage + "::" + totalRecords + ":::" + lastSyncTime + "::"
				+ getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
		String url = getModuleUrl(getModuleName());
		try {

			JSONObject queryParams = new JSONObject();
			queryParams.put("per_page", totalRecords);
			queryParams.put("page", startPage);

			JSONObject headerParams = new JSONObject();
			if (lastSyncTime != null && lastSyncTime.longValue() != -1l) {
				headerParams.put("If-Modified-Since", toISO8601Format(lastSyncTime));
			}

			String response = invoker().get(url, headerParams, queryParams, null);
			RecordSet recordSet = RecordSet.init("zohocrm", "id");
			if (!"".equals(response)) {
				JSONObject json = new JSONObject(response);
				JSONArray contactArrays = json.optJSONArray("data");
				if (contactArrays != null) {
					for (int i = 0; i < contactArrays.length(); i++) {
						JSONObject contactObj = contactArrays.optJSONObject(i);
						createRecord(recordSet, contactObj);
					}
				}
			}
			return recordSet;

		} catch (OsyncException e) {
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			CommonUtil.logOsyncInfo(short_name,"Exception>>>8" + e.getMessage());
		} catch (NumberFormatException e) {
			e.printStackTrace();
			CommonUtil.logOsyncInfo(short_name,"Exception>>>9" + e.getMessage());
		} catch (Exception e) {
			String message = e.getMessage() != null ? e.getMessage() : "";
			if (!message.contains("204 No Content")) {
				log.log(Level.SEVERE, null, e);
			}
		}
		return null;
	}

	private void createRecord(RecordSet recordSet, JSONObject contactObj) throws JSONException, ParseException {
		String modifiedTimeStr = contactObj.optString("Modified_Time");
		Long modifiedTime = -1l;
		if (modifiedTimeStr != null) {
			SimpleDateFormat sdf = new SimpleDateFormat(ISO_CRM_8601_FORMAT);
			Date date2 = sdf.parse(modifiedTimeStr);
			modifiedTime = date2.getTime();
		}
		Record record = recordSet.add(contactObj.optString("id"), modifiedTime);

		@SuppressWarnings("unchecked")
		Iterator<String> iterator = contactObj.keys();
		while (iterator.hasNext()) {
			String key = iterator.next();
			Object value = contactObj.get(key);
			if (!isNull(value)) {
				if (value instanceof JSONObject) {
					value = toMap(((JSONObject) value));
				}
				if (value instanceof JSONArray) {
					value = toList(((JSONArray) value));
				}
				record.addOrUpdateValue(key, value);
			} else {
				record.addOrUpdateValue(key, null);
			}
		}
	}

	public HashMap<String, String> createNewRecords(RecordSet recordSet, String syncFrom) throws OsyncException {

		HashMap<String, String> uvMap = new HashMap<String, String>();
		try {

			String postUrl = getModuleUrl(getModuleName());

			JSONArray array = new JSONArray();
			for (Record record : recordSet) {
				JSONObject json = record.columnValuesAsJson();
				array.put(json);
			}
			JSONObject dataJson = new JSONObject();
			dataJson.put("data", array);
			String response = invoker().post(postUrl, null, null, dataJson);
			JSONObject responseJson = new JSONObject(response);
			JSONArray responseArray = responseJson.getJSONArray("data");
			int i = 0;
			for (Record record : recordSet) {
				JSONObject jsonObject = responseArray.getJSONObject(i);
				String code = jsonObject.getString("code");
				if ("SUCCESS".equalsIgnoreCase(code)) {
					if (jsonObject.has("details")) {
						uvMap.put(record.getMappedRecordUniqueValue(),
								jsonObject.getJSONObject("details").getString("id"));
					}
				}
				i++;
			}
		} catch (OsyncException e) {
			throw e;
		} catch (Exception e) {
			CommonUtil.logOsyncInfo(short_name,e.getMessage());
			log.log(Level.INFO, null, e);
		}
		System.err.println("Records created:" + uvMap);
		return uvMap;
	}

	public void updateRecords(RecordSet recordSet, String syncFrom) throws OsyncException {
		try {
			String putUrl = getModuleUrl(getModuleName());
			JSONArray array = new JSONArray();
			for (Record record : recordSet) {
				JSONObject json = record.columnValuesAsJson();
				json.put("id", record.getUniqueValue());
				array.put(json);
			}
			JSONObject dataJson = new JSONObject();
			dataJson.put("data", array);
			invoker().put(putUrl, null, null, dataJson);

		} catch (OsyncException e) {
			throw e;
		}  catch (Exception e) {
			CommonUtil.logOsyncInfo(short_name,e.getMessage());
			log.log(Level.INFO, null, e);
		}

	}

	public RecordSet getMatchedRecordsById(List<String> recordsToFetchRemote) throws OsyncException {
		return searchRecords("id", recordsToFetchRemote);
	}

	private String appendUrl(String moduleUrl, String entityId) {
		if (!moduleUrl.endsWith("/")) {
			moduleUrl += "/";
		}
		return moduleUrl += entityId;
	}

	public static void main(String[] args) {
		String val = "santh,asdf(asdf)@asdf.com";
		val = val.replace(",", "\\,");
		val = val.replace("(", "\\(");
		val = val.replace(")", "\\)");
		System.out.println(val);
		System.out.println(URLEncoder.encode(val));
		List<String> asList = Arrays.asList("1neela@blue.com", "1rosy@rose.com", "1kala@chozla.com", "1testing@date.com", "1sasi@funds.com",
				"1manjal@myyellow.com", "1yema@destroy.com", "1vani@ranixyz.com", "1lalitha@efg.com",
				"1govind@green.com", "1pari@paise.com", "1hanumaan@jai.com", "1monisha@abc.com", "1raja@chozla.com",
				"neela@blue.com", "rosy@rose.com", "kala@chozla.com", "testing@date.com", "sasi@funds.com",
				"manjal@myyellow.com", "yema@destroy.com", "vani@ranixyz.com", "lalitha@efg.com", "govind@green.com"
				);
		List<String> criteria = getCriteria("Email", asList);
		for (String string : criteria) {
			System.out.println(string);
		}
		
	}

	public static List<String> getCriteria(String uniqueCol, Collection<String> values) {
		List<String> criterias = new ArrayList<String>();
		if (values != null) {
			int i = 0;
			StringBuilder criteria = new StringBuilder("(");
			boolean first = true;
			for (String val : values) {
				i++;
				val = val.replace(",", URLEncoder.encode("\\,"));
				val = val.replace("(", URLEncoder.encode("\\("));
				val = val.replace(")", URLEncoder.encode("\\)"));
				if (!first) {
					criteria.append("or");
				} else {
					first = false;
				}
				criteria.append("(").append(uniqueCol).append(":equals:").append(val).append(")");
				if ((i % 8) == 0) {
					criteria.append(")");
					criterias.add(criteria.toString());
					criteria = criteria.delete(0, criteria.length() - 1);
					if (criteria.length() > 0) {
						criteria = new StringBuilder();
					}
					first = true;
					criteria.append("(");
				}
			}
			if (!first) {
				criteria.append(")");
				criterias.add(criteria.toString());
			}
		}
		return criterias;
	}

	public RecordSet searchRecords(String uniqueColName, Collection<String> values) throws OsyncException {
		
		RecordSet rs = RecordSet.init("zohocrm", "id");
		String url = getModuleUrl(getModuleName());
		
		List<String> criterias = getCriteria(uniqueColName, values);
		for (String criteria : criterias) {
			try {
				JSONObject queryParams = new JSONObject();
				queryParams.put("criteria", criteria);
				String response = invoker().get(appendUrl(url, "search"), null, queryParams, null);
				JSONObject json = new JSONObject(response);
				if (json.has("data")) {
					JSONArray contactArrays = json.getJSONArray("data");
					for(int i = 0; i < contactArrays.length(); i++) {
						createRecord(rs, contactArrays.getJSONObject(i));
					}
				}
			} catch (OsyncException e) {
				throw e;
			} catch (Exception e) {
				if (!e.getMessage().contains("204 No Content")) {
					log.log(Level.SEVERE, "Error on fetchingMatched record, " + uniqueColName);
					log.log(Level.SEVERE, null, e);
				}
			}
		}
		return rs;
	}

	public RecordSet getMatchedRecordsByUniqueColumn(Collection<String> values) throws OsyncException {
		if(getModuleName().equals(CONTACTS_MODULE)) {
			return searchRecords("Email", values);	
		}
		return null;
	}

	@Override
	public User getCurrentUser() {
		try {
			JSONObject param = new JSONObject();
			param.put("type", "CurrentUser");
			String response = invoker().get(USERS_URL, null, param, null);
			JSONObject jsonObject = new JSONObject(response);
			JSONArray values = jsonObject.getJSONArray("users");
			JSONObject userObject = values.getJSONObject(0);
			String email = userObject.getString("email");
			String fullName = userObject.getString("full_name");
			String uniquId = userObject.getString("zuid");
			User user = new User(email);
			user.setUniqueUserId(uniquId);
			user.setFullName(fullName);
			user.setEmail(email);
			user.setFullJson(userObject);
			return user;
		} catch (Exception e) {
			log.log(Level.SEVERE, null, e);
		}
		return null;
	}

//	@Override
//	public Integer getTotalRecordsCountToSync() throws OsyncException {
//		if(getModuleName().equals(CONTACTS_MODULE)) {
//			return searchRecords("Email", values);	
//		}
//		return null;
//	}

}
