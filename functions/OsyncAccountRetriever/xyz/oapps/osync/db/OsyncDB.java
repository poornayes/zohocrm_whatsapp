package xyz.oapps.osync.db;

import static org.jooq.impl.DSL.field;
import static xyz.oapps.osync.api.RequestController.mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.conf.ParamType;
import org.json.simple.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.zc.component.object.ZCColumn;
import com.zc.component.object.ZCObject;
import com.zc.component.object.ZCRowObject;
import com.zc.component.object.ZCTable;
import com.zc.component.zcql.ZCQL;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import xyz.oapps.osync.CurrentContext;
import xyz.oapps.osync.annotation.Table;
import xyz.oapps.osync.api.RequestController;
import xyz.oapps.osync.entity.OsyncEntity;

public class OsyncDB {

	private static Logger log = Logger.getLogger(OsyncDB.class.getName());

	String osyncId = null;

	private OsyncDB(String osyncId) {
		this.osyncId = osyncId;
	}

	public static OsyncDB get(String osyncId) {
		return new OsyncDB(osyncId);
	}

	public static OsyncDB get() {
		return new OsyncDB(CurrentContext.getCurrentOsyncId());
	}

	public <T> T findById(Class<T> c, String id) throws Exception {
		SelectQuery<Record> query = getSingleDataFetchQuery(c, id);
		return convert(c, getRowObject(query));
	}

	private <T> SelectQuery<Record> getSingleDataFetchQuery(Class<T> c, String id) {
		Table table = c.getAnnotation(Table.class);
		String tableName = table.name();
		return RequestController.query().selectFrom(tableName).where(field(table.id()).eq(id)).getQuery();
	}
	
	public HashMap fetchCount(SelectQuery<?> query, String tableName) throws Exception {
		ArrayList<ZCRowObject> result = runSelectQuery(query, false);
		HashMap hashMap = new HashMap();
		if (result != null && result.size() == 1) {
			
			System.out.println("count >>>>>>>>>>>>>>>>>> "+result.get(0).get("count"));
			System.out.println("count >>>>>>>>>>>>>>>>>> "+result.get(0).get("IntegrationProps"));
			System.out.println("getRowObject >>>>>>>>>>>>>>>>>> "+result.get(0).getRowObject());
			
			String countRow = result.get(0).getRowObject().get(tableName).toString();
			org.json.JSONObject jsonObject = new org.json.JSONObject(countRow);
			System.out.println("getRowObject  jsonObject.get(\"ROWID\").toString() >>>>>>>>>>>>>>>>>> "+jsonObject.get("ROWID").toString());
			
			hashMap.put("count", Integer.valueOf(jsonObject.get("ROWID").toString()));
		}
		return hashMap;
	}

//	public int fetchCount(SelectQuery<?> query, String tableName) throws Exception {
//		ArrayList<ZCRowObject> result = runSelectQuery(query, false);
//		if (result != null && result.size() == 1) {
//			
//			System.out.println("count >>>>>>>>>>>>>>>>>> "+result.get(0).get("count"));
//			System.out.println("count >>>>>>>>>>>>>>>>>> "+result.get(0).get("IntegrationProps"));
//			System.out.println("getRowObject >>>>>>>>>>>>>>>>>> "+result.get(0).getRowObject());
//			
//			String countRow = result.get(0).getRowObject().get(tableName).toString();
//			org.json.JSONObject jsonObject = new org.json.JSONObject(countRow);
//			System.out.println("getRowObject  jsonObject.get(\"ROWID\").toString() >>>>>>>>>>>>>>>>>> "+jsonObject.get("ROWID").toString());
//			return Integer.valueOf(jsonObject.get("ROWID").toString());
//		}
//		return 0;
//	}
	private ZCRowObject getRowObject(SelectQuery<Record> query) throws Exception {
		ArrayList<ZCRowObject> result = runSelectQuery(query, true);
		if (result != null && result.size() == 1) {
			return result.get(0);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private <T> T convert(Class<T> c, ZCRowObject zcRowObject)
			throws IOException, JsonParseException, JsonMappingException {
		if (zcRowObject == null) {
			return null;
		}
		JSONObject rowObject = zcRowObject.getRowObject();
		Table table = c.getAnnotation(Table.class);
		String tableName = table.name();
		JSONObject object = (JSONObject) rowObject.getOrDefault(tableName, rowObject);
		return mapper().readValue(object.toJSONString(), c);
	}

	private <T> List<T> fetchMultiple(Class<T> c, SelectQuery<?> query, boolean useOsyncId)
			throws Exception, IOException, JsonParseException, JsonMappingException {
		List<T> resultSet = null;
		ArrayList<ZCRowObject> result = runSelectQuery(query, useOsyncId);
		if (result != null && result.size() > 0) {
			resultSet = new ArrayList<T>(result.size());
			for (ZCRowObject zcRowObject : result) {
				resultSet.add(convert(c, zcRowObject));
			}
		}
		return resultSet;
	}

	private static final List<String> excludeTables = Arrays.asList("ServiceInfo", "ServiceAuthInfo", "DefaultField",
			"DefaultFieldMap", "Module", "OsyncAuthorization", "OsyncConfiguration", "DigestAuth");

//	private String getName(net.sf.jsqlparser.schema.Table table) {
//		return table.getAlias() == null ? table.getName() : table.getAlias().getName();
//	}

	public <T> List<T> executeQuery(Class<T> c, SelectQuery<?> query) throws Exception {
		return fetchMultiple(c, query, true);
	}

	public <T> T findOne(Class<T> class1, SelectQuery<?> query) throws Exception {
		List<T> fetchMultiple = fetchMultiple(class1, query, true);
		if(fetchMultiple == null) {
			return null;
		}
		if (fetchMultiple.size() == 1) {
			return fetchMultiple.get(0);
		}
		if(fetchMultiple.size() > 1) {
			throw new Exception("Requested one row, but more than 1 rows are present. No of rows returned: " + fetchMultiple.size());
		}
		return null;
	}
	
	public <T> List<T> executeSystemQuery(Class<T> c, SelectQuery<?> query) throws Exception {
		return fetchMultiple(c, query, false);
	}

	public List<org.json.JSONObject> executeSystemQuery(SelectQuery<?> query) throws Exception {
		ArrayList<ZCRowObject> result = runSelectQuery(query, false);
		List<org.json.JSONObject> resultSet = null;
		if (result != null && result.size() > 0) {
			resultSet = new ArrayList<org.json.JSONObject>(result.size());
			for (ZCRowObject zcRowObject : result) {
				JSONObject rowObject = zcRowObject.getRowObject();
				org.json.JSONObject jsonObject = new org.json.JSONObject(rowObject.toJSONString());
				@SuppressWarnings("unchecked")
				Iterator<String> keys = jsonObject.keys();
				if(keys.hasNext()) {
					resultSet.add(jsonObject.getJSONObject(keys.next()));
				}
			}
		}
		return resultSet;
	}
	
	public List<org.json.JSONObject> executeQuery(SelectQuery<?> query) throws Exception {
		ArrayList<ZCRowObject> result = runSelectQuery(query, true);
		List<org.json.JSONObject> resultSet = null;
		if (result != null && result.size() > 0) {
			resultSet = new ArrayList<org.json.JSONObject>(result.size());
			for (ZCRowObject zcRowObject : result) {
				org.json.JSONObject jsonObject = new org.json.JSONObject(zcRowObject.getRowObject().toJSONString());
				@SuppressWarnings("unchecked")
				Iterator<String> keys = jsonObject.keys();
				if(keys.hasNext()) {
					resultSet.add(jsonObject.getJSONObject(keys.next()));
				}
			}
		}
		return resultSet;
	}

	private ArrayList<ZCRowObject> runSelectQuery(SelectQuery<?> query, boolean useOsyncId) throws Exception {

		
		String sql = query.getSQL(ParamType.INLINED);
//		CommonUtil.logOsyncInfo(short_name,"parsing " + sql);
		Select selectStatement = (Select) CCJSqlParserUtil.parse(sql);
		// get the body of the select query
		PlainSelect ps = (PlainSelect) selectStatement.getSelectBody();
		net.sf.jsqlparser.schema.Table table = (net.sf.jsqlparser.schema.Table) ps.getFromItem();
		

		if (!excludeTables.contains(table.getName())) {
			if(useOsyncId) {
				query.addConditions(field("osync_id").eq(osyncId));
			}
			sql = query.getSQL(ParamType.INLINED);
		}
//		CommonUtil.logOsyncInfo(short_name,"sql query >>>>>>>>>" + sql.toString());
		return ZCQL.getInstance().executeQuery(sql);
	}

	// <S extends T> S save(S entity);
	@SuppressWarnings("unchecked")
	public <S> S insert(S t) throws Exception {
		ZCObject zco = ZCObject.getInstance();
		Table table = t.getClass().getAnnotation(Table.class);
		String tableName = table.name();
		String idColumn = table.id();
		if (tableName != null) {
			ZCTable tab = zco.getTable(tableName);

			ZCRowObject row = ZCRowObject.getInstance();
			Map<String, Object> map = mapper().convertValue(t, HashMap.class);
			if (osyncId != null && !idColumn.equals("osync_id") && map.containsKey("osync_id") && !map.get("osync_id").equals(osyncId)) {
				throw new Exception("Osync ID Not same");
			}
		row.setRowObject(new JSONObject(map));
		if(!idColumn.trim().isEmpty()) {
			row.set(idColumn, RequestController.getUUID());
		}
		try {
			ZCRowObject newRow = tab.insertRow(row);
			return (S) convert(t.getClass(), newRow);
		} catch (Exception e) {
			log.log(Level.INFO, "Error on inserting a row " + row.getRowObject().toJSONString());
			for (ZCColumn column : tab.getAllColumns()) {
				if (column.getIsMandatory() && row.get(column.getColumnName()) == null) {
					log.log(Level.WARNING, "Mandatory column \"" + column.getColumnName() + "\" not found ");
				}
			}
			throw e;
		}
		}else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public <S> S update(S t) throws Exception {
		ZCObject zco = ZCObject.getInstance();
		Table table = t.getClass().getAnnotation(Table.class);
		String tableName = table.name();
		ZCTable tab = zco.getTable(tableName);

		ZCRowObject row = ZCRowObject.getInstance();

		Map<String, Object> map = mapper().convertValue(t, HashMap.class);
		if (osyncId!= null && map.containsKey("osync_id") && !map.get("osync_id").equals(osyncId)) {
			throw new Exception("Osync ID Not same");
		}
		row.setRowObject(new JSONObject(map));
		List<ZCRowObject> list = new ArrayList<ZCRowObject>();
		list.add(row);
		List<ZCRowObject> newRow = tab.updateRows(list);
		return (S) convert(t.getClass(), newRow.get(0));
	}

	public void deleteByEntity(OsyncEntity e) throws Exception {
		Table table = e.getClass().getAnnotation(Table.class);
		String tableName = table.name();
		ZCObject zco = ZCObject.getInstance();
		ZCTable tab = zco.getTable(tableName);
		ZCColumn column = tab.getColumn("osync_id");
		if (column != null) {
			ZCRowObject rowObject = tab.getRow(e.getRowId());
			if(!rowObject.get("osync_id").toString().equals(osyncId)) {
				throw new Exception("Osync ID Not same");
			}
		}
		tab.deleteRow(e.getRowId());
	}
	
	public <T> T deleteById(Class<T> c, String id) throws Exception {
		Table table = c.getAnnotation(Table.class);
		String tableName = table.name();
		ZCObject zco = ZCObject.getInstance();
		ZCTable tab = zco.getTable(tableName);
		SelectQuery<Record> query = getSingleDataFetchQuery(c, id);
		ZCRowObject rowObject = getRowObject(query);
		if(rowObject != null) {
			Object object = rowObject.get("ROWID");
			if (object != null) {
				tab.deleteRow(Long.valueOf(object.toString()));
			}
		}
		return convert(c, rowObject);
	}	
}
