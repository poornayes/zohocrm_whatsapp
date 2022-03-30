package xyz.oapps.osync.repo;

import static org.jooq.impl.DSL.field;
import static xyz.oapps.osync.api.RequestController.query;

import java.util.List;

import org.jooq.SelectQuery;

import xyz.oapps.osync.CurrentContext;
import xyz.oapps.osync.db.OsyncDB;
import xyz.oapps.osync.entity.DefaultFieldMapEntity;

public class DefaultFieldsMappingRepo {

	public List<DefaultFieldMapEntity> findAllDefaultMappings(String leftServiceId, String leftModuleId,
			String rightServiceId, String rightModuleId) throws Exception {

		OsyncDB osp = OsyncDB.get(CurrentContext.getCurrentOsyncId());

		SelectQuery<?> query = query().select(field("*")).from("DefaultFieldMap")
		.where(field("left_module_id").eq(leftModuleId)
				.and(field("right_module_id").eq(rightModuleId)))
		.getQuery();
		
		return osp.executeQuery(DefaultFieldMapEntity.class, query);
	}

}