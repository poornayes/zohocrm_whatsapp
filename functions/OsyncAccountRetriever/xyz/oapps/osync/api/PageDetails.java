package xyz.oapps.osync.api;


public class PageDetails {
	private Boolean authorization_page;
	private Boolean module_page;
	private Boolean field_page;
	private Boolean configuration_page;
	private int sync_status;
	
	public Boolean getAuthorization_page() {
		return authorization_page;
	}
	public void setAuthorization_page(Boolean authorization_page) {
		this.authorization_page = authorization_page;
	}
	
	public Boolean getModule_page() {
		return module_page;
	}
	public int getsync_status() {
		return sync_status;
	}
	public void setModule_page(Boolean module_page) {
		this.module_page = module_page;
	}
	
	public Boolean getField_page() {
		return field_page;
	}
	
	public void setField_page(Boolean field_page) {
		this.field_page = field_page;
	}
	
	public Boolean getConfiguration_page() {
		return configuration_page;
	}
	
	public void setConfiguration_page(Boolean configuration_page) {
		this.configuration_page = configuration_page;
	}
	
	public void setsync_status(int sync_status) {
		this.sync_status = sync_status;
	}
	

}
