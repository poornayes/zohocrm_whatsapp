package xyz.oapps.osync.repo;

import java.util.List;

import static org.jooq.impl.DSL.field;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;

import xyz.oapps.osync.CurrentContext;
import xyz.oapps.osync.db.OsyncDB;

import xyz.oapps.osync.entity.DefaultFieldEntity;

public class DefaultFieldsRepo {

	public DSLContext query() {
		return DSL.using(SQLDialect.SQLITE);
	}

	public List<DefaultFieldEntity> findByServiceIdAndModuleId(String serviceId, String moduleId) throws Exception {

		SelectQuery<?> query = query().selectFrom("default_field")
				.where(field("service_id").eq(serviceId).and(field("module_id").eq(moduleId))).getQuery();
		return OsyncDB.get(CurrentContext.getCurrentOsyncId()).executeQuery(DefaultFieldEntity.class, query);
	}

	public DefaultFieldEntity insert(DefaultFieldEntity entity) throws Exception {
		return OsyncDB.get().insert(entity);
	}

	public DefaultFieldEntity findById(String id) throws Exception {
		return OsyncDB.get().findById(DefaultFieldEntity.class, id);
	}
}
