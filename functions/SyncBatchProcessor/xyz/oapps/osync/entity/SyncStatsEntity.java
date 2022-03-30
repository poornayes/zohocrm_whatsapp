package xyz.oapps.osync.entity;

import xyz.oapps.osync.annotation.Table;

@Table(name = "SyncStats")
public class SyncStatsEntity extends OsyncEntity {
	
	private String osyncId = null;
	private String integId = null;
	
	private Integer leftCountCreated = 0;
	private Integer leftCountUpdated = 0;

	private Integer rightCountCreated = 0;
	private Integer rightCountUpdated = 0;
	
	private Integer leftErrorsCount = 0;
	private Integer rightErrorsCount = 0;

	private Integer duplicatesCount = 0;
	private Integer conflictCount = 0;

	private Integer dataInSync = 0;
	
	public Integer getLeftCountUpdated() {
		return leftCountUpdated;
	}

	public void setLeftCountUpdated(Integer leftCountUpdated) {
		this.leftCountUpdated = leftCountUpdated;
	}

	public Integer getRightCountUpdated() {
		return rightCountUpdated;
	}

	public void setRightCountUpdated(Integer rightCountUpdated) {
		this.rightCountUpdated = rightCountUpdated;
	}

	public Integer getLeftCountCreated() {
		return leftCountCreated;
	}

	public void setLeftCountCreated(Integer leftCountCreated) {
		this.leftCountCreated = leftCountCreated;
	}

	public Integer getRightCountCreated() {
		return rightCountCreated;
	}

	public void setRightCountCreated(Integer rightCountCreated) {
		this.rightCountCreated = rightCountCreated;
	}

	
	public Integer getLeftErrorsCount() {
		return leftErrorsCount;
	}

	public void setLeftErrorsCount(Integer leftErrorsCount) {
		this.leftErrorsCount = leftErrorsCount;
	}

	public Integer getRightErrorsCount() {
		return rightErrorsCount;
	}

	public void setRightErrorsCount(Integer rightErrorsCount) {
		this.rightErrorsCount = rightErrorsCount;
	}

	public Integer getDuplicatesCount() {
		return duplicatesCount;
	}

	public void setDuplicatesCount(Integer duplicatesCount) {
		this.duplicatesCount = duplicatesCount;
	}

	public Integer getConflictCount() {
		return conflictCount;
	}

	public void setConflictCount(Integer conflictCount) {
		this.conflictCount = conflictCount;
	}

	public String getOsyncId() {
		return osyncId;
	}

	public void setOsyncId(String osyncId) {
		this.osyncId = osyncId;
	}

	public String getIntegId() {
		return integId;
	}

	public void setIntegId(String integId) {
		this.integId = integId;
	}

	public Integer getDataInSync() {
		if(dataInSync == null) {
			dataInSync = 0;
		}
		return dataInSync;
	}

	public void setDataInSync(Integer dataInSync) {
		this.dataInSync = dataInSync;
	}

}
