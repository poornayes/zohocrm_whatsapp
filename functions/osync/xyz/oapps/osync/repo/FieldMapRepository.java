package xyz.oapps.osync.repo;

import static org.jooq.impl.DSL.field;
import static xyz.oapps.osync.api.RequestController.query;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;

import xyz.oapps.osync.CurrentContext;
import xyz.oapps.osync.api.RequestController;
import xyz.oapps.osync.db.OsyncDB;
import xyz.oapps.osync.entity.FieldMapEntity;
import xyz.oapps.osync.entity.IntegrationStatusEntity;
import xyz.oapps.osync.entity.ServiceAuthInfoEntity;
import xyz.oapps.osync.entity.ServiceInfoEntity;
import xyz.oapps.osync.entity.UniqueValuesMapEntity;
import xyz.oapps.osync.util.CommonUtil;

public class FieldMapRepository {

	private static Logger log = Logger.getLogger(ServiceInfoEntity.class.getName());
	private static final String short_name = "fm_rep";

	public DSLContext query() {
		return DSL.using(SQLDialect.SQLITE);
	}

	public List<FieldMapEntity> findAllByIntegId(String integId) throws Exception {
		SelectQuery<?> query = query().select(field("*")).from("FieldMap").where(field("integ_id").eq(integId))
				.getQuery();
		return OsyncDB.get().executeQuery(FieldMapEntity.class, query);
	}

	public FieldMapEntity findByOsyncIdAndIntegId(String osyncId, String integId) throws Exception {
		SelectQuery<?> query = query().select(field("*")).from("FieldMap")
				.where(field("osync_id").eq(osyncId).and(field("integ_id").eq(integId))).getQuery();

		return OsyncDB.get(CurrentContext.getCurrentOsyncId()).findOne(FieldMapEntity.class, query);
	}

	public List<FieldMapEntity> findAll() throws Exception {
		OsyncDB osp = OsyncDB.get(CurrentContext.getCurrentOsyncId());
		SelectQuery<?> query = query().select(field("*")).from("FieldMap").getQuery();

		return osp.executeQuery(FieldMapEntity.class, query);

	}

	public void delete(FieldMapEntity fieldMapEntity) throws Exception {
		OsyncDB.get(CurrentContext.getCurrentOsyncId()).deleteByEntity(fieldMapEntity);
		CommonUtil.logOsyncInfo(short_name, "Integration ID Deleted, osyncId >>>"+fieldMapEntity.getOsyncId() +"::: getIntegId >>" +fieldMapEntity.getIntegId());
	}

	public void save(FieldMapEntity fieldMapEntity) throws Exception {
		fieldMapEntity = OsyncDB.get(CurrentContext.getCurrentOsyncId()).insert(fieldMapEntity);
		CommonUtil.logOsyncInfo(short_name, "New service created,, osyncId >>>"+fieldMapEntity.getOsyncId() +"::: getIntegId >>" +fieldMapEntity.getIntegId());

	}

	public FieldMapEntity findTopByOsyncIdAndIntegId(String osyncId, String integId) throws Exception {
		SelectQuery<?> query = query().select(field("*")).from("FieldMap")
				.where(field("osync_id").eq(osyncId).and(field("integ_id").eq(integId))).getQuery();
		return OsyncDB.get().findOne(FieldMapEntity.class, query);
	}

	public boolean isFieldMapAdded(String osyncId, String integId) throws Exception {
		SelectQuery<?> query = query().select(field("*")).from("FieldMap")
				.where(field("osync_id").eq(osyncId).and(field("integ_id").eq(integId))).limit(1).getQuery();
		return OsyncDB.get().findOne(FieldMapEntity.class, query) != null;
	}

	public List<FieldMapEntity> findEntity(String OsyncId) throws Exception {
		SelectQuery<?> query = query().select(field("*")).from("FieldMap").getQuery();
		return OsyncDB.get().executeQuery(FieldMapEntity.class, query);
	}

	
}
