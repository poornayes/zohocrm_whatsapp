package xyz.oapps.osync.repo;

import static org.jooq.impl.DSL.field;
import static xyz.oapps.osync.api.RequestController.query;

import org.jooq.Record;
import org.jooq.SelectQuery;

import xyz.oapps.osync.api.RequestController;
import xyz.oapps.osync.db.OsyncConfig;
import xyz.oapps.osync.db.OsyncDB;
import xyz.oapps.osync.entity.AuthorizationEntity;

public class AuthorizationRepo {

	public AuthorizationEntity findByToken(String token) throws Exception {
		SelectQuery<Record> query2 = query().selectFrom("OsyncAuthorization").where(field("token").eq(token))
				.getQuery();
		return OsyncDB.get().findOne(AuthorizationEntity.class, query2);
	}

	public AuthorizationEntity create(String osyncId, String osyncUserId) throws Exception {
		AuthorizationEntity authEntity = new AuthorizationEntity();
		authEntity.setAdmin(false);
		authEntity.setOsyncId(osyncId);
		authEntity.setOsyncUserId(osyncUserId);
		String token = RequestController.getUUID().replace("-", "");
		authEntity.setToken(token);
		return OsyncDB.get(osyncId).insert(authEntity);
	}
	
	public AuthorizationEntity createAdminToken(String osyncUserId) throws Exception {
		String adminOsyncId = OsyncConfig.getConfig("admin.account.osyncid", "this-is-not-a-valid-osyncid");
		AuthorizationEntity authEntity = new AuthorizationEntity();
		authEntity.setAdmin(true);
		authEntity.setOsyncId(adminOsyncId);
		authEntity.setOsyncUserId(osyncUserId);
		String token = RequestController.getUUID().replace("-", "");
		authEntity.setToken(token);
		return OsyncDB.get(adminOsyncId).insert(authEntity);
	}
	
}
