package xyz.oapps.osync.entity;

import static xyz.oapps.osync.api.RequestController.mapper;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

public class OsyncEntity {

	@JsonProperty("CREATEDTIME")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createdTime;

	@JsonProperty("MODIFIEDTIME")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date modifiedTime;

	@JsonProperty("ROWID")
	private Long rowId;

	@JsonProperty("CREATORID")
	private Long creatorId;

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public Date getModifiedTime() {
		return modifiedTime;
	}

	public void setModifiedTime(Date modifiedTime) {
		this.modifiedTime = modifiedTime;
	}

	public Long getRowId() {
		return rowId;
	}

	public void setRowId(Long rowId) {
		this.rowId = rowId;
	}

	public Long getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(Long creatorId) {
		this.creatorId = creatorId;
	}

	@Override
	public String toString() {
		try {
			return mapper().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return super.toString();
	}
	
}
