package xyz.oapps.osync.fields;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Record {

	private String uniqueValue;

	private String mappedRecordUniqueValue;
	
	private String mappedRecordUrl;

	private boolean isNewRecord;
	
	private Long modifiedTime;
	
	private boolean isDuplicate = false;

	private HashMap<String, Object> columnValues = new HashMap<String, Object>();

	protected Record(String uniqueValue) {
		super();
		this.setUniqueValue(uniqueValue);
	}

	public Record addOrUpdateValue(String key, Object value) {
		getColumnValues().put(key, value);
		return this;
	}

	public Object getValue(String key) {
		return getColumnValues().get(key);
	}

	public JSONObject columnValuesAsJson() throws JsonProcessingException, JSONException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		return new JSONObject(mapper.writeValueAsString(getColumnValues()));
	}

	public JSONObject toJson() throws JsonProcessingException, JSONException {
		ObjectMapper mapper = new ObjectMapper();
		// mapper.setSerializationInclusion(Include.NON_NULL);
		return new JSONObject(mapper.writeValueAsString(this));
	}

	public boolean isNewRecord() {
		return isNewRecord;
	}

	public void setNewRecord(boolean isNewRecord) {
		this.isNewRecord = isNewRecord;
	}

	public String getUniqueValue() {
		return uniqueValue;
	}

	public void setUniqueValue(String uniqueValue) {
		this.uniqueValue = uniqueValue;
	}

	public String getMappedRecordUniqueValue() {
		return mappedRecordUniqueValue;
	}

	public void setMappedRecordUniqueValue(String mappedRecordUniqueValue) {
		this.mappedRecordUniqueValue = mappedRecordUniqueValue;
	}

	public Map<String, Object> getColumnValues() {
		return columnValues;
	}

	public void setColumnValues(HashMap<String, Object> columnValues) {
		this.columnValues = columnValues;
	}

	public Long getModifiedTime() {
		return modifiedTime;
	}

	public void setModifiedTime(Long modifiedTime) {
		this.modifiedTime = modifiedTime;
	}

	public boolean isDuplicate() {
		return isDuplicate;
	}

	public void setDuplicate(boolean isDuplicate) {
		this.isDuplicate = isDuplicate;
	}

	public String getMappedRecordUrl() {
		return mappedRecordUrl;
	}

	public void setMappedRecordUrl(String mappedRecordUrl) {
		this.mappedRecordUrl = mappedRecordUrl;
	}

}
