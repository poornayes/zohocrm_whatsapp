package xyz.oapps.osync.repo;

import static xyz.oapps.osync.api.RequestController.query;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jooq.Record;
import org.jooq.SelectQuery;

import xyz.oapps.osync.CurrentContext;
import xyz.oapps.osync.db.OsyncDB;
import xyz.oapps.osync.entity.ServiceInfoEntity;
import xyz.oapps.osync.entity.SyncLogEntity;

public class ServiceInfoRepository {

	private static Logger log = Logger.getLogger(ServiceInfoRepository.class.getName());

	public ServiceInfoEntity findByServiceId(String serviceId) throws Exception {
		findAll();
		for (ServiceInfoEntity service : services) {
			if(service.getServiceId().equals(serviceId)) {
				return service;
			}
		}
		return null;
	}

	public ServiceInfoEntity findByName(String name) throws Exception {
		findAll();
		for (ServiceInfoEntity service : services) {
			if(service.getName().equalsIgnoreCase(name)) {
				return service;
			}
		}
		return null;
	}

	public ServiceInfoEntity insert(ServiceInfoEntity authInfoObj) throws Exception {
		// authInfoObj.setServiceId(getUUID());
		ServiceInfoEntity sie = OsyncDB.get(CurrentContext.getCurrentOsyncId()).insert(authInfoObj);
		log.log(Level.INFO, "New service created, {0} {1}", new Object[] { sie.getServiceId(), sie.getName() });
		return sie;
	}

	public ServiceInfoEntity update(String serviceId, ServiceInfoEntity authInfoObj) throws Exception {
		ServiceInfoEntity sie = findByServiceId(serviceId);
		if (sie != null) {
			authInfoObj.setServiceId(sie.getServiceId());
			return OsyncDB.get(CurrentContext.getCurrentOsyncId()).update(authInfoObj);
		}
		return null;
	}

	static List<ServiceInfoEntity> services = null;
	
	public List<ServiceInfoEntity> findAll() throws Exception {
		if (services == null) {
			SelectQuery<Record> query2 = query().selectFrom("ServiceInfo").getQuery();
			services = OsyncDB.get().executeQuery(ServiceInfoEntity.class, query2);
		}
		return services;
	}
	
	

}
