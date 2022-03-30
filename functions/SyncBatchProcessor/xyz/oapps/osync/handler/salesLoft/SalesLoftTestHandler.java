package xyz.oapps.osync.handler.salesLoft;

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
import xyz.oapps.osync.entity.ModuleInfoEntity;
import xyz.oapps.osync.entity.ServiceInfoEntity;
import xyz.oapps.osync.fields.Fields;
import xyz.oapps.osync.fields.Record;
import xyz.oapps.osync.fields.RecordSet;
import xyz.oapps.osync.repo.IntegrationPropsRepository;
import xyz.oapps.osync.repo.ModuleInfoRepository;
import xyz.oapps.osync.repo.ServiceInfoRepository;
import xyz.oapps.osync.service.IntegrationService;

public class SalesLoftTestHandler {
	private static Logger LOGGER = Logger.getLogger(SalesLoftTestHandler.class.getName());
	IntegrationPropsRepository intPropsRepo = new IntegrationPropsRepository();

	IntegrationService intService = new IntegrationService();
	public SyncHandler getController() throws Exception {
		ServiceInfoRepository sir = new ServiceInfoRepository();
		ModuleInfoRepository mir = new ModuleInfoRepository();
		ServiceInfoEntity service = sir.findByName("SalesLoft");
		ModuleInfoEntity module = mir.findByModuleName(service.getServiceId(), "People");
		return SyncHandlerRepo.getInstance(service, module, "942b9642-7681-4686-a8e2-afce5d03793f", "942b9642-7681-4686-a8e2-afce5d03793f", true);
	}

	@RequestMapping(path = "/api/v1/test-create-records", method = "get", produces = "application/json", accessLevel = AccessLevel.OSYNC_ADMIN)
	public HashMap<String, String> testCreateNewRecords() throws Exception {
		RecordSet rs = RecordSet.init("SalesLoft", "id");
		List<String> emailsCreated = new ArrayList<String>();
		for (int i = 0; i < 10; i++) {
			Record record = rs.createEmptyObject();
			String randomValue = "Test Z" + i + " " + System.currentTimeMillis();
			String phone="123456789".concat(Integer.toString(i));
			record.addOrUpdateValue("phone", phone);
			String last_name="bala" + i;
			record.addOrUpdateValue("last_name", last_name);
			String email = randomValue.replace(" ", "").toLowerCase() + "@oappsxyz.com";
			emailsCreated.add(email);
			record.addOrUpdateValue("email_address", email);
			record.setMappedRecordUniqueValue(randomValue.replace(" ", "").toLowerCase());
		}

		HashMap<String, String> newRecords = getController().createNewRecords(rs, null);
		int i = 0;
		for (String string : emailsCreated) {
			newRecords.put("email_created_" + i, string);
			i++;
		}
		return newRecords;
	}

	@RequestMapping(path = "/api/v1/test-fetch-records", method = "get", produces = "application/json", accessLevel = AccessLevel.OSYNC_ADMIN)
	public RecordSet testFetchRecords() throws Exception {
		return getController().fetchRecords(1, 100, -1l);
	}

	@RequestMapping(path = "/api/v1/test-fetch-fields", method = "get", produces = "application/json", accessLevel = AccessLevel.OSYNC_ADMIN)
	public Fields testFetchFields() throws Exception {
		return getController().getFields();
	}

	@RequestMapping(path = "/api/v1/test-fetch-by-id", method = "get", produces = "application/json", accessLevel = AccessLevel.OSYNC_ADMIN)
	public RecordSet testFetchFieldsById() throws Exception {

		//HashMap<String, String> records = testCreateNewRecords();
		List<String> ids = new ArrayList<String>();
		/*for (Entry<String, String> entry : records.entrySet()) {
			if (!entry.getKey().startsWith("email_created_")) {
				ids.add(entry.getValue());
			}
		}*/
		ids.add("33520226");
		//ids.add("33520227");
		return getController().getMatchedRecordsById(ids);
	}

	@RequestMapping(path = "/api/v1/test-fetch-by-email", method = "get", produces = "application/json", accessLevel = AccessLevel.OSYNC_ADMIN)
	public RecordSet testFetchFieldsByEmail() throws Exception {
		//HashMap<String, String> records = testCreateNewRecords();
		List<String> emails = new ArrayList<String>();
		emails.add("testz41594038255563@oappsxyz.com");
		/*for (Entry<String, String> entry : records.entrySet()) {
			if (entry.getKey().startsWith("testz41594038255563")) {
			emails.add(entry.getValue());
			}
		}*/
		return getController().getMatchedRecordsByUniqueColumn(emails);
	}

	@RequestMapping(path = "/api/v1/test-update-records", method = "get", produces = "application/json", accessLevel = AccessLevel.OSYNC_ADMIN)
	public RecordSet testUpdateRecords() throws Exception {
		RecordSet recordSet = testFetchFieldsById();
		List<String> ids = new ArrayList<String>();
		for (Record record : recordSet) {
			Date date = new Date();
			record.addOrUpdateValue("Last_Name", record.getValue("Devibalan") + date.toLocaleString());
			ids.add(record.getUniqueValue());
		}
		getController().updateRecords(recordSet, null);
		return getController().getMatchedRecordsById(ids);
	}

}
