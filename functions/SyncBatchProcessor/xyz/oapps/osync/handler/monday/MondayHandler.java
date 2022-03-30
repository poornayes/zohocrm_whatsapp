package xyz.oapps.osync.handler.monday;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.text.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import xyz.oapps.osync.OsyncException;
import xyz.oapps.osync.SyncHandler;
import xyz.oapps.osync.User;
import xyz.oapps.osync.entity.ModuleInfoEntity;
import xyz.oapps.osync.fields.Fields;
import xyz.oapps.osync.fields.Record;
import xyz.oapps.osync.fields.RecordSet;
import xyz.oapps.osync.util.CommonUtil;

public class MondayHandler extends SyncHandler{
	private static final Logger log = Logger.getLogger(MondayHandler.class.getName());
	private static String uniquecolid = "";
	private static final String short_name = "my_hl";
	public Fields getFields() throws OsyncException {
		Fields field = Fields.of();
		String response = "";
		try {
			
			JSONObject queryJson =  new JSONObject();
			queryJson.put("query", "{ boards (ids:" + getModule().getModuleId() +" state:active) { columns {id title type}  } }");
			response = invoker().get("https://api.monday.com/v2", null, queryJson, null);

			JSONObject json = new JSONObject(response);
			JSONArray columnArray = json.getJSONObject("data").getJSONArray("boards").getJSONObject(0).getJSONArray("columns");

			for (int i=0; i < columnArray.length(); i++) {
				
				String columnType = columnArray.getJSONObject(i).getString("type");
				String columnName = columnArray.getJSONObject(i).getString("title");
				String columnID = columnArray.getJSONObject(i).getString("id");
				
				if (columnType.equalsIgnoreCase("name")) {
					field.text(columnID, columnName, true);

				} else if (columnType.equalsIgnoreCase("numeric")) {
					field.number(columnID, columnName, false);

				} else if (columnType.equalsIgnoreCase("text") || (columnType.equalsIgnoreCase("long-text"))) {
					field.text(columnID, columnName, false);
				
				} else if (columnType.equalsIgnoreCase("phone")) {
					field.number(columnID, columnName, false);

				} else if (columnType.equalsIgnoreCase("date")) {
					field.date(columnID, columnName, false, "YYYY-MM-DD");

				} else if (columnType.equalsIgnoreCase("Boolean")) {
					field.bool(columnID, columnName, false);
					
				} else if (columnType.equalsIgnoreCase("link")) {
					field.text(columnID, columnName, false);
					
				} else if (columnType.equalsIgnoreCase("email")) {
					field.text(columnID, columnName, false);
				
				} 
			}
			return field;
		} catch (Exception e) {
			e.printStackTrace();
			CommonUtil.logOsyncInfo(short_name,e.getMessage());
		}
		return null;
	}

	public RecordSet fetchRecords(int page, int limit, Long lastSyncTime) {
		Long updated_atTime = null;
		RecordSet recordSet = RecordSet.init("monday", "id");
		CommonUtil.logOsyncInfo(short_name,"last sync time" + lastSyncTime.toString());
		try {
			if (lastSyncTime != null && lastSyncTime.longValue() != -1) {
				updated_atTime = lastSyncTime;
			}
			String response="";
			JSONObject queryJson =  new JSONObject();
			queryJson.put("query", "{ boards(ids:" + getModule().getModuleId() + " page:"+page+" limit:"+limit+" state:active){items { id name updated_at column_values {id text type value}}}}");

			System.out.print(" fetch queryJason :: "+queryJson);
			response = invoker().post("https://api.monday.com/v2", null, queryJson, null);

			CommonUtil.logOsyncInfo(short_name,"Response fetch Monday Items==>" + response);
			if (response != null) {
			JSONObject json = new JSONObject(response);
			JSONArray itemDataArrays = json.getJSONObject("data").getJSONArray("boards").getJSONObject(0).getJSONArray("items");
	
			for (int i = 0; i < itemDataArrays.length(); i++) {
				Long lastUpdateTime = getModifiedTimeForMonDay(itemDataArrays.optJSONObject(i));
				CommonUtil.logOsyncInfo(short_name,"last updated time" + lastUpdateTime);
				
				if (isNull(updated_atTime )) {
					addItemRecords(recordSet,itemDataArrays.optJSONObject(i));
					
				} else if (lastUpdateTime>updated_atTime) {
					addItemRecords(recordSet,itemDataArrays.optJSONObject(i));
				} 
			}	
			}
			return recordSet;
		} catch (IOException e) {
			CommonUtil.logOsyncInfo(short_name,"Exception>>>" + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
		CommonUtil.logOsyncInfo(short_name,"Exception>>>" + e.getMessage());
		e.printStackTrace();
		}
		return null;
	}
	
	private void addItemRecords(RecordSet recordSet, JSONObject itemObj) throws ParseException, JSONException {
		Long modifiedTime = getModifiedTimeForMonDay(itemObj);
		Record record = recordSet.add(itemObj.optString("id"), modifiedTime);
		Iterator<String> iterator = itemObj.keys();
		while (iterator.hasNext()) {
			String key = iterator.next();
			if (key.equalsIgnoreCase("column_values")) {
				JSONArray columnArray = itemObj.getJSONArray(key);
				for (int i=0 ; i<columnArray.length();i++) {
					JSONObject columnObj = columnArray.optJSONObject(i);
					if (columnObj.get("type").equals("boolean") && columnObj.get("text").equals("v")) {
						record.addOrUpdateValue(columnObj.get("id").toString(), "true");
					
					}else if (columnObj.get("type").equals("boolean") && columnObj.get("value").equals(null	)) {
						record.addOrUpdateValue(columnObj.get("id").toString(), "false");
						
					}else if(columnObj.get("type").equals("email") && !isNull(columnObj.get("value").toString())) {
						JSONObject jsonObj = new JSONObject(columnObj.get("value").toString());
						record.addOrUpdateValue(columnObj.get("id").toString(), jsonObj.getString("email"));
						
					}else if(columnObj.get("type").equals("link") && !isNull(columnObj.get("value").toString())) {
						JSONObject jsonObj = new JSONObject(columnObj.get("value").toString());
						record.addOrUpdateValue(columnObj.get("id").toString(), jsonObj.getString("url"));
						
					}else {
						record.addOrUpdateValue(columnObj.get("id").toString(),columnObj.get("text") );
					}
				}
				
			} else {
				record.addOrUpdateValue(key, itemObj.get(key));
			}
		}
	}

	private Long getModifiedTimeForMonDay(JSONObject itemObj) throws ParseException {
		String date = itemObj.optString("updated_at");
		// 2020-09-02 04:55:09 UTC
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date date2 = sdf.parse(date);
		return date2.getTime();
	}
	
	public HashMap<String, String> createNewRecords(RecordSet recordSet, String syncFrom) {
		HashMap<String, String> uvMap = new HashMap<String, String>();
		JSONObject Json = new JSONObject();
		for (Record record : recordSet) {
			try {
				String response = "";
				Json = itemRecordSetConstructor(record);
				String itemName = Json.get("name").toString();
				itemName= StringEscapeUtils.escapeJson(itemName).replace(";","%3B");
				Json.remove("name");
				if (Json != null) {
					String json = StringEscapeUtils.escapeJson(Json.toString());
					String columnValues ="mutation { create_item(board_id: " + getModule().getModuleId() + ", item_name:\""+itemName+"\", column_values:\"".concat(json).concat("\") {id}}");
					CommonUtil.logOsyncInfo(short_name,"creating this: " +columnValues);

					JSONObject queryJson =  new JSONObject();
					queryJson.put("query", columnValues);
					System.out.print("create queryJason :: "+queryJson);
					response = invoker().post("https://api.monday.com/v2", null, queryJson, null);
					CommonUtil.logOsyncInfo(short_name,"Response==>." + response);
					
					String id = getRecordId(response);
					if (id != null) {
						uvMap.put(record.getMappedRecordUniqueValue(), id);
					}
				} 
			}  catch (Exception e) {
				log.log(Level.SEVERE, "Exception while creating new items to Monday - " + syncFrom, e);
				CommonUtil.logOsyncInfo(short_name,"Continuing the next item..");
				e.printStackTrace();
			}
		}
		return uvMap;
	}
		
	private String getRecordId(String response) {
		try {
			JSONObject itemObj = new JSONObject(response);
			if (itemObj.has("data")) {
				return itemObj.optJSONObject("data").getJSONObject("create_item").optString("id");
			} else {
				log.severe("error on create item : " + itemObj);
			}
		} catch (JSONException e) {
			log.severe("response on create item : " + response);
			e.printStackTrace();
		}
		return null;
	}

	public JSONObject itemRecordSetConstructor(Record record) throws ParseException, JSONException{
		String phone="";
		String date ="";
		String check ="";
		String longText ="";
		String email = "";
		String link = "";
		
		try {
			JSONObject json = record.columnValuesAsJson();	
			List<Entry<String,Object>> emailIds = record.getColumnValues().entrySet().stream().filter(entry -> entry.getKey().contains("email")).collect(Collectors.toList());
			List<Entry<String, Object>> phoneNos= record.getColumnValues().entrySet().stream().filter(entry -> entry.getKey().contains("phone")).collect(Collectors.toList());
			List<Entry<String, Object>> longTexts = record.getColumnValues().entrySet().stream().filter(entry -> entry.getKey().contains("long")).collect(Collectors.toList());
			List<Entry<String, Object>> checkBoxes = record.getColumnValues().entrySet().stream().filter(entry -> entry.getKey().contains("check")).collect(Collectors.toList());
			List<Entry<String, Object>> dateColumn = record.getColumnValues().entrySet().stream().filter(entry -> entry.getKey().contains("date")).collect(Collectors.toList());
			List<Entry<String, Object>> URLs = record.getColumnValues().entrySet().stream().filter(entry -> entry.getKey().contains("link")).collect(Collectors.toList());
			
			if(emailIds!=null)
			{
				for(Entry<String, Object> emailId : emailIds)
				{
					String key = emailId.getKey();
					if (record.getValue(key) != null) {
						email = record.getValue(key).toString();
						JSONObject emailObj = new JSONObject();
						emailObj.put("email", email);
						emailObj.put("text", email);
						json.remove(key);
						json.put(key, emailObj);

					}
				 }
			 }
			 
			 if(URLs!=null)
			 {
				 for(Entry<String, Object> URL : URLs)
				 {
					 String key = URL.getKey();
					 if (record.getValue(key) != null) {
						 link = record.getValue(key).toString();
						 JSONObject linkObj = new JSONObject();
						 linkObj.put("url",link);
						 linkObj.put("text",link);
						 json.remove(key);
						 json.put(key, linkObj );
					}
				 }
			 }
			 			 
			 if(phoneNos!=null)
			 {
				 for(Entry<String, Object> phoneNo : phoneNos)
				 {
					 String key = phoneNo.getKey();
					 if (record.getValue(key) != null) {
						 phone = record.getValue(key).toString();
						 JSONObject phoneObj = new JSONObject();
						 phoneObj.put("phone", phone);
						 json.remove(key);
						 json.put(key, phoneObj);

					}
				 }
			 }		

			 if(checkBoxes !=null)
			 {
				 for(Entry<String, Object> checkBox : checkBoxes)
				 {
					 String key = checkBox.getKey();
					 if (record.getValue(key) != null) {
						 check = record.getValue(key).toString();
						 json.remove(key);
						 if (check.equalsIgnoreCase("true")) {
							 JSONObject checkObj = new JSONObject();
							 checkObj.put("checked", check);
							 json.put(key, checkObj);
						 } else  if (check.equalsIgnoreCase("false")){
							 json.put(key,json.NULL);
						 }
					}
				 }
			 }
			 
			 if(dateColumn !=null)
			 {
				 for(Entry<String, Object> dates : dateColumn)
				 {
					 String key = dates.getKey();
					 if (record.getValue(key) != null) {
						 date = record.getValue(key).toString();
						 JSONObject dateObj = new JSONObject();
						 dateObj.put("date", date);
						 json.remove(key);
						 json.put(key, dateObj );
					}
				 }
			 }
			 
			 if(longTexts !=null)
			 {
				 for(Entry<String, Object> longTextId : longTexts)
				 {
					 String key = longTextId.getKey();
					 if (record.getValue(key) != null) {
						 longText = record.getValue(key).toString();
						 JSONObject txtObj = new JSONObject(longText);
						 txtObj.put("text", longText);
						 json.remove(key);
						 json.put(key, txtObj);
					}
				 }
			 }
			 
			 return json;
		} catch (Exception e) {
			return null;
		}
	}
	
	public void updateRecords(RecordSet recordSet, String syncFrom) {
		JSONObject Json = new JSONObject();
		for (Record record : recordSet) {
			try {
				String response = "";
				Json = itemRecordSetConstructor(record);
				if (Json != null) {
					String json = StringEscapeUtils.escapeJson(Json.toString()).replace(";","%3B");
					String columnValues ="mutation { change_multiple_column_values(board_id: " + getModule().getModuleId() + ", item_id: "+record.getUniqueValue()+", column_values:\"".concat(json).concat("\") {id}}");
					CommonUtil.logOsyncInfo(short_name,"creating this: " +columnValues);
					                                                                                                                                                                      
					JSONObject queryJson =  new JSONObject();
					queryJson.put("query", columnValues);
					System.out.print("Update queryJason :: "+queryJson);
					response = invoker().post("https://api.monday.com/v2", null, queryJson, null);
					CommonUtil.logOsyncInfo(short_name,"Response==>>>>>>>>>>>>>>>>>>>" + response);
				} else {
					log.severe("Error while updating item : " + response);
				}
			} catch (Exception e) {
				log.log(Level.SEVERE, "Exception while updating new items to Monday - " + syncFrom, e);
				e.printStackTrace();
			}
		}
	}

	public RecordSet getMatchedRecordsById(List<String> itemsToFetchRemote) throws OsyncException {
		try {
			String response = "";
			RecordSet recordSet = RecordSet.init("monday", "id");
			StringBuffer entityIds = new StringBuffer();
			for (String entityId : itemsToFetchRemote) {
				entityIds.append(entityId).append(",");
			}
		
			JSONObject queryJson =  new JSONObject();
			queryJson.put("query", " { boards(ids:" + getModule().getModuleId() + "  state:active){items (ids:["+entityIds+"]) { id name updated_at column_values {id title text type value}}}}");
			System.out.print("Matched ID queryJason :: "+queryJson);
			response = invoker().post("https://api.monday.com/v2", null, queryJson, null);

			CommonUtil.logOsyncInfo(short_name,"Response getMatchedRecordsById Monday Items==>" + response);

			if (response != null) {
				JSONObject json = new JSONObject(response);
				JSONArray itemDataArrays = json.getJSONObject("data").getJSONArray("boards").getJSONObject(0).getJSONArray("items");
				for (int i = 0; i < itemDataArrays.length(); i++) {
					addItemRecords(recordSet, itemDataArrays.getJSONObject(i));
				}
			}
			return recordSet;
		} catch (OsyncException e) {
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			CommonUtil.logOsyncInfo(short_name,e.getMessage());
		} catch (NumberFormatException e) {
			e.printStackTrace();
			CommonUtil.logOsyncInfo(short_name,e.getMessage());
		} catch (Exception e) {
			CommonUtil.logOsyncInfo(short_name,"Exception updating IDS" + e);
			e.printStackTrace();
		}

		return null;
	}

		
	public RecordSet getMatchedRecordsByUniqueColumn(Collection<String> values) throws OsyncException {
		try {
			String response = "";
			StringBuffer mailId = new StringBuffer();
			RecordSet recordSet = RecordSet.init("monday", "id");

			CommonUtil.logOsyncInfo(short_name,"values::>>" + values);
			for (String entityId : values) {
				mailId.append(entityId).append(",");
			}

			CommonUtil.logOsyncInfo(short_name,"values::>>" + mailId);
			JSONObject queryJson =  new JSONObject();
			queryJson.put("query", "{ boards(ids:" + getModule().getModuleId() + " limit:100 state:active){items { id name updated_at column_values {id text type value}}}}");

			System.out.print("Unique Col queryJason :: "+queryJson);
			response = invoker().post("https://api.monday.com/v2", null, queryJson, null);

			if (response != null) {
				JSONObject json = new JSONObject(response);
				JSONArray itemDataArrays = json.getJSONObject("data").getJSONArray("boards").getJSONObject(0).getJSONArray("items");

				for (int i = 0; i < itemDataArrays.length(); i++) {
					JSONObject itemObj =itemDataArrays.getJSONObject(i);
					JSONArray columnArray =itemObj.getJSONArray("column_values");
					for (int j=0 ; j<columnArray.length();j++) {
						if ((columnArray.optJSONObject(j).getString("id")).equalsIgnoreCase(uniquecolid)){
							String emailObj = columnArray.optJSONObject(j).getString("text");
							if (mailId.toString().contains(emailObj) && !isNull(emailObj)) {
								addItemRecords(recordSet,itemObj);
							}
						}
					}
				}
			}
			return recordSet;
		} catch (OsyncException e) {
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			CommonUtil.logOsyncInfo(short_name,e.getMessage());
		} catch (NumberFormatException e) {
			e.printStackTrace();
			CommonUtil.logOsyncInfo(short_name,e.getMessage());
		} catch (Exception e) {
			CommonUtil.logOsyncInfo(short_name,"Exception updating IDS" + e);
			e.printStackTrace();
		}
		return null;
	}

	
	@Override
	public User getCurrentUser() {
		try {
			String response = "";

			JSONObject queryJson =  new JSONObject();
			queryJson.put("query", "{ me { id name email}}");
			System.out.print("User queryJason :: "+queryJson);
			response = invoker().post("https://api.monday.com/v2", null, queryJson, null);

			JSONObject json = new JSONObject(response);
			JSONObject me = json.getJSONObject("data").getJSONObject("me");

			String fullName = me.optString("name");
			String uniquId = me.optString("id");
			String email = me.optString("email");
			User user = new User(email);
			user.setUniqueUserId(uniquId);
			user.setFullName(fullName);
			user.setEmail(email);
			user.setFullJson(me);
			return user;
		} catch (Exception e) {
			log.log(Level.SEVERE, null, e);
		}
		return null;
	}
	
	@Override
	public List<ModuleInfoEntity> getModules(String osyncId, String serviceId) throws Exception {
		String response = "";
		JSONObject queryJson = new JSONObject();
		queryJson.put("query", "{ boards(newest_first: true, state:active) { id name } }");
		response = invoker().post("https://api.monday.com/v2", null, queryJson, null);
		JSONObject json = new JSONObject(response);
		JSONArray boards = json.getJSONObject("data").getJSONArray("boards");
		List<ModuleInfoEntity> modules = new ArrayList<ModuleInfoEntity>();
		if (boards != null) {
			for (int i = 0; i < boards.length(); i++) {
				JSONObject board = boards.getJSONObject(i);
				ModuleInfoEntity mie = new ModuleInfoEntity();
				mie.setModuleId(board.getString("id"));
				mie.setName(board.getString("name"));
				modules.add(mie);
			}
		}
		return modules;
	}
}
	
