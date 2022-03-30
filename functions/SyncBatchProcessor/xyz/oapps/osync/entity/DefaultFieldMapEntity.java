package xyz.oapps.osync.entity;

import xyz.oapps.osync.annotation.Table;

@Table(name = "DefaultFieldMap", id="default_fieldmap_id")
public class DefaultFieldMapEntity extends OsyncEntity {
	private String defaultFieldmapId;

	private String leftServiceId;

	private String rightServiceId;

	private String leftModuleId;

	private String rightModuleId;

	private String leftColumnName;

	private String rightColumnName;

	private String leftColumnType;

	private String rightColumnType;

	public String getDefaultFieldmapId() {
		return defaultFieldmapId;
	}

	public void setDefaultFieldmapId(String defaultFieldmapId) {
		this.defaultFieldmapId = defaultFieldmapId;
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

	public String getLeftColumnName() {
		return leftColumnName;
	}

	public void setLeftColumnName(String leftColumnName) {
		this.leftColumnName = leftColumnName;
	}

	public String getRightColumnName() {
		return rightColumnName;
	}

	public void setRightColumnName(String rightColumnName) {
		this.rightColumnName = rightColumnName;
	}

	public String getLeftColumnType() {
		return leftColumnType;
	}

	public void setLeftColumnType(String leftColumnType) {
		this.leftColumnType = leftColumnType;
	}

	public String getRightColumnType() {
		return rightColumnType;
	}

	public void setRightColumnType(String rightColumnType) {
		this.rightColumnType = rightColumnType;
	}
	
	

}
