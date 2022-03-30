package xyz.oapps.osync.handler.outreach;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

import xyz.oapps.osync.SyncHandler;
import xyz.oapps.osync.User;
import xyz.oapps.osync.fields.Fields;
import xyz.oapps.osync.fields.Record;
import xyz.oapps.osync.fields.RecordSet;
import xyz.oapps.osync.util.CommonUtil;

public class OutreachHandler extends SyncHandler {

	private static final Logger log = Logger.getLogger(OutreachHandler.class.getName());

	private static final String PROSPECT_MODULE = "Prospect";

	private static final String ACCOUNT_MODULE = "Accounts";

	private static final String PROSPECT_MODULE_URL = "https://api.outreach.io/api/v2/prospects";

	private static final String ACCOUNT_MODULE_URL = "https://api.outreach.io/api/v2/accounts";

	private static final String CURRENT_USER_URL = "https://api.outreach.io/api/v2/users";

	private static final int firstElement_Array = 0;
	
	private static final String short_name = "or_hl";

	// private static final String CF_PREFIX = "custom_fields_";

	public Fields getFields() {

		Fields field = Fields.of();
		if ((ACCOUNT_MODULE).equals(getModuleName())) {
			field = getAccountFields(field);
			field = fetchCustomFields(field);
		} else if (PROSPECT_MODULE.equals(getModuleName())) {
			field = getProspectsFields(field);
			field = fetchCustomFields(field);
		}
		return field;
	}

	private Fields getAccountFields(Fields fields) {
		fields.doubleField("buyerIntentScore", "Buyer Intent Score", false);
		fields.text("companyType", "Company Type", false);
		fields.text("description", "Description", false);
		fields.text("domain", "Domain", false);
		fields.text("foundedAt", "FOUNDED MONTH / FOUNDED YEAR", false);
		fields.text("industry", "Industry", false);
		fields.text("linkedInUrl", "LinkedInUrl", false);
		fields.text("locality", "Locality", false);
		fields.text("name", "Name", true);
		fields.text("naturalName", "NaturalName", false);
		fields.number("numberOfEmployees", "Number of Employess", false);
		fields.text("tags", "Tags", false);
		fields.text("websiteUrl", "WebSite URL", false);

		return fields;

	}

	private Fields getProspectsFields(Fields fields) {

		try {
			fields.text("addressCity", "Address City", false);
			fields.text("addressCountry", "Address Country", false);
			fields.text("addressState", "Address State", false);
			fields.text("addressStreet", "Address Street", false);
			fields.text("addressStreet2", "Address Street2", false);
			fields.text("addressZip", "Address Zip", false);
			fields.date("availableAt", "Available AT", false, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			fields.text("callsOptStatus", "Calls Opt Status", false);
			fields.text("company", "Company", false);
			fields.text("companyFollowers", "Company Followers", false);
			fields.text("companyIndustry", "Company Industry", false);
			fields.text("companyLinkedIn", "company Linked In", false);
			fields.text("companyLocality", "Company Locality", false);
			fields.text("companyNatural", "Company Natural", false);
			fields.text("companySize", "Company Size", false);
			fields.text("companyType", "Company Type", false);
			fields.date("dateOfBirth", "Date Of Birth", false, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			fields.text("degree", "Degree", false);
			fields.text("emails", "Emails", true);
			fields.text("emailsOptStatus", "Emails OptStatus", false);
			fields.text("facebookUrl", "FaceBook Url", false);
			fields.text("firstName", "First Name", false);
			fields.text("gender", "Gender", false);
			fields.text("githubUrl", "Github Url", false);
			fields.text("githubUsername", "Github Username", false);
			fields.text("googlePlusUrl", "GooglePlus Url", false);
			fields.text("graduationDate", "GraduationDate", false);
			fields.text("homePhones", "Home Phones", false);
			fields.date("jobStartDate", "JobStartDate", false, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			fields.text("lastName", "LastName", false);
			fields.text("linkedInId", "LinkedIn Id", false);
			fields.text("linkedInUrl", "LinkedIn Url", false);
			fields.text("middleName", "MiddleName", false);
			fields.text("mobilePhones", "Mobile Phones", false);
			fields.text("nickname", "Nick Name", false);
			fields.text("occupation", "Occupation", false);
			fields.bool("optedOut", "Opted Out", false);
			fields.text("otherPhones", "Other Phones", false);
			fields.text("personalNote1", "Personal Note1", false);
			fields.text("personalNote2", "personal Note2", false);
			fields.text("region", "Region", false);
			fields.text("school", "School", false);
			fields.text("score", "Score", false);
			fields.text("smsOptStatus", "Sms Op tStatus", false);
			fields.text("source", "Source", false);
			// Need to check speciality
			fields.text("specialties", "Specialties", false);
			fields.text("stackOverflowUrl", "StackOver Flow Url", false);
			fields.text("tags", "Tags", false);
			fields.text("timeZone", "Time Zone", false);
			fields.text("title", "Title", false);
			fields.text("twitterUrl", "Twitter Url", false);
			fields.text("twitterUsername", "Twitter Username", false);
			fields.text("voipPhones", "Voip Phones", false);
			fields.text("websiteUrl1", "website URL1", false);
			fields.text("websiteUrl2", "Website URL2", false);
			fields.text("websiteUrl3", "WebSite URL3", false);
			fields.text("workPhones", "Work Phones", false);

			return fields;
		} catch (Exception e) {
			e.printStackTrace();
			CommonUtil.logOsyncInfo(short_name,e.getMessage());
		}

		return null;

	}

	private String getModuleUrl() {
		String url = "";
		if ((ACCOUNT_MODULE).equals(getModuleName())) {
			url = ACCOUNT_MODULE_URL;
		} else if (PROSPECT_MODULE.equals(getModuleName())) {
			url = PROSPECT_MODULE_URL;
		}
		return url;
	}

	@Override
	public HashMap<String, String> createNewRecords(RecordSet recordSet, String syncFrom) {

		HashMap<String, String> uvMap = new HashMap<String, String>();
		JSONObject Json = new JSONObject();
		String response = "";
		for (Record record : recordSet) {
			try {
				CommonUtil.logOsyncInfo(short_name,"creating this: " + Json.toString());
				JSONObject dataObject = new JSONObject();
				JSONObject childObject = new JSONObject();
				if (PROSPECT_MODULE.equals(getModuleName())) {
					childObject.put("type", "prospect");
					Json = prospectRecordSetConstructor(record);
				} else if (ACCOUNT_MODULE.equals(getModuleName())) {
					childObject.put("type", "account");
					Json = accountRecordSetConstructor(record);
				}
				childObject.put("attributes", Json);
				dataObject.put("data", childObject);
				response = invoker().post(getModuleUrl(), null, null, dataObject);
				String id = getRecordId(response);
				if (id != null) {
					uvMap.put(record.getMappedRecordUniqueValue(), id);
				}

			} catch (Exception e) {
				e.printStackTrace();
				CommonUtil.logOsyncInfo(short_name,e.getMessage());
			}

		}

		return uvMap;

	}

	@Override
	public void updateRecords(RecordSet recordSet, String syncFrom) {
		HashMap<String, String> uvMap = new HashMap<String, String>();
		JSONObject Json = new JSONObject();
		String response = "";
		int recordId = 0;
		for (Record record : recordSet) {
			try {
				JSONObject dataObject = new JSONObject();
				JSONObject childObject = new JSONObject();
				if (PROSPECT_MODULE.equals(getModuleName())) {
					childObject.put("type", "prospect");
					Json = prospectRecordSetConstructor(record);
				} else if (ACCOUNT_MODULE.equals(getModuleName())) {
					childObject.put("type", "account");
					Json = accountRecordSetConstructor(record);
				}
				String id = record.getUniqueValue();
				if (id != null) {
					recordId = Integer.parseInt(id);
				}
				childObject.put("attributes", Json);
				childObject.put("id", recordId);
				dataObject.put("data", childObject);
				response = invoker().patch(getModuleUrl() + "/" + record.getUniqueValue(), null, null, dataObject);

			} catch (Exception e) {
				e.printStackTrace();
				CommonUtil.logOsyncInfo(short_name,e.getMessage());
			}
		}

	}

	public RecordSet getMatchedRecordsById(List<String> recordsToFetchRemote) {
		RecordSet recordSet = RecordSet.init("outReach", "id");
		String URL = "";

		try {
			StringBuffer entityIds = new StringBuffer();
			for (String entityId : recordsToFetchRemote) {
				entityIds.append(entityId).append(",");
			}
			if ((ACCOUNT_MODULE).equals(getModuleName())) {
				URL = "https://api.outreach.io/api/v2/accounts?filter[id]=" + entityIds;
			} else if (PROSPECT_MODULE.equals(getModuleName())) {
				URL = "https://api.outreach.io/api/v2/prospects?filter[id]=" + entityIds;
			}
			String response = invoker().get(URL, null, null, null);

			if (response != null) {
				JSONObject json = new JSONObject(response);
				JSONObject metaData = json.getJSONObject("meta");
				int count = metaData.getInt("count");
				if (count > 0) {
					JSONArray mailDataArrays = json.getJSONArray("data");
					for (int i = 0; i < mailDataArrays.length(); i++) {
						JSONObject dataObject = mailDataArrays.getJSONObject(i);
						JSONObject Json = dataObject.getJSONObject("attributes");
						Long modifiedTime = getModifiedTimeForOutReach(Json);
						Record record = recordSet.add(dataObject.optString("id"), modifiedTime);
						Iterator<String> iterator = Json.keys();
						while (iterator.hasNext()) {

							String key = iterator.next();
							if (key.startsWith("custom")) {
								// record.addOrUpdateValue(CF_PREFIX + key, Json.get(key));
								record.addOrUpdateValue(key, Json.get(key));
							} else {
								if (key.equals("emails") || key.equals("homePhones") || key.equals("mobilePhones")
										|| key.equals("voipPhones") || key.equals("otherPhones")) {
									JSONArray array = Json.getJSONArray(key);
									if (array.length() > 0) {
										String item = array.get(0).toString();
										record.addOrUpdateValue(key, item);
									} else {
										record.addOrUpdateValue(key, "");
									}

								} else if ((key.equals("dateOfBirth") || key.equals("jobStartDate"))) {
									String date = dateFormatConvertionToZOHO(Json.get(key).toString());
									record.addOrUpdateValue(key, date);
								} else {
									record.addOrUpdateValue(key, Json.get(key));
								}

							}

						}
					}
				}
			}

			return recordSet;
		} catch (IOException e) {
			e.printStackTrace();
			CommonUtil.logOsyncInfo(short_name,e.getMessage());
		} catch (NumberFormatException e) {
			e.printStackTrace();
			CommonUtil.logOsyncInfo(short_name,e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			CommonUtil.logOsyncInfo(short_name,e.getMessage());
		}

		return null;

	}

	@Override
	public RecordSet fetchRecords(int startPage, int totalRecords, Long lastSyncTime) {
		String updatedtimePage = "";
		String appendUpdatedAtAPI = "";
		RecordSet recordSet = RecordSet.init("outReach", "id");

		CommonUtil.logOsyncInfo(short_name,"last sync time" + lastSyncTime.toString());
		if (lastSyncTime != null && lastSyncTime.longValue() != -1) {
			updatedtimePage = toISO8601Format(lastSyncTime);
			appendUpdatedAtAPI = "&filter[updatedAt]=" + updatedtimePage + "..inf";
		} else {
			appendUpdatedAtAPI = "";
		}

		try {
			CommonUtil.logOsyncInfo(short_name,"last sync time" + updatedtimePage);
			String URL = "";
			if ((PROSPECT_MODULE).equals(getModuleName())) {
				URL = "https://api.outreach.io/api/v2/prospects?page[size]=" + totalRecords + appendUpdatedAtAPI;

			} else if (ACCOUNT_MODULE.equals(getModuleName())) {
				URL = "https://api.outreach.io/api/v2/accounts?page[size]=" + totalRecords + appendUpdatedAtAPI;

			}
			CommonUtil.logOsyncInfo(short_name,"Fetch Records URL::>>" + URL);
			String response = invoker().get(URL, null, null, null);
			if (response.length() > 0) {
				JSONObject json = new JSONObject(response);
				JSONObject metaData = json.getJSONObject("meta");
				int count = metaData.getInt("count");
				if (count > 0) {
					JSONArray accountArrays = json.getJSONArray("data");
					for (int i = 0; i < accountArrays.length(); i++) {
						JSONObject masterObj = accountArrays.optJSONObject(i);
						String id = masterObj.get("id").toString();
						JSONObject prospect = masterObj.getJSONObject("attributes");
						Long modifiedTime = getModifiedTimeForOutReach(prospect);
						Record record = recordSet.add(id, modifiedTime);
						Iterator<String> iterator = prospect.keys();
						while (iterator.hasNext()) {

							String key = iterator.next();
							if (key.startsWith("custom")) {
								// record.addOrUpdateValue(CF_PREFIX + key, prospect.get(key));
								record.addOrUpdateValue(key, prospect.get(key));
							} else {
								if (key.equals("emails") || key.equals("homePhones") || key.equals("mobilePhones")
										|| key.equals("voipPhones") || key.equals("otherPhones")) {
									JSONArray array = prospect.getJSONArray(key);
									if (array.length() > 0) {
										String item = array.get(0).toString();
										record.addOrUpdateValue(key, item);
									} else {
										record.addOrUpdateValue(key, "");
									}

								} else if ((key.equals("dateOfBirth") || key.equals("jobStartDate"))) {
									String date = dateFormatConvertionToZOHO(prospect.get(key).toString());
									record.addOrUpdateValue(key, date);
								} else {
									record.addOrUpdateValue(key, prospect.get(key));
								}
							}
						}

					}
				}

				return recordSet;
			}
		} catch (IOException e) {
			e.printStackTrace();
			CommonUtil.logOsyncInfo(short_name,e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			CommonUtil.logOsyncInfo(short_name,e.getMessage());
		}

		return null;

	}

	@Override
	public RecordSet getMatchedRecordsByUniqueColumn(Collection<String> values) {
		try {
			String response = "";
			String URL = "";
			StringBuffer mailId = new StringBuffer();
			RecordSet recordSet = RecordSet.init("outReach", "id");

			CommonUtil.logOsyncInfo(short_name,"values::>>" + values);
			for (String entityId : values) {
				mailId.append(entityId).append(",");
			}

			CommonUtil.logOsyncInfo(short_name,"values::>>" + mailId);
			if ((ACCOUNT_MODULE).equals(getModuleName())) {
				URL = "https://api.outreach.io/api/v2/accounts?filter[emails]=" + mailId;
			} else if (PROSPECT_MODULE.equals(getModuleName())) {
				URL = "https://api.outreach.io/api/v2/prospects?filter[emails]=" + mailId;

			}
			response = invoker().get(URL, null, null, null);

			if (response != null) {
				JSONObject json = new JSONObject(response);
				JSONObject metaData = json.getJSONObject("meta");
				int count = metaData.getInt("count");
				if (count > 0) {
					JSONArray mailDataArrays = json.getJSONArray("data");
					for (int i = 0; i < mailDataArrays.length(); i++) {
						JSONObject dataObject = mailDataArrays.getJSONObject(i);
						JSONObject prospectObj = dataObject.getJSONObject("attributes");
						Long modifiedTime = getModifiedTimeForOutReach(prospectObj);
						Record record = recordSet.add(prospectObj.optString("id"), modifiedTime);
						Iterator<String> iterator = prospectObj.keys();
						while (iterator.hasNext()) {

							String key = iterator.next();
							if (key.startsWith("custom")) {
								// record.addOrUpdateValue(CF_PREFIX + key, prospectObj.get(key));
								record.addOrUpdateValue(key, prospectObj.get(key));
							} else {

								if (key.equals("emails") || key.equals("homePhones") || key.equals("mobilePhones")
										|| key.equals("voipPhones") || key.equals("otherPhones")) {
									JSONArray array = prospectObj.getJSONArray(key);
									if (array.length() > 0) {
										String item = array.get(0).toString();
										record.addOrUpdateValue(key, item);
									} else {
										record.addOrUpdateValue(key, "");
									}

								} else if ((key.equals("dateOfBirth") || key.equals("jobStartDate"))) {
									String date = dateFormatConvertionToZOHO(prospectObj.get(key).toString());
									record.addOrUpdateValue(key, date);
								} else {
									record.addOrUpdateValue(key, prospectObj.get(key));
								}

							}

						}
					}

				}
			}

			return recordSet;

		} catch (Exception e) {
			e.printStackTrace();
			CommonUtil.logOsyncInfo(short_name,e.getMessage());
		}

		return null;

	}

	@Override
	public User getCurrentUser() {

		try {
			JSONObject param = new JSONObject();
			param.put("type", "CurrentUser");
			String response = invoker().get(CURRENT_USER_URL, null, param, null);
			JSONObject json = new JSONObject(response);
			JSONArray mailDataArrays = json.getJSONArray("data");
			JSONObject dataObject = mailDataArrays.getJSONObject(firstElement_Array);
			JSONObject userObject = dataObject.getJSONObject("attributes");
			String email = userObject.getString("email");
			String fullName = userObject.getString("firstName").concat(userObject.getString("lastName"));
			String uniquId = userObject.getString("id");
			User user = new User(email);
			user.setUniqueUserId(uniquId);
			user.setFullName(fullName);
			user.setEmail(email);
			user.setFullJson(userObject);
			return user;
		} catch (Exception e) {
			log.log(Level.SEVERE, null, e);
			e.printStackTrace();
		}
		return null;
	}

	private Fields fetchCustomFields(Fields field) {

		try {
			String customResponse = invoker().get("https://api.outreach.io/api/v2", null, null, null);
			String customerDisplayId = "";
			JSONObject jsonObject = new JSONObject(customResponse);
			JSONObject dataObject = jsonObject.getJSONObject("data");
			JSONObject attribute = dataObject.getJSONObject("attributes");

			Iterator<String> iterator = attribute.keys();

			if (iterator != null) {
				while (iterator.hasNext()) {
					String key = iterator.next();
					if (PROSPECT_MODULE.equals(getModuleName())) {
						if (key.startsWith("prospectLabel")) {
							String labelName = attribute.get(key).toString();
							if (!labelName.equals("null") && !labelName.isEmpty()) {
								// labelName = CF_PREFIX + labelName;
								customerDisplayId = "custom" + key.substring(13);
								// customerDisplayId = CF_PREFIX + customerDisplayId;
								CommonUtil.logOsyncInfo(short_name,"customerDisplayId::" + customerDisplayId);
								CommonUtil.logOsyncInfo(short_name,"labelName::>>>>>" + labelName);
								field.text(customerDisplayId, labelName, false);
							}
						}
					}
					if (ACCOUNT_MODULE.equals(getModuleName())) {
						if (key.startsWith("accountLabel")) {
							String labelName = attribute.get(key).toString();
							if (!labelName.equals("null") && !labelName.isEmpty()) {
								// labelName = CF_PREFIX + labelName;
								customerDisplayId = "custom" + key.substring(12);
								// customerDisplayId = CF_PREFIX + customerDisplayId;
								CommonUtil.logOsyncInfo(short_name,"labelName::>>>>>" + labelName);
								CommonUtil.logOsyncInfo(short_name,"customerDisplayId::" + customerDisplayId);
								field.text(customerDisplayId, labelName, false);
							}
						}
					}
				}
			}

			return field;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getRecordId(String response) {

		try {
			JSONObject obj = new JSONObject(response);
			if (obj.has("data")) {
				return obj.optJSONObject("data").optString("id");
			} else {
				log.severe("error on create record : " + obj);
			}

		} catch (JSONException e) {
			log.severe("response on create record : " + response);
			e.printStackTrace();
		}
		return null;
	}

	public JSONObject prospectRecordSetConstructor(Record record) {

		String response = "";
		String emails = "";
		String homePhones = "";
		String mobilePhones = "";
		String otherPhones = "";
		String voipPhones = "";
		String workPhones = "";

		try {
			JSONObject peopleJson = record.columnValuesAsJson();
			if (record.getValue("emails") != null) {
				emails = record.getValue("emails").toString();
			}
			if (record.getValue("homePhones") != null) {
				homePhones = record.getValue("homePhones").toString();
			}
			if (record.getValue("mobilePhones") != null) {
				mobilePhones = record.getValue("mobilePhones").toString();
			}
			if (record.getValue("otherPhones") != null) {
				otherPhones = record.getValue("otherPhones").toString();
			}
			if (record.getValue("voipPhones") != null) {
				voipPhones = record.getValue("voipPhones").toString();
			}
			if (record.getValue("workPhones") != null) {
				voipPhones = record.getValue("workPhones").toString();
			}
			if (!emails.isEmpty()) {
				JSONArray emailJsonArray = new JSONArray();
				emailJsonArray.put(emails);
				peopleJson.remove("emails");
				peopleJson.put("emails", emailJsonArray);
			}
			if (!homePhones.isEmpty()) {
				JSONArray homePhonesJsonArray = new JSONArray();
				homePhonesJsonArray.put(homePhones);
				peopleJson.remove("homePhones");
				peopleJson.put("homePhones", homePhonesJsonArray);
			}
			if (!mobilePhones.isEmpty()) {
				JSONArray mobilePhonesJsonArray = new JSONArray();
				mobilePhonesJsonArray.put(mobilePhones);
				peopleJson.remove("mobilePhones");
				peopleJson.put("mobilePhones", mobilePhonesJsonArray);
			}
			if (!otherPhones.isEmpty()) {
				JSONArray otherPhonesJsonArray = new JSONArray();
				otherPhonesJsonArray.put(otherPhones);
				peopleJson.remove("otherPhones");
				peopleJson.put("otherPhones", otherPhonesJsonArray);
			}
			if (!voipPhones.isEmpty()) {
				JSONArray voipPhonessJsonArray = new JSONArray();
				voipPhonessJsonArray.put(voipPhones);
				peopleJson.remove("voipPhones");
				peopleJson.put("voipPhones", voipPhonessJsonArray);
			}
			if (!workPhones.isEmpty()) {
				JSONArray vworkPhonesJsonArray = new JSONArray();
				vworkPhonesJsonArray.put(voipPhones);
				peopleJson.remove("workPhones");
				peopleJson.put("workPhones", vworkPhonesJsonArray);
			}
			return peopleJson;
		} catch (Exception e) {
			e.printStackTrace();
			CommonUtil.logOsyncInfo(short_name,e.getMessage());
			return null;
		}
	}

	public JSONObject accountRecordSetConstructor(Record record) {

		String tags = "";
		try {

			JSONObject accountJson = record.columnValuesAsJson();
			if (record.getValue("tags") != null) {
				tags = record.getValue("emails").toString();
			}
			if (!tags.isEmpty()) {
				JSONArray tagsArray = new JSONArray();
				tagsArray.put(tags);
				accountJson.remove("emails");
				accountJson.put("emails", tagsArray);
			}
			return accountJson;
		} catch (Exception e) {
			e.printStackTrace();
			CommonUtil.logOsyncInfo(short_name,e.getMessage());
			return null;
		}
	}

	private String dateFormatConvertionToZOHO(String outReachdateFormat) {
		String zohoDatFormat = null;
		{
			if (!outReachdateFormat.equals("null")) {
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				DateFormat outputformat = new SimpleDateFormat("yyyy-MM-dd");
				Date date = null;
				try {
					date = df.parse(outReachdateFormat);
					zohoDatFormat = outputformat.format(date);

				} catch (ParseException pe) {
					pe.printStackTrace();
					CommonUtil.logOsyncInfo(short_name,pe.getMessage());
				}
				return zohoDatFormat;
			}
			return null;
		}
	}

}
