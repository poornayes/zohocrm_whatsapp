package xyz.oapps.osync.repo;

import static org.jooq.impl.DSL.field;
import static xyz.oapps.osync.api.RequestController.getUUID;
import static xyz.oapps.osync.api.RequestController.query;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jooq.Record;
import org.jooq.SelectQuery;

import xyz.oapps.osync.CurrentContext;
import xyz.oapps.osync.db.OsyncDB;
import xyz.oapps.osync.entity.FieldMapEntity;
import xyz.oapps.osync.entity.IntegrationPropsEntity;
import xyz.oapps.osync.entity.ServiceAuthInfoEntity;
import xyz.oapps.osync.entity.UniqueValuesMapEntity;
import xyz.oapps.osync.util.CommonUtil;

public class ServiceAuthInfoRepository {
	private static Logger log = Logger.getLogger(ServiceAuthInfoRepository.class.getName());
	private static final String short_name = "sa_rep";
	IntegrationPropsRepository intPropsRepo = new IntegrationPropsRepository();

	public ServiceAuthInfoEntity findLeftServiceAuthInfo(String osyncId, String serviceId) throws Exception {
		return findServiceAuthInfo(osyncId, serviceId, true);
	}

	public ServiceAuthInfoEntity findRightServiceAuthInfo(String osyncId, String serviceId) throws Exception {
		return findServiceAuthInfo(osyncId, serviceId, false);
	}

	public ServiceAuthInfoEntity findServiceAuthInfo(String osyncId, String serviceId, boolean isLeft)
			throws Exception {
		SelectQuery<Record> query2 = query().selectFrom("ServiceAuthInfo").where(field("service_id").eq(serviceId))
				.and(field("osync_id").eq(osyncId))
				.and(field("left_service").eq(isLeft ? Boolean.TRUE.toString() : Boolean.FALSE.toString())).getQuery();
		ServiceAuthInfoEntity serviceAuthInfoEntity = null;
		try {
			serviceAuthInfoEntity = OsyncDB.get().findOne(ServiceAuthInfoEntity.class, query2);
		} catch (Exception e) {
			if (e.getMessage().contains("Requested one row, but more than 1 rows are present")) {
				List<ServiceAuthInfoEntity> saies = OsyncDB.get().executeQuery(ServiceAuthInfoEntity.class, query2);
				CommonUtil.logOsyncInfo(short_name,"More than 1 service auth info found.. cleaning up.." + saies.size());
				// starting from 1 so that the first element stays
				serviceAuthInfoEntity = saies.get(0);
				for (int i = 1; i < saies.size(); i++) {
					CommonUtil.logOsyncInfo(short_name,"cleaning up :" + saies.get(i).getRowId());
					OsyncDB.get().deleteByEntity(saies.get(i));
				}
			} else {
				log.log(Level.SEVERE, null, e);
			}
		}
		return serviceAuthInfoEntity;
	}

	public ServiceAuthInfoEntity save(ServiceAuthInfoEntity authInfoObj) throws Exception {
		ServiceAuthInfoEntity sie = null;
		if (authInfoObj.getRowId() == null) {
			authInfoObj.setAuthId(getUUID());
			sie = OsyncDB.get().insert(authInfoObj);
		} else {
			log.log(Level.INFO, "New getOsyncId,>>>>>>>" + authInfoObj.getOsyncId());
			sie = OsyncDB.get().update(authInfoObj);
		}

		log.log(Level.INFO, "New getAuthId,>>>>>>>" + sie.getAuthId());
		log.log(Level.INFO, "New getServiceId >>>>>>>" + sie.getServiceId());
		log.log(Level.INFO, "New getOsyncUserId,>>>>>>>" + sie.getOsyncUserId());
		return sie;
	}

	public boolean delete(String serviceId, String integId, boolean isLeft) throws Exception {
		boolean isDeleted = false;

		IntegrationPropsEntity findByIntegId = intPropsRepo.findById(integId);

		ServiceAuthInfoEntity findByOsyncIdAndServiceIdAndIntegId = isLeft
				? findLeftServiceAuthInfo(findByIntegId.getOsyncId(), serviceId)
				: findRightServiceAuthInfo(findByIntegId.getOsyncId(), serviceId);

		if (findByOsyncIdAndServiceIdAndIntegId != null) {
			OsyncDB.get().deleteById(ServiceAuthInfoEntity.class, findByOsyncIdAndServiceIdAndIntegId.getAuthId());
			log.log(Level.INFO, "Integration ID Deleted, {0} {1}",
					new Object[] { findByOsyncIdAndServiceIdAndIntegId.getOsyncId(),
							findByOsyncIdAndServiceIdAndIntegId.getIntegId(),
							findByOsyncIdAndServiceIdAndIntegId.getServiceId() });
			isDeleted = true;
		}
		CommonUtil.logOsyncInfo(short_name,"isDeleted>>>>>>>>>" + isDeleted);
		return isDeleted;
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

	public boolean deleteByEntity(ServiceAuthInfoEntity serviceAuth) throws Exception {
		OsyncDB.get().deleteByEntity(serviceAuth);
		return true;

	}

	public List<ServiceAuthInfoEntity> findEntity(String OsyncId) throws Exception {
		SelectQuery<?> query = query().select(field("*")).from("ServiceAuthInfo").getQuery();
		return OsyncDB.get().executeQuery(ServiceAuthInfoEntity.class, query);
	}

}