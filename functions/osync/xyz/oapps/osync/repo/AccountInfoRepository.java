package xyz.oapps.osync.repo;

import static org.jooq.impl.DSL.field;
import static xyz.oapps.osync.api.RequestController.query;

import java.util.List;

import org.jooq.SelectQuery;

import xyz.oapps.osync.CurrentContext;
import xyz.oapps.osync.db.OsyncDB;
import xyz.oapps.osync.entity.AccountInfoEntity;
import xyz.oapps.osync.entity.AccountUserInfoEntity;
import xyz.oapps.osync.entity.CronDetailsEntity;
import xyz.oapps.osync.entity.IntegrationPropsEntity;
import xyz.oapps.osync.entity.IntegrationStatusEntity;
import xyz.oapps.osync.entity.ServiceAuthInfoEntity;

public class AccountInfoRepository {

	public List<AccountInfoEntity> findByRemoteIdentifier(String remoteIdentifier) throws Exception {
		OsyncDB osp = OsyncDB.get(CurrentContext.getCurrentOsyncId());
		return osp.executeQuery(AccountInfoEntity.class, query().select(field("*")).from("OsyncAccount")
				.where(field("remote_identifier").eq(remoteIdentifier)).getQuery());
	}

	public AccountInfoEntity findByServiceId(Long serviceId) throws Exception {
		OsyncDB osp = OsyncDB.get(CurrentContext.getCurrentOsyncId());
		SelectQuery<?> query = query().select(field("*")).from("OsyncAccount").where(field("service_id").eq(serviceId))
				.getQuery();
		return osp.findOne(AccountInfoEntity.class, query);
	}

	public AccountInfoEntity findByOsyncId(String osyncId) throws Exception {
		OsyncDB osp = OsyncDB.get(CurrentContext.getCurrentOsyncId());
		SelectQuery<?> query = query().select(field("*")).from("OsyncAccount").where(field("osync_id").eq(osyncId))
				.getQuery();
		return osp.findOne(AccountInfoEntity.class, query);
	}

	public AccountInfoEntity findTopByRemoteIdentifierAndEmail(String remoteIdentifier, String email) throws Exception {
		OsyncDB osp = OsyncDB.get(CurrentContext.getCurrentOsyncId());
		SelectQuery<?> query = query().select(field("*")).from("OsyncAccount")
				.where(field("remote_identifier").eq(remoteIdentifier).and(field("email").eq(email))).getQuery();
		return osp.findOne(AccountInfoEntity.class, query);
	}

	public AccountInfoEntity save(AccountInfoEntity accInfoObj) throws Exception {
		// System.out.println(accInfoObj.getEmail() + ":::" + accInfoObj.getName() +
		// ":::" + accInfoObj.getRemoteIdentifier());
		return OsyncDB.get().insert(accInfoObj);
	}

	public CronDetailsEntity findCronByOsyncId(String osyncId) throws Exception {
		OsyncDB osp = OsyncDB.get(CurrentContext.getCurrentOsyncId());
		SelectQuery<?> query = query().select(field("*")).from("CronDetails").where(field("osync_id").eq(osyncId))
				.getQuery();
		return osp.findOne(CronDetailsEntity.class, query);
	}

	public boolean deleteCronByOsyncId(String osyncId) throws Exception {
		OsyncDB osp = OsyncDB.get(CurrentContext.getCurrentOsyncId());
		CronDetailsEntity cronDetailsEntity = findCronByOsyncId(osyncId);
		osp.deleteByEntity(cronDetailsEntity);
		return true;
	}

	public CronDetailsEntity createCron(CronDetailsEntity cron) throws Exception {
		OsyncDB osp = OsyncDB.get(CurrentContext.getCurrentOsyncId());
		return osp.insert(cron);
	}

	public AccountUserInfoEntity findUserByEmail(String osyncId, String emailId) throws Exception {
		OsyncDB osp = OsyncDB.get(osyncId);
		SelectQuery<?> query = query().select(field("*")).from("AccountUserInfo")
				.where(field("email").eq(emailId).and(field("osync_id").eq(osyncId))).getQuery();
		return osp.findOne(AccountUserInfoEntity.class, query);
	}

	public List<AccountInfoEntity> findEntity(String OsyncId) throws Exception {
		SelectQuery<?> query = query().select(field("*")).from("AccountUserInfo").getQuery();
		return OsyncDB.get().executeQuery(AccountInfoEntity.class, query);
	}

}