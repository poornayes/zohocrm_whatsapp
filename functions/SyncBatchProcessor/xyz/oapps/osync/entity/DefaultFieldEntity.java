package xyz.oapps.osync.entity;

import xyz.oapps.osync.annotation.Table;

@Table(name = "DefaultField", id="default_field_id")
public class DefaultFieldEntity extends OsyncEntity {
	
	private String defaultFieldId;

	private String serviceId;

	private String moduleId;

	private String columnName;

	private String columnType;

	public String getDefaultFieldId() {
		return defaultFieldId;
	}

	public void setDefaultFieldId(String defaultFieldId) {
		this.defaultFieldId = defaultFieldId;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getColumnType() {
		return columnType;
	}

	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}

	
}
