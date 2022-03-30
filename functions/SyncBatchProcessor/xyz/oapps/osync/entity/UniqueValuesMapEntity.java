package xyz.oapps.osync.entity;

import xyz.oapps.osync.annotation.Table;

@Table(name = "UniqueValuesMap")
public class UniqueValuesMapEntity extends OsyncEntity {

	private String osyncId;

	private String integId;

	private String leftUniqueValue;

	private String rightUniqueValue;

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

	public String getLeftUniqueValue() {
		return leftUniqueValue;
	}

	public void setLeftUniqueValue(String leftUniqueValue) {
		this.leftUniqueValue = leftUniqueValue;
	}

	public String getRightUniqueValue() {
		return rightUniqueValue;
	}

	public void setRightUniqueValue(String rightUniqueValue) {
		this.rightUniqueValue = rightUniqueValue;
	}
}
