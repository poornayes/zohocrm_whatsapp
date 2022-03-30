package xyz.oapps.osync.entity;

import xyz.oapps.osync.annotation.Table;

@Table(name = "IntegrationProps", id="integ_id")
public class IntegrationPropsEntity extends OsyncEntity {
	private String integId;

	private String osyncId;

	private String leftServiceId;

	private String rightServiceId;

	private String leftModuleId;

	private String rightModuleId;

	private boolean syncRecordsWithEmail = false;

	private String masterService;

	private Integer syncDuration;

	private boolean lookupUniqueColumn = false;

	private int syncStatus = 0;
	
	/**
	 * 1 - Left - Right
	 * 2 - Right - Left
	 * 3 - Both
	 */
	private int direction = 1;

	
	public String getIntegId() {
		return integId;
	}

	public void setIntegId(String integId) {
		this.integId = integId;
	}

	public String getOsyncId() {
		return osyncId;
	}

	public void setOsyncId(String osyncId) {
		this.osyncId = osyncId;
	}

	public String getLeftServiceId() {
		return leftServiceId;
	}

	public void setLeftServiceId(String leftServiceId) {
		this.leftServiceId = leftServiceId;
	}

	public String getRightServiceId() {
		return rightServiceId;
	}

	public void setRightServiceId(String rightServiceId) {
		this.rightServiceId = rightServiceId;
	}

	public String getLeftModuleId() {
		return leftModuleId;
	}

	public void setLeftModuleId(String leftModuleId) {
		this.leftModuleId = leftModuleId;
	}

	public String getRightModuleId() {
		return rightModuleId;
	}

	public void setRightModuleId(String rightModuleId) {
		this.rightModuleId = rightModuleId;
	}

	public boolean isSyncRecordsWithEmail() {
		return syncRecordsWithEmail;
	}

	public void setSyncRecordsWithEmail(boolean syncRecordsWithEmail) {
		this.syncRecordsWithEmail = syncRecordsWithEmail;
	}

	public String getMasterService() {
		return masterService;
	}

	public void setMasterService(String masterService) {
		this.masterService = masterService;
	}

	public Integer getSyncDuration() {
		return syncDuration;
	}

	public void setSyncDuration(Integer syncDuration) {
		this.syncDuration = syncDuration;
	}

	public boolean isLookupUniqueColumn() {
		return lookupUniqueColumn;
	}

	public void setLookupUniqueColumn(boolean lookupUniqueColumn) {
		this.lookupUniqueColumn = lookupUniqueColumn;
	}


	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public int getSyncStatus() {
		return syncStatus;
	}

	public void setSyncStatus(int syncStatus) {
		this.syncStatus = syncStatus;
	}

}
