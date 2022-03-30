package xyz.oapps.osync.repo;

import static org.jooq.impl.DSL.*;
import static xyz.oapps.osync.api.RequestController.query;

import java.util.List;

import java.util.List;

import org.jooq.Record;
import org.jooq.SelectQuery;

import xyz.oapps.osync.CurrentContext;
import xyz.oapps.osync.api.RequestController;
import xyz.oapps.osync.db.OsyncDB;
import xyz.oapps.osync.entity.AccountInfoEntity;
import xyz.oapps.osync.entity.IntegrationStatusEntity;

public class IntegrationStatusRepository {

	public IntegrationStatusEntity findById(String integId) throws Exception {
		SelectQuery<Record> query = RequestController.query().selectFrom("IntegrationStatus")
				.where(field("integ_id").eq(integId)).getQuery();
		return OsyncDB.get().findOne(IntegrationStatusEntity.class, query);
	}

	public IntegrationStatusEntity save(IntegrationStatusEntity status) throws Exception {
		return OsyncDB.get().insert(status);
	}

	public IntegrationStatusEntity update(IntegrationStatusEntity status) throws Exception {
		return OsyncDB.get().update(status);
	}

	public List<IntegrationStatusEntity> findAllIntStatus(String osyncId) throws Exception {
		SelectQuery<Record> query = RequestController.query().selectFrom("IntegrationStatus")
				.where(field("osync_id").eq(osyncId)).getQuery();
		return OsyncDB.get().executeQuery(IntegrationStatusEntity.class, query);
	}

	public IntegrationStatusEntity deleteIntegrationStatus(String OsyncId) throws Exception {
		IntegrationStatusEntity findById = findById(OsyncId);
		if (findById != null) {
			OsyncDB.get().deleteByEntity(findById);
		}
		return findById;
	}

}