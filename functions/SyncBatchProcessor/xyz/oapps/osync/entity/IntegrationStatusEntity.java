package xyz.oapps.osync.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import xyz.oapps.osync.annotation.Table;

@Table(name = "IntegrationStatus")
public class IntegrationStatusEntity extends OsyncEntity {
	private String integId;

	private String osyncId;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss" )
	private Date prevStartTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss" )
	private Date prevEndTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss" )
	private Date startTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss" )
	private Date endTime;
	
	private String status;
	
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

	public Date getPrevStartTime() {
		return prevStartTime;
	}

	public void setPrevStartTime(Date prevStartTime) {
		this.prevStartTime = prevStartTime;
	}

	public Date getPrevEndTime() {
		return prevEndTime;
	}

	public void setPrevEndTime(Date prevEndTime) {
		this.prevEndTime = prevEndTime;
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
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	
}

