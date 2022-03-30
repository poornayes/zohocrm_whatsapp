package xyz.oapps.osync.entity;

import xyz.oapps.osync.annotation.Table;

@Table(name = "FieldMap", id="fieldmap_id")
public class FieldMapEntity extends OsyncEntity {
	private String fieldmapId;

	private String osyncId;

	private String integId;

	private String leftColumnName;

	private String rightColumnName;

	private String leftColumnType;

	private String rightColumnType;
	
	private String leftColumnFormat;
	
	private String rightColumnFormat;
	
	private boolean oneWay = false;
	
	private boolean enabled = true;
	
	private boolean leftMandatory = false;
	
	private boolean rightMandatory = false;

	public String getFieldmapId() {
		return fieldmapId;
	}

	public void setFieldmapId(String fieldmapId) {
		this.fieldmapId = fieldmapId;
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

	public String getLeftColumnFormat() {
		return leftColumnFormat;
	}

	public void setLeftColumnFormat(String leftColumnFormat) {
		this.leftColumnFormat = leftColumnFormat;
	}

	public String getRightColumnFormat() {
		return rightColumnFormat;
	}

	public void setRightColumnFormat(String rightColumnFormat) {
		this.rightColumnFormat = rightColumnFormat;
	}

	public boolean isOneWay() {
		return oneWay;
	}

	public void setOneWay(boolean oneWay) {
		this.oneWay = oneWay;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isLeftMandatory() {
		return leftMandatory;
	}

	public void setLeftMandatory(boolean leftMandatory) {
		this.leftMandatory = leftMandatory;
	}

	public boolean isRightMandatory() {
		return rightMandatory;
	}

	public void setRightMandatory(boolean rightMandatory) {
		this.rightMandatory = rightMandatory;
	}

}
