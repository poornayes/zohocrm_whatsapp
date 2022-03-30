package xyz.oapps.osync.api;

import java.util.Arrays;
import java.util.List;

import com.zc.component.object.ZCObject;
import com.zc.component.object.ZCRowObject;
import com.zc.component.object.ZCTable;

import xyz.oapps.osync.annotation.RequestMapping;
import xyz.oapps.osync.api.OsyncConstants.AccessLevel;

public class CleanupController {
	List<String> ignoreTables = Arrays.asList("ServiceInfo", "Module", "OsyncConfiguration");

	String token = "1ee4372a1c894d34b4aec3a4ed4e78f7";

	@RequestMapping(path = "/cleanup", method = "get", accessLevel = AccessLevel.PUBLIC)
	public boolean cleanup() throws Exception {
//		ZCObject zco = ZCObject.getInstance();
//		List<ZCTable> allTables = zco.getAllTables();
//		for (ZCTable zcTable : allTables) {
//			if (!ignoreTables.contains(zcTable.getName())) {
//				System.out.println("Deleting from table:" + zcTable.getName());
//				List<ZCRowObject> allRows = zcTable.getAllRows();
//				for (ZCRowObject zcRow : allRows) {
//					try {
//						System.out.println("Deleting " + zcRow.getRowObject());
//						if ("OsyncAuthorization".equals(zcTable.getName())) {
//							Boolean admin = (Boolean) zcRow.get("admin");
//							if (admin != null && admin.booleanValue() == true) {
//								System.out.println("Skipped admin row");
//								continue;
//							}
//						}
//						zcTable.deleteRow(Long.valueOf((String) zcRow.get("ROWID")));
//					} catch (Exception e) {
//						System.out.println("ERROR Deleting " + zcRow.getRowObject());
//					}
//				}
//			} else {
//				System.out.println("Table ignored:" + zcTable.getName());
//			}
//		}
		return true;
	}
}
