package xyz.oapps.osync.repo;

import xyz.oapps.osync.entity.AccountUserInfoEntity;
import static org.jooq.impl.DSL.field;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.SelectQuery;

import org.jooq.impl.DSL;

import xyz.oapps.osync.CurrentContext;
import xyz.oapps.osync.db.OsyncDB;

public class AccountUserInfoRepository {

	public DSLContext query() {
		return DSL.using(SQLDialect.SQLITE);
	}

	public AccountUserInfoEntity findByOsyncId(String osyncId) throws Exception {
		SelectQuery<?> accountUserquery = query().select(field("*")).from("osync_account_user")
				.where(field("osync_id").eq(osyncId)).getQuery();
		return OsyncDB.get(CurrentContext.getCurrentOsyncId()).findOne(AccountUserInfoEntity.class,
				accountUserquery);
	}

	public AccountUserInfoEntity insert(AccountUserInfoEntity userInfo) throws Exception {
		return OsyncDB.get(userInfo.getOsyncId()).insert(userInfo);
	}
}