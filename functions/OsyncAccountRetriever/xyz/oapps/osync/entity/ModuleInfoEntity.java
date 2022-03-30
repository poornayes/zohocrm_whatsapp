package xyz.oapps.osync.entity;

import xyz.oapps.osync.annotation.Table;

@Table(name = "Module", id="module_id")
public class ModuleInfoEntity extends OsyncEntity {

	private String moduleId;
	
	private String serviceId;
	
	private String name;

	private String primaryColumn;

	private String uniqueColumn;
	
	private String emailColumn;
	
	private int moduleOrder;
	
	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPrimaryColumn() {
		return primaryColumn;
	}

	public void setPrimaryColumn(String primaryColumn) {
		this.primaryColumn = primaryColumn;
	}

	public String getUniqueColumn() {
		return uniqueColumn;
	}

	public void setUniqueColumn(String uniqueColumn) {
		this.uniqueColumn = uniqueColumn;
	}

	public String getEmailColumn() {
		return emailColumn;
	}

	public void setEmailColumn(String emailColumn) {
		this.emailColumn = emailColumn;
	}

	public int getModuleOrder() {
		return moduleOrder;
	}

	public void setModuleOrder(int moduleOrder) {
		this.moduleOrder = moduleOrder;
	}
	
}
