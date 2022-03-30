package xyz.oapps.osync.db;

import static org.jooq.impl.DSL.field;
import static xyz.oapps.osync.api.RequestController.mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.SelectQuery;
import org.jooq.conf.ParamType;
import org.json.simple.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.zc.component.object.ZCObject;
import com.zc.component.object.ZCRowObject;
import com.zc.component.object.ZCTable;
import com.zc.component.zcql.ZCQL;

import xyz.oapps.osync.CurrentContext;
import xyz.oapps.osync.annotation.Table;

public class OsyncPersistence {

//	private static Logger log = Logger.getLogger(OsyncPersistence.class.getName());

	String osyncId = null;

	private OsyncPersistence(String osyncId) {
		this.osyncId = osyncId;
	}

	public static OsyncPersistence get(String osyncId) {
		return new OsyncPersistence(osyncId);
	}
	
	public static OsyncPersistence get() {
		return new OsyncPersistence(CurrentContext.getCurrentOsyncId());
	}

	public <T> T findById(Class<T> c, String id) throws Exception {
		String query = getSingleDataFetchQuery(c, id);
		return convert(c, getRowObject(query));
	}

	private <T> String getSingleDataFetchQuery(Class<T> c, String id) {
		Table table = c.getAnnotation(Table.class);
		String tableName = table.name();
//		String query = "select * from " + tableName + " where osync_id = " + osyncId + " AND " + table.id() + " = " + id
//				+ " limit 1";
		
		String query = "select * from " + tableName + " where " + table.id() + " = " + id
				+ " limit 1";
		return query;
	}

	private ZCRowObject getRowObject(String query) throws Exception {
		ArrayList<ZCRowObject> result = ZCQL.getInstance().executeQuery(query);
		if (result != null && result.size() == 1) {
			return result.get(0);
		}
		return null;
	}

	private <T> T convert(Class<T> c, ZCRowObject zcRowObject)
			throws IOException, JsonParseException, JsonMappingException {
		JSONObject rowObject = zcRowObject.getRowObject();
		Table table = c.getAnnotation(Table.class);
		String tableName = table.name();
		JSONObject object = (JSONObject)rowObject.getOrDefault(tableName, rowObject);
		return mapper().readValue(object.toJSONString(), c);
	}

	public <T> List<T> findAllByOsyncId(Class<T> c) throws Exception {
		Table table = c.getAnnotation(Table.class);
		String tableName = table.name();
		String query = "select * from " + tableName + " where osync_id = " + osyncId;
		return fetchMultiple(c, query);
	}

	private <T> List<T> fetchMultiple(Class<T> c, String query)
			throws Exception, IOException, JsonParseException, JsonMappingException {
		List<T> resultSet = null;
		ArrayList<ZCRowObject> result = ZCQL.getInstance().executeQuery(query);
		if (result != null && result.size() > 0) {
			resultSet = new ArrayList<T>(result.size());
			for (ZCRowObject zcRowObject : result) {
				resultSet.add(convert(c, zcRowObject));
			}
		}
		return resultSet;
	}

//	private static final List<String> excludeTables = Arrays.asList("service_info", "service_auth_info",
//			"default_field", "default_field_map", "module", "osync_authorization");

//	private String getName(net.sf.jsqlparser.schema.Table table) {
//		return table.getAlias() == null ? table.getName() : table.getAlias().getName();
//	}

	public <T> List<T> executeQuery(Class<T> c, SelectQuery<?> query) throws Exception {
		addOsyncCondition(query);
		String sql = toSQL(query);
		return fetchMultiple(c, sql);
	}

	private String toSQL(SelectQuery<?> query) {
		return query.getSQL(ParamType.INLINED);
	}

	private void addOsyncCondition(SelectQuery<?> query) {
		if (!isExcludeNow()) {
			query.addConditions(field("osync_id").eq(osyncId));
		}
	}

	private boolean isExcludeNow() {
		return true;
	}

	public <T> T findOne(Class<T> class1, SelectQuery<?> query) throws Exception {
		addOsyncCondition(query);
		String sql = toSQL(query);
		List<T> fetchMultiple = fetchMultiple(class1, sql);
		if (fetchMultiple != null && fetchMultiple.size() == 1) {
			return fetchMultiple.get(0);
		}
		return null;
	}

	public List<JSONObject> executeQuery(SelectQuery<?> query) throws Exception {
		addOsyncCondition(query);
		String sql = toSQL(query);
		ArrayList<ZCRowObject> result = ZCQL.getInstance().executeQuery(sql);
		List<JSONObject> resultSet = null;
		if (result != null && result.size() > 0) {
			resultSet = new ArrayList<JSONObject>(result.size());
			for (ZCRowObject zcRowObject : result) {
				resultSet.add(zcRowObject.getRowObject());
			}
		}
		return resultSet;
	}

	// <S extends T> S save(S entity);
	public <S> S insert(S t) throws Exception {
		ZCObject zco = ZCObject.getInstance();
		Table table = t.getClass().getAnnotation(Table.class);
		String tableName = table.name();
		ZCTable tab = zco.getTable(tableName);

		ZCRowObject row = ZCRowObject.getInstance();

		Map<String, Object> map = mapper().convertValue(t, HashMap.class);
		row.setRowObject(new JSONObject(map));
		ZCRowObject newRow = tab.insertRow(row);
		return (S) convert(t.getClass(), newRow);
	}

	public <S> S update(S t) throws Exception {
		ZCObject zco = ZCObject.getInstance();
		Table table = t.getClass().getAnnotation(Table.class);
		String tableName = table.name();
		ZCTable tab = zco.getTable(tableName);

		ZCRowObject row = ZCRowObject.getInstance();

		Map<String, Object> map = mapper().convertValue(t, HashMap.class);
		row.setRowObject(new JSONObject(map));
		List<ZCRowObject> list = new ArrayList<ZCRowObject>();
		list.add(row);
		List<ZCRowObject> newRow = tab.updateRows(list);
		return (S) convert(t.getClass(), newRow.get(0));
	}

	public <T> T deleteById(Class<T> c, String id) throws Exception {
		Table table = c.getAnnotation(Table.class);
		String tableName = table.name();
		ZCObject zco = ZCObject.getInstance();
		ZCTable tab = zco.getTable(tableName);
		tab.deleteRow((Long) getRowObject(getSingleDataFetchQuery(c, id)).get("row_id"));
		return null;
	}
}
