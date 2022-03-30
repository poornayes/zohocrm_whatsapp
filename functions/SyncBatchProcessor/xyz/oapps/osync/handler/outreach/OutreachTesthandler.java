package xyz.oapps.osync.handler.outreach;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import xyz.oapps.osync.CurrentContext;
import xyz.oapps.osync.SyncHandlerRepo;
import xyz.oapps.osync.SyncHandler;
import xyz.oapps.osync.annotation.RequestMapping;
import xyz.oapps.osync.api.OsyncConstants.AccessLevel;
import xyz.oapps.osync.entity.IntegrationPropsEntity;
import xyz.oapps.osync.fields.Fields;
import xyz.oapps.osync.fields.Record;
import xyz.oapps.osync.fields.RecordSet;
import xyz.oapps.osync.repo.IntegrationPropsRepository;
import xyz.oapps.osync.service.IntegrationService;
import xyz.oapps.osync.handler.outreach.OutreachHandler;

public class OutreachTesthandler {
	// localhost:3000/server/osync/api/v1/outreachprospect-test-create-records?module=outreach
	@RequestMapping(path = "/api/v1/outreachprospect-test-create-records", method = "get", produces = "application/json", accessLevel = AccessLevel.PUBLIC)
	public HashMap<String, String> testCreateNewRecords() throws Exception {
		RecordSet rs = RecordSet.init("outReach", "id");
		List<String> emailsCreated = new ArrayList<String>();
		OutreachHandler OutreachHandler = new OutreachHandler();
		for (int i = 0; i < 10; i++) {
			Record record = rs.createEmptyObject();
			String randomValue = "Test Z" + i + " " + System.currentTimeMillis();
			String phone = "123456789".concat(Integer.toString(i));
			// record.addOrUpdateValue("mobilePhones", phone);
			String firstName = "Devibalan" + i;
			record.addOrUpdateValue("firstName", firstName);
			String lastName = "Mahadevan" + i;
			record.addOrUpdateValue("lastName", lastName);
			String email1 = randomValue.replace(" ", "").toLowerCase() + "@oappsxyz.com";
			String[] email = { "devibalan.mahadevan@gmail.com" };
			// record.addOrUpdateValue("emails", email);
			record.setMappedRecordUniqueValue(randomValue.replace(" ", "").toLowerCase());
		}

		HashMap<String, String> newRecords = OutreachHandler.createNewRecords(rs, null);
		int i = 0;
		for (String string : emailsCreated) {
			newRecords.put("email_created_" + i, string);
			i++;
		}
		return newRecords;
	}

	// localhost:3000/server/osync/api/v1/outreachprospect-test-fetch-by-id?module=outreach
	@RequestMapping(path = "/api/v1/outreachprospect-test-fetch-by-id", method = "get", produces = "application/json", accessLevel = AccessLevel.PUBLIC)
	public RecordSet testFetchFieldsById() throws Exception {

		// HashMap<String, String> records = testCreateNewRecords();
		List<String> ids = new ArrayList<String>();
		/*
		 * for (Entry<String, String> entry : records.entrySet()) { if
		 * (!entry.getKey().startsWith("email_created_")) { ids.add(entry.getValue()); }
		 * }
		 */
		ids.add("132");
		// ids.add("33520227");
		OutreachHandler OutreachHandler = new OutreachHandler();
		return OutreachHandler.getMatchedRecordsById(ids);
	}

	// localhost:3000/server/osync/api/v1/outreachprospect-fetch-by-email?module=outreach
	@RequestMapping(path = "/api/v1/outreachprospect-fetch-by-email", method = "get", produces = "application/json", accessLevel = AccessLevel.PUBLIC)
	public RecordSet testFetchFieldsByEmail() throws Exception {
		// HashMap<String, String> records = testCreateNewRecords();
		List<String> emails = new ArrayList<String>();
		emails.add("devibalan.mahadevan@oappsxyz.com");
		/*
		 * for (Entry<String, String> entry : records.entrySet()) { if
		 * (entry.getKey().startsWith("testz41594038255563")) {
		 * emails.add(entry.getValue()); } }
		 */
		OutreachHandler OutreachHandler = new OutreachHandler();
		return OutreachHandler.getMatchedRecordsByUniqueColumn(emails);
	}

	// localhost:3000/server/osync/api/v1/outreachprospect-update-records?module=outreach
	@RequestMapping(path = "/api/v1/outreachprospect-update-records", method = "get", produces = "application/json", accessLevel = AccessLevel.PUBLIC)
	public RecordSet testUpdateRecords() throws Exception {
		RecordSet recordSet = testFetchFieldsById();
		List<String> ids = new ArrayList<String>();
		for (Record record : recordSet) {
			Date date = new Date();
			record.addOrUpdateValue("Last_Name", record.getValue("Boon") + date.toLocaleString());
			ids.add(record.getUniqueValue());
		}
		OutreachHandler OutreachHandler = new OutreachHandler();
		OutreachHandler.updateRecords(recordSet, null);
		return OutreachHandler.getMatchedRecordsById(ids);
	}

	// localhost:3000/server/osync/api/v1/outreachprospect-fetch-records?module=outreach
	@RequestMapping(path = "/api/v1/outreachprospect-fetch-records", method = "get", produces = "application/json", accessLevel = AccessLevel.PUBLIC)
	public RecordSet testFetchRecords() throws Exception {
		OutreachHandler OutreachHandler = new OutreachHandler();
		return OutreachHandler.fetchRecords(1, 100, -1l);
	}
///////////////////////////////////
	/////////////////////////
	/////////////////////////////////////////////////////////////////////

	// localhost:3000/server/osync/api/v1/outreachAccounttest-fetch-fields?module=outreach
	@RequestMapping(path = "/api/v1/outreachAccounttest-fetch-fields", method = "get", produces = "application/json", accessLevel = AccessLevel.PUBLIC)
	public Fields testFetchFields() throws Exception {
		OutreachHandler OutreachHandler = new OutreachHandler();
		return OutreachHandler.getFields();
	}

	// localhost:3000/server/osync/api/v1/outreachAccounttest-test-create-records?module=outreach
	@RequestMapping(path = "/api/v1/outreachAccounttest-test-create-records", method = "get", produces = "application/json", accessLevel = AccessLevel.PUBLIC)
	public HashMap<String, String> testoutreachAccountCreateNewRecords() throws Exception {
		RecordSet rs = RecordSet.init("outReach", "id");
		List<String> emailsCreated = new ArrayList<String>();
		OutreachHandler OutreachHandler = new OutreachHandler();
		for (int i = 0; i < 10; i++) {
			Record record = rs.createEmptyObject();
			String randomValue = "Test Z" + i + " " + System.currentTimeMillis();
			String name = "cognizant" + i;
			record.addOrUpdateValue("name", name);
			String domain = name + ".com";
			record.addOrUpdateValue("domain", domain);
			record.setMappedRecordUniqueValue(randomValue.replace(" ", "").toLowerCase());
		}

		HashMap<String, String> newRecords = OutreachHandler.createNewRecords(rs, null);
		int i = 0;
		for (String string : emailsCreated) {
			newRecords.put("email_created_" + i, string);
			i++;
		}
		return newRecords;
	}

	// localhost:3000/server/osync/api/v1/outreachAccounttest-test-fetch-by-id?module=outreach
	@RequestMapping(path = "/api/v1/outreachAccounttest-test-fetch-by-id", method = "get", produces = "application/json", accessLevel = AccessLevel.PUBLIC)
	public RecordSet accounttestFetchFieldsById() throws Exception {

		// HashMap<String, String> records = testCreateNewRecords();
		List<String> ids = new ArrayList<String>();
		/*
		 * for (Entry<String, String> entry : records.entrySet()) { if
		 * (!entry.getKey().startsWith("email_created_")) { ids.add(entry.getValue()); }
		 * }
		 */
		ids.add("209");
		// ids.add("33520227");
		OutreachHandler OutreachHandler = new OutreachHandler();
		return OutreachHandler.getMatchedRecordsById(ids);
	}

	// localhost:3000/server/osync/api/v1/outreachAccounttest-fetch-by-email?module=outreach
	@RequestMapping(path = "/api/v1/outreachAccounttest-fetch-by-email", method = "get", produces = "application/json", accessLevel = AccessLevel.PUBLIC)
	public RecordSet accountTestFetchFieldsByEmail() throws Exception {
		// HashMap<String, String> records = testCreateNewRecords();
		List<String> emails = new ArrayList<String>();
		emails.add("devibalan.mahadevan@oappsxyz.com");
		/*
		 * for (Entry<String, String> entry : records.entrySet()) { if
		 * (entry.getKey().startsWith("testz41594038255563")) {
		 * emails.add(entry.getValue()); } }
		 */
		OutreachHandler OutreachHandler = new OutreachHandler();
		return OutreachHandler.getMatchedRecordsByUniqueColumn(emails);
	}

	// localhost:3000/server/osync/api/v1/outreachAccounttest-update-records?module=outreach
	@RequestMapping(path = "/api/v1/outreachAccounttest-update-records", method = "get", produces = "application/json", accessLevel = AccessLevel.PUBLIC)
	public RecordSet accountTestUpdateRecords() throws Exception {
		RecordSet recordSet = testFetchFieldsById();
		List<String> ids = new ArrayList<String>();
		for (Record record : recordSet) {
			Date date = new Date();
			record.addOrUpdateValue("Last_Name", record.getValue("Boon") + date.toLocaleString());
			ids.add(record.getUniqueValue());
		}
		OutreachHandler OutreachHandler = new OutreachHandler();
		OutreachHandler.updateRecords(recordSet, null);
		return OutreachHandler.getMatchedRecordsById(ids);
	}

	// localhost:3000/server/osync/api/v1/outreachAccounttest-fetch-records?module=outreach
	@RequestMapping(path = "/api/v1/outreachAccounttest-fetch-records", method = "get", produces = "application/json", accessLevel = AccessLevel.PUBLIC)
	public RecordSet accounttestFetchRecords() throws Exception {
		OutreachHandler OutreachHandler = new OutreachHandler();
		return OutreachHandler.fetchRecords(1, 100, -1l);
	}

}
