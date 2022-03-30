package xyz.oapps.osync.api;



import xyz.oapps.osync.entity.IntegrationPropsEntity;
import xyz.oapps.osync.entity.IntegrationStatusEntity;
import xyz.oapps.osync.entity.SyncStatsEntity;

public class SyncStats {
	private IntegrationPropsEntity integProps;
	private IntegrationStatusEntity integStatus;
	private SyncStatsEntity syncstats;
	
	public IntegrationPropsEntity getIntegProps() {
		return integProps;
	}
	public void setIntegProps(IntegrationPropsEntity integProps) {
		this.integProps = integProps;
	}
	public IntegrationStatusEntity getIntegStatus() {
		return integStatus;
	}
	public void setIntegStatus(IntegrationStatusEntity integStatus) {
		this.integStatus = integStatus;
	}
	public SyncStatsEntity getSyncstats() {
		return syncstats;
	}
	public void setSyncstats(SyncStatsEntity syncstats) {
		this.syncstats = syncstats;
	}
	
	
}
