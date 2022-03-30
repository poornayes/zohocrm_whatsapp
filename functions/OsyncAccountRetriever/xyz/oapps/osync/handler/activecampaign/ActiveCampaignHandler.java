package xyz.oapps.osync.handler.activecampaign;

import xyz.oapps.osync.OsyncException;
import xyz.oapps.osync.SyncHandler;
import xyz.oapps.osync.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import xyz.oapps.osync.fields.Fields;
import xyz.oapps.osync.fields.Record;
import xyz.oapps.osync.fields.RecordSet;
import xyz.oapps.osync.util.HttpUtil;
import xyz.oapps.osync.invoker.*;

public class ActiveCampaignHandler extends SyncHandler {

	private static final Logger log = Logger.getLogger(ActiveCampaignHandler.class.getName());

	private static final String CF_NAME = "fields";

	private static final String CF_PREFIX = "fields";

	private static final String CURRENT_USER_URL = "https://oapps10940.api-us1.com/api/3/users";

//	public static Map<String,String> getAuthMap(){
//		HashMap<String,String> map= new HashMap<String,String>();
//		map.put("Api-Token", "c494acb199718c8662eb7505c20bcadeb8ff79c7b22f9d33e3e5da98e0c0ab44bd1a0292");
//		map.put("Accept-Language", "en-US,en;q=0.5");
//		map.put("Content-Type","application/json");
//		map.put("Charset", "UTF-8");
//		
//		return map;
//		
//	}

	@Override
	public RecordSet fetchRecords(int startPage, int totalRecords, Long startTime) throws OsyncException {
		try {

//			JSONObject queryJson = new JSONObject();
//			queryJson.put("Api-Token", "c494acb199718c8662eb7505c20bcadeb8ff79c7b22f9d33e3e5da98e0c0ab44bd1a0292");
//			queryJson.put("Content-Type", "application/json");
//       		queryJson.put("Charset", "UTF-8");
			
			String response = "";
			String URL = "";
			StringBuffer mailId = new StringBuffer();
			RecordSet recordSet = RecordSet.init("ActiveCampaign", "id");

			log.info("values::>>" + mailId);
			if (("Contacts").equals(getModuleName())) {
				URL = "https://oapps10940.api-us1.com/api/3/contacts?status=-1&orders%5Bemail%5D=ASC'";
			}
			
			log.info("Fetch Records URL::>>" + URL);
			response = invoker().get(URL, null, null, null);

			System.out.println(response);

			JSONObject json = new JSONObject(response);
			JSONArray accountArrays = json.getJSONArray("contacts");
			JSONObject metaJson = json.getJSONObject("meta");
			int total = metaJson.getInt("total");
			if (total > 0) {

				for (int i = 0; i < accountArrays.length(); i++) {
					JSONObject json1 = accountArrays.getJSONObject(i);
					Iterator<String> keys = json1.keys();

					while (keys.hasNext()) {
						String key = keys.next();
						System.out.println("Key: " + key + "  Value: " + json1.get(key));
					}

				}
			}
			return recordSet;

		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();

		}

		return null;
	}

	public HashMap<String, String> createNewRecords(RecordSet recordSet, String syncFrom) {
		try {
//			JSONObject queryJson = new JSONObject();
//			queryJson.put("Api-Token", "c494acb199718c8662eb7505c20bcadeb8ff79c7b22f9d33e3e5da98e0c0ab44bd1a0292");
//			queryJson.put("Content-Type", "application/json");

			HashMap<String, String> map = new HashMap<String, String>();
			JSONObject Json = new JSONObject();
			for (Record record : recordSet) {
				try {
					log.info("creating this: " + Json.toString());
					JSONObject dataObject = new JSONObject();
					JSONObject peopleJson = record.columnValuesAsJson();
					System.out.println("people111==" + peopleJson);

					JSONObject dataJson = new JSONObject();
					dataJson.put("contact", peopleJson);

					System.out.println("dataObject111==" + dataJson);
					// System.out.println("queryJson==" + queryJson);
					String response = invoker().post("https://oapps10940.api-us1.com/api/3/contacts", null, null,
							dataJson);
					System.out.println("response==" + response);

				} catch (Exception e) {
					e.printStackTrace();
					log.info(e.getMessage());
				}

			}

			return map;
		} catch (Exception e) {
			e.printStackTrace();

		}
		return null;
	}

	public void updateRecords(RecordSet recordSet, String syncFrom) {
		System.out.println("--------------------------------------------------------");

		HashMap<String, String> map = new HashMap<String, String>();

		JSONObject Json = new JSONObject();
		String response = "";
		int recordId = 0;
		for (Record record : recordSet) {
			try {
//				JSONObject queryJson =  new JSONObject();
//				queryJson.put("Api-Token", "1ca7f3756ed5113dc03d43771dc6ec1e710352e2e08ffbee0b22069a6825d11871315c4e");
//				JSONObject jsonTemp = new JSONObject();
				JSONObject jsonObject = new JSONObject();
				String id = record.getUniqueValue();
				System.out.println(id);
				jsonObject.put("contact", record.columnValuesAsJson());
				String url = "https://oapps33834.api-us1.com/api/3/contacts/";
				System.out.println("url===" + url);
				System.out.println("jsonObject===" + jsonObject);
				System.out.println("======" + jsonObject.toString());

				response = invoker().put("https://oapps10940.api-us1.com/api/3/contacts/" + id, null, null, jsonObject);
				System.out.println(response);

			} catch (Exception e) {
				e.printStackTrace();
				log.info(e.getMessage());
			}

		}

		// return map;

	}

	public static void main(String[] args) {
		try {

			ActiveCampaignHandler ActiveCampaignHandler = new ActiveCampaignHandler();
			ActiveCampaignHandler.testCreateNewRecords();
			ActiveCampaignHandler.fetchRecords(1, 100, null);
			// ActiveCampaignHandler.testUpdateRecords();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void testCreateNewRecords() {
		try {
			RecordSet rs = RecordSet.init("ActiveCampaign", "id");
			List<String> contactsCreated = new ArrayList<String>();

			List<String> emailsCreated = new ArrayList<String>();
			for (int i = 0; i <= 1; i++) {

				Record record = rs.createEmptyObject();
				String randomValue = "Test Z" + i + " " + System.currentTimeMillis();
				String firstName = "Santhosh " + i;
				record.addOrUpdateValue("firstName", firstName);
				String lastName = "Kiran " + i;
				record.addOrUpdateValue("lastName", lastName);
				String email = randomValue.replace(" ", "").toLowerCase() + "@oappsxyz.com";
				emailsCreated.add(email);
				record.addOrUpdateValue("email", email);
				record.setMappedRecordUniqueValue(randomValue.replace(" ", "").toLowerCase());
			}

			createNewRecords(rs, "syncFrom");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void testUpdateRecords() {

		try {
			RecordSet rs = RecordSet.init("ActiveCampaign", "id");
			List<String> ids = new ArrayList<String>();
			ids.add("8");
			Record rec = rs.createEmptyObject();
			rec.setUniqueValue("8");
			rec.addOrUpdateValue("lastName", " Updated3");
			updateRecords(rs, "syncFrom");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public Fields getFields() throws OsyncException {
		System.out.println("hiii");
		Fields field = Fields.of();
		if (("Contacts").equals(getModuleName())) {

			field.text("email", "Email", true);
			field.text("firstName", "First Name", false);
			field.text("lastName", "Last Name", false);
			field.text("phone", "Phone Number", false);

		}
		return field;
	}

	@Override
	public RecordSet getMatchedRecordsById(List<String> recordsToFetchRemote) throws OsyncException {
		RecordSet recordSet = RecordSet.init("ActiveCampaign", "id");
		String URL = "";

		try {
			StringBuffer entityIds = new StringBuffer();
			for (String entityId : recordsToFetchRemote) {
				entityIds.append(entityId).append(",");
			}
			if (("Contacts").equals(getModuleName())) {
				URL = "https://oapps10940.api-us1.com/api/3/contacts?filters[id]=" + entityIds;
			}
			String response = invoker().get(URL, null, null, null);

			if (response != null) {
				JSONObject json = new JSONObject(response);
				JSONArray accountArrays = json.getJSONArray("contacts");
				JSONObject metaData = json.getJSONObject("meta");
				int total = metaData.getInt("total");
				if (total > 0) {
					for (int i = 0; i < accountArrays.length(); i++) {
						JSONObject json1 = accountArrays.getJSONObject(i);
						Iterator<String> keys = json1.keys();

						while (keys.hasNext()) {
							String key = keys.next();
							System.out.println("Key: " + key + "  Value: " + json1.get(key));
						}

					}
				}
			}

			return recordSet;
		} catch (IOException e) {
			e.printStackTrace();
			log.info(e.getMessage());
		} catch (NumberFormatException e) {
			e.printStackTrace();
			log.info(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			log.info(e.getMessage());
		}

		return null;
	}

	private Fields fetchCustomFields(Fields field, String module) throws OsyncException {
		try {
			boolean hasNextPage = true;
			int page = 1;

			while (hasNextPage) {
				JSONObject params = new JSONObject();
				params.put("limit", 100);
				String customResponse = invoker().get("https://oapps10940.api-us1.com/api/3/fields", null, params,
						null);

				JSONObject customObject = new JSONObject(customResponse);
				JSONArray customArrays = customObject.getJSONArray("fields");
				for (int i = 0; i < customArrays.length(); i++) {
					JSONObject customObj = customArrays.optJSONObject(i);
					String type = customObj.get("type").toString();
					String title = customObj.get("title").toString();
					String descript = customObj.get("descript").toString();
					String id = CF_PREFIX;
					log.info("type==>>" + type);
					log.info("title==>>" + title);
					log.info("descript==>>" + descript);
					if (("textarea").equals(type)) {
						if (("Field Title").equals(title)) {
							field.text(id, title, false);
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

	@Override
	public User getCurrentUser() throws OsyncException {
		try {
			JSONObject param = new JSONObject();
			param.put("type", "CurrentUser");
			String response = invoker().get(CURRENT_USER_URL + "/me", null, param, null);
			System.out.println(response);
			JSONObject userObject = (new JSONObject(response)).getJSONObject("user");
			String email = userObject.getString("email");
			String fullName = userObject.getString("firstName").concat(userObject.getString("lastName"));
			String uniquId = userObject.getString("id");
			User user = new User(email);
			user.setUniqueUserId(uniquId);
			user.setFullName(fullName);
			user.setEmail(email);
			user.setFullJson(userObject);
			return user;
		} catch (OsyncException e) {
			throw e;
		} catch (Exception e) {
			log.log(Level.SEVERE, null, e);
		}
		return null;
	}

	@Override
	public RecordSet getMatchedRecordsByUniqueColumn(Collection<String> values) throws OsyncException {
		try {
			String response = "";
			String URL = "";
			StringBuffer mailId = new StringBuffer();
			RecordSet recordSet = RecordSet.init("ActiveCampaign", "id");

			log.info("values::>>" + values);
			for (String entityId : values) {
				mailId.append(entityId).append(",");
			}

			log.info("values::>>" + mailId);
			if (("Contacts").equals(getModuleName())) {
				URL = "https://oapps10940.api-us1.com/api/3/contacts?filters[email]=" + mailId;
			}
			response = invoker().get(URL, null, null, null);

			if (response != null) {
				JSONObject json = new JSONObject(response);
				JSONArray accountArrays = json.getJSONArray("contacts");
				JSONObject metaData = json.getJSONObject("meta");
				int total = metaData.getInt("total");
				if (total > 0) {
					for (int i = 0; i < accountArrays.length(); i++) {
						JSONObject json1 = accountArrays.getJSONObject(i);
						Iterator<String> keys = json1.keys();

						while (keys.hasNext()) {
							String key = keys.next();
							System.out.println("Key: " + key + "  Value: " + json1.get(key));
						}

					}
				}
			}
			return recordSet;

		} catch (Exception e) {
			e.printStackTrace();
			log.info(e.getMessage());
		}
		return null;
	}

}
