package xyz.oapps.osync.entity;

import xyz.oapps.osync.annotation.Table;

@Table(name = "CronDetails")
public class CronDetailsEntity extends OsyncEntity {

	private String osyncId;

	private Long cronJobId;

	public String getOsyncId() {
		return osyncId;
	}

	public void setOsyncId(String osyncId) {
		this.osyncId = osyncId;
	}

	public Long getCronJobId() {
		return cronJobId;
	}

	public void setCronJobId(Long cronJobId) {
		this.cronJobId = cronJobId;
	}

}