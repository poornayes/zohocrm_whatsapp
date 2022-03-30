package xyz.oapps.osync.repo;

import static org.jooq.impl.DSL.field;
import static xyz.oapps.osync.api.RequestController.query;

import java.util.List;

import org.jooq.Record;
import org.jooq.SelectQuery;

import xyz.oapps.osync.CurrentContext;
import xyz.oapps.osync.api.RequestController;
import xyz.oapps.osync.db.OsyncDB;
import xyz.oapps.osync.entity.FieldMapEntity;
import xyz.oapps.osync.entity.ServiceAuthInfoEntity;
import xyz.oapps.osync.entity.UniqueValuesMapEntity;

public class UniqueValuesMapRepo {

	public UniqueValuesMapEntity findByIntegIdAndLeftUniqueValue(String integId, String leftUniqueValue)
			throws Exception {
		SelectQuery<Record> query = RequestController.query().selectFrom("UniqueValuesMap")
				.where(field("integ_id").eq(integId).and(field("left_unique_value").eq(leftUniqueValue))).getQuery();
		return OsyncDB.get().findOne(UniqueValuesMapEntity.class, query);
	}

	public UniqueValuesMapEntity findByIntegIdAndRightUniqueValue(String integId, String rightUniqueValue)
			throws Exception {
		SelectQuery<Record> query = RequestController.query().selectFrom("UniqueValuesMap")
				.where(field("integ_id").eq(integId).and(field("right_unique_value").eq(rightUniqueValue))).getQuery();
		return OsyncDB.get().findOne(UniqueValuesMapEntity.class, query);

	}

	public List<UniqueValuesMapEntity> findByIntegIdAndLeftUniqueValueIn(String integId, List<String> uniqueIns)
			throws Exception {
		if (uniqueIns == null || uniqueIns.size() == 0) {
			return null;
		}
		SelectQuery<Record> query = RequestController.query().selectFrom("UniqueValuesMap")
				.where(field("integ_id").eq(integId).and(field("left_unique_value").in(uniqueIns))).getQuery();
		return OsyncDB.get().executeQuery(UniqueValuesMapEntity.class, query);
	}

	public List<UniqueValuesMapEntity> findByIntegIdAndRightUniqueValueIn(String integId, List<String> uids)
			throws Exception {
		if (uids == null || uids.size() == 0) {
			return null;
		}
		SelectQuery<Record> query = RequestController.query().selectFrom("UniqueValuesMap")
				.where(field("integ_id").eq(integId).and(field("right_unique_value").in(uids))).getQuery();
		return OsyncDB.get().executeQuery(UniqueValuesMapEntity.class, query);
	}

	public UniqueValuesMapEntity save(UniqueValuesMapEntity uniqueValuesMap) throws Exception {
		return OsyncDB.get().insert(uniqueValuesMap);
	}

	public List<UniqueValuesMapEntity> findEntity(String OsyncId) throws Exception {
		SelectQuery<?> query = query().select(field("*")).from("UniqueValuesMap").getQuery();
		return OsyncDB.get().executeQuery(UniqueValuesMapEntity.class, query);
	}

	public void deleteOsyncEntity(String osyncId) throws Exception {
		OsyncDB.get().deleteById(ServiceAuthInfoEntity.class, osyncId);

	}

}
