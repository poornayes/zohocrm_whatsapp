package xyz.oapps.osync.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import xyz.oapps.osync.annotation.Table;

@Table(name = "DigestAuth")
public class DigestAuthEntity extends OsyncEntity {

	private String osyncId;
	
	private String digestValue;

	private String entityId;

	private String entityType;

	private int status;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date validUpto;

	public String getOsyncId() {
		return osyncId;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getDigestValue() {
		return digestValue;
	}

	public void setDigestValue(String digestValue) {
		this.digestValue = digestValue;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public Date getValidUpto() {
		return validUpto;
	}

	public void setValidUpto(Date validUpto) {
		this.validUpto = validUpto;
	}

	public void setOsyncId(String osyncId) {
		this.osyncId = osyncId;
	}
	
}
