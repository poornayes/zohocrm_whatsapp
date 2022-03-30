package xyz.oapps.osync.fields;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import xyz.oapps.osync.entity.UniqueValuesMapEntity;
import xyz.oapps.osync.repo.UniqueValuesMapRepo;

public class RecordSet implements Iterable<Record> {

	String controllerName;

	String uniqueId;

	String uniqueColumnName;
	
	private String source;
	
	private String destination;
	
	private String sourceModule;
	
	private String destinationModule;

	HashMap<String, Record> recordSets = new HashMap<String, Record>();

	private RecordSet(String controllerName, String uniqueId) {
		this.controllerName = controllerName;
		this.uniqueId = uniqueId;
	}

	public static RecordSet init(String controllerName, String uniqueId) {
		return new RecordSet(controllerName, uniqueId);
	}

	public Record add(String uniqueValue, Long modifiedTime) {
		Record record = new Record(uniqueValue);
		record.setModifiedTime(modifiedTime);
		recordSets.put(uniqueValue, record);
		return record;
	}

	public void remove(String uniqueValue) {
		recordSets.remove(uniqueValue);
	}

	public Record createEmptyObject() {
		String uniqueValue = "generated-" + (this.count() + 1) + "-" + System.currentTimeMillis();
		Record record = new Record(uniqueValue);
		record.setNewRecord(true);
		recordSets.put(uniqueValue, record);
		return record;
	}

	public Record find(String uniqueValue) {
		return recordSets.get(uniqueValue);
	}

	public Record findByUniqueColumn(String uniqueValue) {
		// TODO : Optimize
		for (Record record : recordSets.values()) {
			Object valueObj = record.getValue(uniqueColumnName);
			if (valueObj != null) {
				if (valueObj.toString().equalsIgnoreCase(uniqueValue)) {
					return record;
				}
			}
		}
		return null;
	}

	public int count() {
		return recordSets.size();
	}

	@Override
	public Iterator<Record> iterator() {
		return getAllRecords().iterator();
	}

	public Collection<Record> getAllRecords() {
		return recordSets.values();
	}

	public void fillRightUniqueValueMap(UniqueValuesMapRepo uvMapRepo, String integId) throws Exception {
		List<String> uniqueIns = new ArrayList<String>();
		for (Record record : getAllRecords()) {
			uniqueIns.add(record.getUniqueValue());
		}
		HashMap<String, String> uniqueValues = new HashMap<String, String>();
		List<UniqueValuesMapEntity> uniqueValuesMap = uvMapRepo.findByIntegIdAndRightUniqueValueIn(integId, uniqueIns);
		if (uniqueValuesMap != null) {
			for (UniqueValuesMapEntity uniqueValuesMap2 : uniqueValuesMap) {
				uniqueValues.put(uniqueValuesMap2.getRightUniqueValue(), uniqueValuesMap2.getLeftUniqueValue());
			}
			for (Record record : getAllRecords()) {
				record.setMappedRecordUniqueValue(uniqueValues.get(record.getUniqueValue()));
			}
		}
	}

	public void fillLeftUniqueValueMap(UniqueValuesMapRepo uvMapRepo, String integId) throws Exception {
		List<String> uniqueIns = new ArrayList<String>();
		for (Record record : getAllRecords()) {
			uniqueIns.add(record.getUniqueValue());
		}
		HashMap<String, String> uniqueValues = new HashMap<String, String>();
		List<UniqueValuesMapEntity> uniqueValuesMap = uvMapRepo.findByIntegIdAndLeftUniqueValueIn(integId, uniqueIns);
		if (uniqueValuesMap != null) {
			for (UniqueValuesMapEntity uniqueValuesMap2 : uniqueValuesMap) {
				uniqueValues.put(uniqueValuesMap2.getLeftUniqueValue(), uniqueValuesMap2.getRightUniqueValue());
			}

			for (Record record : getAllRecords()) {
				record.setMappedRecordUniqueValue(uniqueValues.get(record.getUniqueValue()));
			}
		}
	}

	public void fillUniqueValueMap(UniqueValuesMapRepo uvMapRepo, String integId, boolean isLeft) throws Exception {
		if (isLeft) {
			fillLeftUniqueValueMap(uvMapRepo, integId);
		} else {
			fillRightUniqueValueMap(uvMapRepo, integId);
		}
	}

	public void setUniqueColumnName(String uniqueColumn) {
		this.uniqueColumnName = uniqueColumn;

	}
	
	public List<String> getUniqueIds() {
		List<String> list = new ArrayList<String>();
		for (String key : recordSets.keySet()) {
			list.add(key);
		};
		return list;
	}

	public String getSource() {
		return source;
	}

	public RecordSet setSource(String source) {
		this.source = source;
		return this;
	}

	public String getDestination() {
		return destination;
	}

	public RecordSet setDestination(String destination) {
		this.destination = destination;
		return this;
	}

	public String getSourceModule() {
		return sourceModule;
	}

	public RecordSet setSourceModule(String sourceModule) {
		this.sourceModule = sourceModule;
		return this;
	}

	public String getDestinationModule() {
		return destinationModule;
	}

	public RecordSet setDestinationModule(String destinationModule) {
		this.destinationModule = destinationModule;
		return this;
	}
}
