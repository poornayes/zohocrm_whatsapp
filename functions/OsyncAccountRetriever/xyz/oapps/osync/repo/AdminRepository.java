package xyz.oapps.osync.repo;

import static org.jooq.impl.DSL.field;
import static xyz.oapps.osync.api.RequestController.query;

import java.util.List;
import java.util.logging.Logger;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;

import xyz.oapps.osync.CurrentContext;
import xyz.oapps.osync.api.RequestController;
import xyz.oapps.osync.db.OsyncDB;
import xyz.oapps.osync.entity.AccountInfoEntity;
import xyz.oapps.osync.entity.FieldMapEntity;
import xyz.oapps.osync.entity.IntegrationPropsEntity;
import xyz.oapps.osync.entity.IntegrationStatusEntity;
import xyz.oapps.osync.entity.ModuleInfoEntity;
import xyz.oapps.osync.entity.ServiceAuthInfoEntity;
import xyz.oapps.osync.entity.ServiceInfoEntity;
import xyz.oapps.osync.entity.SyncLogEntity;

public class AdminRepository {

	private static Logger log = Logger.getLogger(ServiceInfoEntity.class.getName());

	public DSLContext query() {
		return DSL.using(SQLDialect.SQLITE);
	}

	/***
	 * DISCLAIMER : IT IS DEDICATED ONLY FOR ADMIN ROLES : DON NOT USE FOR NORMA
	 * OPERATIONS
	 ***/

	public List<FieldMapEntity> getAllFieldsByIntegId(String integId) throws Exception {
		SelectQuery<?> query = query().select(field("*")).from("FieldMap").where(field("integ_id").eq(integId))
				.getQuery();
		return OsyncDB.get().executeSystemQuery(FieldMapEntity.class, query);
	}

	public List<AccountInfoEntity> findByType(String searchText) throws Exception {
		OsyncDB osp = OsyncDB.get(CurrentContext.getCurrentOsyncId());
		SelectQuery<Record> fetchServiceInfoById = query().selectFrom("OsyncAccount")
				.where(field("name").eq(searchText).or(field("email").eq(searchText))
						.or(field("osync_id").eq(searchText)).or(field("remote_identifier").eq(searchText)))
				.orderBy(field("ROWID").desc()).getQuery();
		return osp.executeSystemQuery(AccountInfoEntity.class, fetchServiceInfoById);
	}

	public List<IntegrationPropsEntity> findAllIntPropsForSync(Long rowId) throws Exception {
		OsyncDB osp = OsyncDB.get(null);

		SelectQuery<?> query = query().select(field("*")).from("IntegrationProps")
				.where(field("sync_status").eq(1).and(field("ROWID").greaterThan(rowId))).orderBy(field("ROWID").asc())
				.limit(2).getQuery();
		return osp.executeSystemQuery(IntegrationPropsEntity.class, query);
	}

	public List<IntegrationPropsEntity> getIntegrationByIntegId(String integId) throws Exception {

		OsyncDB osp = OsyncDB.get();
		SelectQuery<Record> fetchById = query().selectFrom("IntegrationProps")
				.where(field("integ_id").eq(integId).or(field("osync_id").eq(integId))

				).orderBy(field("ROWID").desc()).getQuery();
		return osp.executeSystemQuery(IntegrationPropsEntity.class, fetchById);
	}

	public List<ModuleInfoEntity> findAllModules() throws Exception {
		SelectQuery<Record> query2 = query().selectFrom("Module").getQuery();
		List<ModuleInfoEntity> moduleInfoEntites = OsyncDB.get().executeSystemQuery(ModuleInfoEntity.class, query2);
		return moduleInfoEntites;
	}

	public List<SyncLogEntity> findAllByIntegId(String integId) throws Exception {
		OsyncDB osp = OsyncDB.get();

		SelectQuery<?> query = query().select(field("*")).from("SyncLog").where(field("integ_id").eq(integId))
				.orderBy(field("ROWID").desc()).getQuery();
		return osp.executeSystemQuery(SyncLogEntity.class, query);
	}
	
public List<ServiceAuthInfoEntity> getReportsByServiceAuthInfo(String authinfoId) throws Exception {
		
		OsyncDB osp = OsyncDB.get();
		SelectQuery<Record> fetchById = query().selectFrom("ServiceAuthInfo")
				.where(
						field("integ_id").eq(authinfoId)
						.or(field("osync_id").eq(authinfoId))
						
						).orderBy(field("ROWID").desc())
				.getQuery();
		return osp.executeSystemQuery(ServiceAuthInfoEntity.class, fetchById);
	}

	public ServiceAuthInfoEntity findByAuthId(String authId) throws Exception {
		SelectQuery<Record> fetchServiceAuthId = query().selectFrom("ServiceAuthInfo")
				.where(field("auth_id").eq(authId)).getQuery();
		return OsyncDB.get().findOne(ServiceAuthInfoEntity.class, fetchServiceAuthId);
	}
	public ServiceAuthInfoEntity update(String authId, ServiceAuthInfoEntity serviceAuthInfoObj) throws Exception {
		ServiceAuthInfoEntity sie = findByAuthId(authId);
		if (sie != null) {
			return OsyncDB.get(CurrentContext.getCurrentOsyncId()).update(serviceAuthInfoObj);
		}
		return null;
		
	}
	
	public List<SyncLogEntity> findAll() throws Exception {
		OsyncDB osp = OsyncDB.get();
		SelectQuery<?> query = query().select(field("*")).from("SyncLog").orderBy(field("CREATEDTIME").desc())
				.limit(100).getQuery();
		return osp.executeSystemQuery(SyncLogEntity.class, query);
	}
	
	public List<IntegrationStatusEntity> getReportsByIntegStatus(String integstatusId) throws Exception {

		System.out.println(integstatusId);
		OsyncDB osp = OsyncDB.get();
		SelectQuery<Record> fetchById = query().selectFrom("IntegrationStatus")
				.where(field("integ_id").eq(integstatusId).or(field("osync_id").eq(integstatusId))
				).orderBy(field("ROWID").desc()).getQuery();
		return osp.executeSystemQuery(IntegrationStatusEntity.class, fetchById);
	}

	public List<IntegrationStatusEntity> findAllIntStatus(String osyncId) throws Exception {
		SelectQuery<Record> query = RequestController.query().selectFrom("IntegrationStatus")
				.where(field("osync_id").eq(osyncId)).getQuery();
		return OsyncDB.get().executeQuery(IntegrationStatusEntity.class, query);
	}
			
}
