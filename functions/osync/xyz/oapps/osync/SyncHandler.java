package xyz.oapps.osync;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import xyz.oapps.osync.entity.ModuleInfoEntity;
import xyz.oapps.osync.entity.ServiceInfoEntity;
import xyz.oapps.osync.fields.Fields;
import xyz.oapps.osync.fields.RecordSet;
import xyz.oapps.osync.invoker.Invoker;
import xyz.oapps.osync.repo.ModuleInfoRepository;
import xyz.oapps.osync.repo.ServiceInfoRepository;

public abstract class SyncHandler {

	private ServiceInfoEntity service;
	private ModuleInfoEntity module;
	protected String integId;
	protected String osyncId;
	private boolean isLeft;
	private String  generatedLogString;

	/**
	 * List of fields
	 * 
	 * @param accountId
	 * @return
	 */
	public abstract Fields getFields() throws OsyncException;

	/**
	 * Create the new records and return the unique values
	 * 
	 * @param recordsToCreate
	 * @param syncFrom
	 * @return
	 */
	public abstract HashMap<String, String> createNewRecords(RecordSet recordsToCreate, String syncFrom) throws OsyncException;

	/**
	 * Update record
	 * 
	 * @param recordsToUpdate
	 * @param syncFrom
	 */
	public abstract void updateRecords(RecordSet recordsToUpdate, String syncFrom) throws OsyncException;

	/**
	 * Fetch the records from services.
	 * 
	 * @param startPage    - Number of the page
	 * @param totalRecords - Total records per page
	 * @param startTime    - Start time of the changes. If -1, then its a first time
	 *                     sync
	 * @return
	 */
	public abstract RecordSet fetchRecords(int startPage, int totalRecords, Long startTime)  throws OsyncException;

	/**
	 * Matching records based on the Unique Identifier. It can be a ID
	 * 
	 * @param recordsToFetchRemote
	 * @return
	 */
	public abstract RecordSet getMatchedRecordsById(List<String> recordsToFetchRemote)  throws OsyncException;

	public abstract User getCurrentUser()  throws OsyncException;

	/**
	 * List of values of unique columns. Return the record set
	 * 
	 * @param values
	 * @return
	 */
	public abstract RecordSet getMatchedRecordsByUniqueColumn(Collection<String> values) throws OsyncException;
	
	
	//public abstract Integer getTotalRecordsCountToSync() throws OsyncException;

	public Invoker invoker() throws Exception {
		return new Invoker(getServiceName(), true, isLeft, false, getOsyncId());
	}
	
	private String getOsyncId() {
		// TODO Auto-generated method stub
		return this.osyncId;
	}

	public List<ModuleInfoEntity> getModules(String osyncId, String serviceId) throws Exception {
		ModuleInfoRepository moduleRepo = new ModuleInfoRepository();
		return moduleRepo.findAllByServiceId(serviceId);
	}

	public String getModuleName() {
		return getModule().getName();
	}

	public String getIntegId() {
		return integId;
	}

	public String getServiceName() {
		return getService().getName();
	}

	public void setIntegId(String integId) {
		this.integId = integId;
	}

	public static boolean isNull(Object value) {
		return value == null || value.toString().trim().isEmpty() || value.toString().equalsIgnoreCase("null");
	}

	public Object toMap(JSONObject jsonObject) throws JSONException {
		@SuppressWarnings("unchecked")
		Iterator<String> keys = jsonObject.keys();
		Map<String, Object> values = new HashMap<String, Object>();
		while (keys.hasNext()) {
			String key = keys.next();
			Object value = jsonObject.get(key);
			values.put(key, value);
		}
		return values;
	}

	public Object toList(JSONArray jsonArray) throws JSONException {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < jsonArray.length(); i++) {
			list.add(jsonArray.get(i).toString());
		}
		return list;
	}

	public static String toISO8601Format(Long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(new Date(time));
	}

	public boolean isLeft() {
		return isLeft;
	}

	public void setLeft(boolean isLeft) {
		this.isLeft = isLeft;
	}

	protected Long getModifiedTimeForOutReach(JSONObject accountObj) throws ParseException {
		String date = accountObj.optString("updatedAt");
		// 2020-07-31T07:40:40.000Z
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		Date date2 = sdf.parse(date);
		return date2.getTime();
	}

	public ServiceInfoEntity getService() {
		return service;
	}

	public void setService(ServiceInfoEntity service) {
		this.service = service;
	}

	public ModuleInfoEntity getModule() {
		return module;
	}

	public void setModule(ModuleInfoEntity module) {
		this.module = module;
	}

	public String getGeneratedLogString() {
		return generatedLogString;
	}

	public void setGeneratedLogString(String generatedLogString) {
		this.generatedLogString = generatedLogString;
	}
	
	
	

}
