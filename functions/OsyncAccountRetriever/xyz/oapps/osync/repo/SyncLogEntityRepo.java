package xyz.oapps.osync.repo;

import static org.jooq.impl.DSL.field;
import static xyz.oapps.osync.api.RequestController.query;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.SelectQuery;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;

import xyz.oapps.osync.CurrentContext;
import xyz.oapps.osync.db.OsyncDB;
import xyz.oapps.osync.entity.SyncLogEntity;
import xyz.oapps.osync.entity.SyncStatsEntity;
import static org.jooq.impl.DSL.table;
public class SyncLogEntityRepo {

	private static Logger log = Logger.getLogger(SyncLogEntityRepo.class.getName());

	public void save(SyncLogEntity logEntity) {
		try {
			log.log(Level.INFO, "coming to saveSycReportdata");
			OsyncDB osyncDB = OsyncDB.get(CurrentContext.getCurrentOsyncId());
			
			SelectQuery<?> query = query().select(field("*")).from("SyncStats")
					.where(field("integ_id").eq(logEntity.getIntegId()))
					.getQuery();
			SyncStatsEntity stats = osyncDB.findOne(SyncStatsEntity.class, query);
			
			log.log(Level.INFO, "coming to saveSycReportdata :integId",logEntity.getIntegId());
			boolean create = false;
			
			if(stats == null) {
				stats = new SyncStatsEntity();
				stats.setIntegId(logEntity.getIntegId());
				stats.setOsyncId(logEntity.getOsyncId());
				create = true;
				log.log(Level.INFO, "Syn Status boolean if stats null",create);
			}
			log.log(Level.INFO, "SLER getLeftCountCreated",logEntity.getLeftCountCreated());
			log.log(Level.INFO, "SLER getLeftCountUpdated",logEntity.getLeftCountUpdated());
			log.log(Level.INFO, "SLER getRightCountCreated",logEntity.getRightCountCreated());
			log.log(Level.INFO, "SLER getRightCountUpdated",logEntity.getRightCountUpdated());
			
				stats.setConflictCount(stats.getConflictCount() + logEntity.getConflictCount());
			stats.setDuplicatesCount(stats.getDuplicatesCount() + logEntity.getDuplicatesCount());
			
			stats.setLeftErrorsCount(stats.getLeftErrorsCount() + logEntity.getLeftErrorsCount()
					+ logEntity.getRightSkippedForEmailColumn());
			stats.setRightErrorsCount(stats.getRightErrorsCount() + logEntity.getRightErrorsCount()
					+ +logEntity.getLeftSkippedForEmailColumn());
			
			stats.setLeftCountCreated(stats.getLeftCountCreated() + logEntity.getLeftCountCreated());
			stats.setLeftCountUpdated(stats.getLeftCountUpdated() + logEntity.getLeftCountUpdated());
			
			stats.setRightCountCreated(stats.getRightCountCreated() + logEntity.getRightCountCreated());
			stats.setRightCountUpdated(stats.getRightCountUpdated() + logEntity.getRightCountUpdated());
			stats.setDataInSync(stats.getDataInSync() + logEntity.getNewDataInSync());
			
			log.log(Level.INFO, "Coming to SyncLogEntityRepo save ",logEntity.getIntegId());
			osyncDB.insert(logEntity);
			log.log(Level.INFO, "coming to saveSycReportdata :create",create);
			if (create) {
				log.log(Level.INFO, "coming to saveSycReportdata :insert",stats);
				osyncDB.insert(stats);
			} else {
				log.log(Level.INFO, "coming to saveSycReportdata :update",stats);
				osyncDB.update(stats);
			}
			
			log.log(Level.INFO, "New syncLog  created,for {0} {1}",
					new Object[] { logEntity.getOsyncId(), logEntity.getIntegId() });
		} catch (Exception e) {
			e.getMessage();
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public SyncLogEntity findByIntegId(String integId) throws Exception {
		SelectQuery<?> query = query().select(field("*")).from("SyncLog")
				.where(field("integ_id").eq(integId)).orderBy(field("ROWID").desc())
				.limit(1).getQuery();
		log.log(Level.INFO, "Query   : ",query );
		return OsyncDB.get().findOne(SyncLogEntity.class, query);
	}

	public SyncStatsEntity findStatsByIntegId(String integId) throws Exception {
		SelectQuery<?> query = query().select(field("*")).from("SyncStats")
				.where(field("integ_id").eq(integId)).getQuery();
		log.log(Level.INFO, "Query   : ",query );
		return OsyncDB.get().findOne(SyncStatsEntity.class, query);
	}
	
	public List<SyncStatsEntity> findAllStats(String osyncId) throws Exception {
		SelectQuery<?> query = query().select(field("*")).from("SyncStats")
				.where(field("osync_id").eq(osyncId)).getQuery();
		log.log(Level.INFO, "Query   : ",query );
		return OsyncDB.get().executeQuery(SyncStatsEntity.class, query);
	}
	
//	public static void main(String[] args) {
//		DSLContext create = DSL.using(SQLDialect.SQLITE);
//		SelectQuery<?> query = query().select(field("*"))
//				.from(table("SyncLog")).join(table("OsyncAccount"))
//				.on(field("SyncLog.osync_id").eq(field("AccountInfoEntity.osync_id")))
//				.where(field("SyncLog.osync_id").eq("bala")).getQuery();
//		String sql = query.getSQL(ParamType.INLINED);
//		System.out.println(sql);
//	}
}
