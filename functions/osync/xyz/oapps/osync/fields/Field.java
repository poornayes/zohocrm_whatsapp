package xyz.oapps.osync.fields;

public class Field {
	public Field(String dataType, String id, String displayName, boolean mandatory) {
		super();
		this.dataType = dataType;
		this.id = id;
		this.displayName = displayName;
		this.mandatory = mandatory;
	}

	String dataType;

	String id;

	String displayName;
	
	private String format;

	private boolean mandatory;
	
	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

}
