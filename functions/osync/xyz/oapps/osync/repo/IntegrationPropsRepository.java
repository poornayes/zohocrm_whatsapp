package xyz.oapps.osync.repo;

import static org.jooq.impl.DSL.field;
import static xyz.oapps.osync.api.RequestController.query;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.SelectQuery;

import com.zc.component.object.ZCRowObject;

import xyz.oapps.osync.CurrentContext;
import xyz.oapps.osync.db.OsyncDB;
import xyz.oapps.osync.entity.FieldMapEntity;
import xyz.oapps.osync.entity.IntegrationPropsEntity;
import xyz.oapps.osync.entity.ModuleInfoEntity;
import xyz.oapps.osync.entity.ServiceAuthInfoEntity;
import xyz.oapps.osync.entity.UniqueValuesMapEntity;

public class IntegrationPropsRepository {
	private static Logger log = Logger.getLogger(OsyncDB.class.getName());
	ModuleInfoRepository repo = new ModuleInfoRepository();

	public List<IntegrationPropsEntity> findByOsyncId(String osyncId, String leftService, String rightService)
			throws Exception {
		SelectQuery<?> query = query().select(field("*")).from("IntegrationProps")
				.where(field("osync_id").eq(osyncId)
						.and(field("left_service_id").eq(leftService).and(field("right_service_id").eq(rightService))))
				.getQuery();
		return OsyncDB.get(CurrentContext.getCurrentOsyncId()).executeQuery(IntegrationPropsEntity.class, query);
	}

	public IntegrationPropsEntity findByOsyncIdAndIntegId(String osyncId, String integId) throws Exception {
		SelectQuery<?> query = query().select(field("*")).from("IntegrationProps").where(field("osync_id").eq(osyncId))
				.and(field("integ_id").eq(integId)).getQuery();
		return OsyncDB.get(CurrentContext.getCurrentOsyncId()).findOne(IntegrationPropsEntity.class, query);

	}

	public List<IntegrationPropsEntity> findByOsyncIdAndLeftServiceIdAndRightServiceId(String osyncId,
			String leftServiceId, String rightServiceId) throws Exception {

		SelectQuery<?> query = query().select(field("*")).from("IntegrationProps").where(field("osync_id").eq(osyncId)
				.and(field("left_Service_Id").eq(leftServiceId)).and(field("right_Service_Id").eq(rightServiceId)))
				.orderBy(field("ROWID").asc()).getQuery();
		System.out.println("IntegrationPropsEntity :::::: query >>>>>" + query.toString());

		return OsyncDB.get().executeQuery(IntegrationPropsEntity.class, query);
	}

	public IntegrationPropsEntity save(IntegrationPropsEntity intInfoObj) throws Exception {
		fillLookupColumn(intInfoObj);
		return OsyncDB.get().insert(intInfoObj);
	}

	private boolean isValid(String uniqueColumn) {
		return uniqueColumn != null && !uniqueColumn.trim().isEmpty();
	}

	public IntegrationPropsEntity update(String integId, IntegrationPropsEntity intInfoObj) throws Exception {

		if (intInfoObj != null) {
			fillLookupColumn(intInfoObj);
			return OsyncDB.get().update(intInfoObj);
		}
		return null;
	}

	FieldMapRepository fieldMapRepo = new FieldMapRepository();

	private void fillLookupColumn(IntegrationPropsEntity intInfoObj) throws Exception {
		intInfoObj.setIntegId(intInfoObj.getIntegId());
		String leftModuleId = intInfoObj.getLeftModuleId();
		String rightModuleId = intInfoObj.getRightModuleId();
		ModuleInfoEntity leftModule = repo.findByModuleId(leftModuleId);
		ModuleInfoEntity rightModule = repo.findByModuleId(rightModuleId);
		if (leftModule != null && rightModule != null && isValid(leftModule.getUniqueColumn())
				&& isValid(rightModule.getUniqueColumn())) {
			SelectQuery<?> query = query()
					.select(field("*")).from("FieldMap").where(
							(field("integ_id").eq(intInfoObj.getIntegId())
									.and(field("left_column_name").eq(leftModule.getUniqueColumn())
											.and(field("right_column_name").eq(rightModule.getUniqueColumn())))))
					.getQuery();
			List<FieldMapEntity> fes = OsyncDB.get().executeQuery(FieldMapEntity.class, query);
			if (fes != null && fes.size() > 0) {
				intInfoObj.setSyncRecordsWithEmail(true);
				intInfoObj.setLookupUniqueColumn(true);
			}
		} else {
			intInfoObj.setSyncRecordsWithEmail(false);
			intInfoObj.setLookupUniqueColumn(false);
		}
	}

	public IntegrationPropsEntity findById(String id) throws Exception {
		return OsyncDB.get().findById(IntegrationPropsEntity.class, id);
	}

	public IntegrationPropsEntity findTopByOsyncIdAndIntegId(String osyncId, String integId) throws Exception {
		SelectQuery<?> query = query().select(field("*")).from("IntegrationProps").where(field("osync_id").eq(osyncId))
				.and(field("integ_id").eq(integId)).getQuery();
		return OsyncDB.get().findOne(IntegrationPropsEntity.class, query);
	}

	public IntegrationPropsEntity findTopByLeftModuleId(String leftModuleId) throws Exception {
		SelectQuery<?> query = query().select(field("*")).from("IntegrationProps")
				.where(field("left_module_id").eq(leftModuleId)).getQuery();
		return OsyncDB.get(CurrentContext.getCurrentOsyncId()).findOne(IntegrationPropsEntity.class, query);
	}

	public IntegrationPropsEntity findTopByRightModuleId(String rightModuleId) throws Exception {
		SelectQuery<?> query = query().select(field("*")).from("IntegrationProps")
				.where(field("right_module_id").eq(rightModuleId)).getQuery();
		return OsyncDB.get(CurrentContext.getCurrentOsyncId()).findOne(IntegrationPropsEntity.class, query);
	}

	public IntegrationPropsEntity findTopByMasterService(String masterService) throws Exception {
		SelectQuery<?> query = query().select(field("*")).from("IntegrationProps")
				.where(field("master_service").eq(masterService)).getQuery();
		return OsyncDB.get(CurrentContext.getCurrentOsyncId()).findOne(IntegrationPropsEntity.class, query);
	}

	public List<IntegrationPropsEntity> findAllByOsyncId(String osyncId) throws Exception {
		OsyncDB osp = OsyncDB.get(osyncId);
		SelectQuery<Record> fetchById = query().selectFrom("IntegrationProps").where(field("osync_id").eq(osyncId))
				.getQuery();
		return osp.executeQuery(IntegrationPropsEntity.class, fetchById);
	}
	
	public IntegrationPropsEntity deleteIntegrationByIntegId(String integId) throws Exception {
		IntegrationPropsEntity findById = findById(integId);
		if(findById != null) {
			OsyncDB.get().deleteByEntity(findById);
		}
		return findById;
	}

	public List<IntegrationPropsEntity> findEntity(String OsyncId) throws Exception {
		SelectQuery<?> query = query().select(field("*")).from("IntegrationProps").getQuery();
		return OsyncDB.get().executeQuery(IntegrationPropsEntity.class, query);
	}

	public List<IntegrationPropsEntity> findAllIntPropsForSync(Long rowId , int offset , int limit) throws Exception{
		OsyncDB osp = OsyncDB.get(null);
		
		SelectQuery<?> query = query().select(field("*")).from("IntegrationProps")
				.where(
						field("sync_status").eq(1)
						.and(field("ROWID").greaterThan(rowId))
						).orderBy(field("ROWID").asc())
				.limit(offset,limit)
				.getQuery();
		return osp.executeSystemQuery(IntegrationPropsEntity.class, query);
	}
	public List<IntegrationPropsEntity> findAllIntOsyncIdsForSync(Long rowId , int offset , int limit) throws Exception{
		OsyncDB osp = OsyncDB.get(null);
		
		SelectQuery<?> query = query().select(field("osync_id")).from("IntegrationProps")
				.where(
						field("sync_status").eq(1)
						.and(field("ROWID").greaterThan(rowId))
						).orderBy(field("ROWID").asc())
				.limit(offset,limit)
				.getQuery();
		return osp.executeSystemQuery(IntegrationPropsEntity.class, query);
	}
	public Object getIntegrationPropsCount() throws Exception{
		OsyncDB osp = OsyncDB.get(null);
		SelectQuery<Record1<Object>> query2 = query().select(field("count(ROWID)")).from("IntegrationProps").where(field("sync_status").eq(1)).getQuery();
		return osp.fetchCount(query2,"IntegrationProps");
	}
}
