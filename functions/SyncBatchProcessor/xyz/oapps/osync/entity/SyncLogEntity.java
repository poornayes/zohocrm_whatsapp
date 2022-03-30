package xyz.oapps.osync.entity;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import xyz.oapps.osync.annotation.Table;
@Table(name = "SyncLog")
public class SyncLogEntity extends OsyncEntity {
	private String osyncId;
	private String integId;
	private String leftService;
	private String rightService;
	private String leftModule;
	private String rightModule;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss" )
	private Date startTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss" )
	private Date endTime;
	
	private String status;
	private Integer timeTaken = 0;
	private Integer leftCountFetched = 0;
	private Integer leftCountUpdated = 0;
	private Integer leftNoChangeCount = 0;
	private Integer rightNoChangeCount = 0;
	private Integer rightCountFetched = 0;
	private Integer matchedOnUniqueColumn = 0;
	private Integer leftSkippedForEmailColumn = 0;
	private Integer rightSkippedForEmailColumn = 0;
	private Integer rightCountUpdated = 0;
	private Integer leftCountCreated = 0;
	private Integer rightCountCreated = 0;
	
	private Integer leftErrorsCount = 0;
	private Integer rightErrorsCount = 0;

	private Integer duplicatesCount = 0;
	private Integer conflictCount = 0;
	
	private Integer newDataInSync = 0;
	
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
	public String getLeftService() {
		return leftService;
	}
	public void setLeftService(String leftService) {
		this.leftService = leftService;
	}
	public String getRightService() {
		return rightService;
	}
	public void setRightService(String rightService) {
		this.rightService = rightService;
	}
	public String getLeftModule() {
		return leftModule;
	}
	public void setLeftModule(String leftModule) {
		this.leftModule = leftModule;
	}
	public String getRightModule() {
		return rightModule;
	}
	public void setRightModule(String rightModule) {
		this.rightModule = rightModule;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public Integer getTimeTaken() {
		return timeTaken;
	}
	public void setTimeTaken(Integer timeTaken) {
		this.timeTaken = timeTaken;
	}
	public Integer getLeftCountFetched() {
		return leftCountFetched;
	}
	public void setLeftCountFetched(Integer leftCountFetched) {
		this.leftCountFetched = leftCountFetched;
	}
	public Integer getLeftCountUpdated() {
		return leftCountUpdated;
	}
	public void setLeftCountUpdated(Integer leftCountUpdated) {
		this.leftCountUpdated = leftCountUpdated;
	}
	public Integer getLeftNoChangeCount() {
		return leftNoChangeCount;
	}
	public void setLeftNoChangeCount(Integer leftNoChangeCount) {
		this.leftNoChangeCount = leftNoChangeCount;
	}
	public Integer getRightNoChangeCount() {
		return rightNoChangeCount;
	}
	public void setRightNoChangeCount(Integer rightNoChangeCount) {
		this.rightNoChangeCount = rightNoChangeCount;
	}
	public Integer getRightCountFetched() {
		return rightCountFetched;
	}
	public void setRightCountFetched(Integer rightCountFetched) {
		this.rightCountFetched = rightCountFetched;
	}
	public Integer getMatchedOnUniqueColumn() {
		return matchedOnUniqueColumn;
	}
	public void setMatchedOnUniqueColumn(Integer matchedOnUniqueColumn) {
		this.matchedOnUniqueColumn = matchedOnUniqueColumn;
	}
	public Integer getLeftSkippedForEmailColumn() {
		return leftSkippedForEmailColumn;
	}
	public void setLeftSkippedForEmailColumn(Integer leftSkippedForEmailColumn) {
		this.leftSkippedForEmailColumn = leftSkippedForEmailColumn;
	}
	public Integer getRightSkippedForEmailColumn() {
		return rightSkippedForEmailColumn;
	}
	public void setRightSkippedForEmailColumn(Integer rightSkippedForEmailColumn) {
		this.rightSkippedForEmailColumn = rightSkippedForEmailColumn;
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
	public Integer getDuplicatesCount() {
		return duplicatesCount;
	}
	public void setDuplicatesCount(Integer duplicatesCount) {
		this.duplicatesCount = duplicatesCount;
	}
	public void addFetchCount(int count, boolean isLeft) {
		if (isLeft) {
			setLeftCountFetched(getLeftCountFetched() + count);
		} else {
			setRightCountFetched(getRightCountFetched() + count);
		}
	}
	public void incrementSkippedCount(boolean isLeft) {
		if (isLeft) {
			setLeftSkippedForEmailColumn(getLeftSkippedForEmailColumn() + 1);
		} else {
			setRightSkippedForEmailColumn(getRightSkippedForEmailColumn() + 1);
		}
	}
	
	public void incrementConflictCount() {
		setConflictCount(getConflictCount() + 1);
	}
	public void incrementUniqueColumnMatch() {
		setMatchedOnUniqueColumn(getMatchedOnUniqueColumn() + 1);
	}
	public void addCreatedCount(int count, boolean isLeft) {
		if (isLeft) {
			setLeftCountCreated(getLeftCountCreated() + count);
		} else {
			setRightCountCreated(getRightCountCreated() + count);
		}
	}
	public void addUpdatedCount(int count, boolean isLeft) {
		if (isLeft) {
			setLeftCountUpdated(getLeftCountUpdated() + count);
		} else {
			setRightCountUpdated(getRightCountUpdated() + count);
		}
	}
	public void incrNoChangeCount(boolean isLeft) {
		if (isLeft) {
			setLeftNoChangeCount(getLeftNoChangeCount() + 1);
		} else {
			setRightNoChangeCount(getRightNoChangeCount() + 1);
		}
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getConflictCount() {
		return conflictCount;
	}
	public void setConflictCount(Integer conflictCount) {
		this.conflictCount = conflictCount;
	}

	public Integer getLeftErrorsCount() {
		return leftErrorsCount;
	}
	public void setLeftErrorsCount(Integer leftErrorsCount) {
		this.leftErrorsCount = leftErrorsCount;
	}
	
	public void addErrorsCount(int count, boolean isLeft) {
		if (isLeft) {
			setLeftErrorsCount(getLeftErrorsCount() + count);
		} else {
			setRightErrorsCount(getRightErrorsCount() + count);
		}
	}
	
	public Integer getRightErrorsCount() {
		return rightErrorsCount;
	}
	public void setRightErrorsCount(Integer rightErrorsCount) {
		this.rightErrorsCount = rightErrorsCount;
	}
	public Integer getNewDataInSync() {
		if(newDataInSync == null) {
			newDataInSync = 0;
		}
		return newDataInSync;
	}
	public void setNewDataInSync(Integer newDataInSync) {
		this.newDataInSync = newDataInSync;
	}
	public void incrementNewDataInSync() {
		setNewDataInSync(getNewDataInSync() + 1);
	}
}

