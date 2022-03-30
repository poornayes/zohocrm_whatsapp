package xyz.oapps.osync.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import xyz.oapps.osync.CurrentContext;
import xyz.oapps.osync.OsyncLogHandler;
import xyz.oapps.osync.SyncHandler;
import xyz.oapps.osync.SyncHandlerRepo;
import xyz.oapps.osync.User;
import xyz.oapps.osync.annotation.PathVariable;
import xyz.oapps.osync.annotation.RequestMapping;
import xyz.oapps.osync.annotation.RequestParam;
import xyz.oapps.osync.api.OsyncConstants.AccessLevel;
import xyz.oapps.osync.cron.OsyncCron;
import xyz.oapps.osync.entity.IntegrationPropsEntity;
import xyz.oapps.osync.entity.ServiceAuthInfoEntity;
import xyz.oapps.osync.entity.ServiceInfoEntity;
import xyz.oapps.osync.fields.Fields;
import xyz.oapps.osync.fields.Record;
import xyz.oapps.osync.fields.RecordSet;
import xyz.oapps.osync.repo.IntegrationPropsRepository;
import xyz.oapps.osync.repo.ModuleInfoRepository;
import xyz.oapps.osync.repo.ServiceAuthInfoRepository;
import xyz.oapps.osync.repo.ServiceInfoRepository;
import xyz.oapps.osync.service.IntegrationService;
import xyz.oapps.osync.util.EmailSender;
import xyz.oapps.osync.util.ErrorEmailSender;

public class TestController {
	private static Logger LOGGER = Logger.getLogger(TestController.class.getName());
	IntegrationPropsRepository intPropsRepo = new IntegrationPropsRepository();

	IntegrationService intService = new IntegrationService();

	ServiceAuthInfoRepository serviceAuthRepo = new ServiceAuthInfoRepository();
	//","osync_id":""
	String osyncId = "49deaee1-61a9-4ecc-8c61-fcbe7645c38c";
	String integId = "ecbe3139-c415-4cbc-8002-6001b5474933";
	String handler = "zohocrm";
	String module = "Contacts";
	boolean isLeft = false;

	public void initValues() throws Exception {
		HttpServletRequest request = OsyncLogHandler.getCurrentRequest();
		if (request.getParameter("osync_id") != null) {
			this.osyncId = request.getParameter("osync_id");
		}
		CurrentContext.getCurrentContext().setCurrentContext(osyncId, null, 1);
		if (request.getParameter("integ_id") != null) {
			this.integId = request.getParameter("integ_id");
		}
		if (request.getParameter("handler") != null) {
			this.handler = request.getParameter("handler");
		}
		if (request.getParameter("handler_module") != null) {
			this.module = request.getParameter("handler_module");
		}
		IntegrationPropsRepository ipr = new IntegrationPropsRepository();
		IntegrationPropsEntity findById = ipr.findById(this.integId);
		if (findById != null) {
			ServiceInfoRepository sir = new ServiceInfoRepository();
			List<ServiceInfoEntity> sies = sir.findAll();
			for (ServiceInfoEntity sie : sies) {
				if(sie.getName().equalsIgnoreCase(handler)) {
					if(sie.getServiceId().equalsIgnoreCase(findById.getLeftServiceId())) {
						isLeft = true;
						break;
					} else if (sie.getServiceId().equalsIgnoreCase(findById.getRightServiceId())) {
						isLeft = false;
						break;
					}
				}
			}
		}
	}

	public SyncHandler getController() throws Exception {
		ServiceInfoRepository sir = new ServiceInfoRepository();
		ModuleInfoRepository mir = new ModuleInfoRepository();
		ServiceInfoEntity service = sir.findByName(handler);
		return SyncHandlerRepo.getInstance(service, mir.findByModuleName(service.getServiceId(), module), osyncId, integId, isLeft);
	}

	@RequestMapping(path = "/api/v1/test-create-records", method = "get", produces = "application/json", accessLevel = AccessLevel.PUBLIC)
	public HashMap<String, String> testCreateNewRecords() throws Exception {
		initValues();
		RecordSet rs = RecordSet.init(handler, "id");
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

	@RequestMapping(path = "/api/v1/test-fetch-records", method = "get", produces = "application/json", accessLevel = AccessLevel.PUBLIC)
	public RecordSet testFetchRecords() throws Exception {
		initValues();
		return getController().fetchRecords(1, 100, -1l);
	}

	@RequestMapping(path = "/api/v1/test-fetch-fields", method = "get", produces = "application/json", accessLevel = AccessLevel.PUBLIC)
	public Fields testFetchFields() throws Exception {
		initValues();
		return getController().getFields();
	}

	@RequestMapping(path = "/api/v1/test-fetch-by-id", method = "get", produces = "application/json", accessLevel = AccessLevel.PUBLIC)
	public RecordSet testFetchFieldsById() throws Exception {
		initValues();
		//HashMap<String, String> records = testCreateNewRecords();
		List<String> ids = new ArrayList<String>();
		/*for (Entry<String, String> entry : records.entrySet()) {
			if (!entry.getKey().startsWith("email_created_")) {
				ids.add(entry.getValue());
			}
		}*/
		ids.add("33520226");
		ids.add("33520227");
		return getController().getMatchedRecordsById(ids);
	}

	@RequestMapping(path = "/api/v1/test-fetch-by-email", method = "get", produces = "application/json", accessLevel = AccessLevel.PUBLIC)
	public RecordSet testFetchFieldsByEmail() throws Exception {
		initValues();
		HashMap<String, String> records = testCreateNewRecords();
		List<String> emails = new ArrayList<String>();
		for (Entry<String, String> entry : records.entrySet()) {
			if (entry.getKey().startsWith("email")) {
				emails.add(entry.getValue());
			}
		}
		return getController().getMatchedRecordsByUniqueColumn(emails);
	}

	@RequestMapping(path = "/api/v1/test-fetch-by-email-1/{email}", method = "get", produces = "application/json", accessLevel = AccessLevel.PUBLIC)
	public RecordSet testFetchFieldsByEmail(@PathVariable("email") String email) throws Exception {
		initValues();
		List<String> emails = new ArrayList<String>();
		emails.add(email);
		return getController().getMatchedRecordsByUniqueColumn(emails);
	}

	@RequestMapping(path = "/api/v1/test-update-records", method = "get", produces = "application/json", accessLevel = AccessLevel.PUBLIC)
	public RecordSet testUpdateRecords() throws Exception {
		initValues();
		RecordSet recordSet = testFetchFieldsById();
		List<String> ids = new ArrayList<String>();
		for (Record record : recordSet) {
			Date date = new Date();
			record.addOrUpdateValue("Last_Name", record.getValue("Last_Name") + date.toLocaleString());
			ids.add(record.getUniqueValue());
		}
		getController().updateRecords(recordSet, null);
		return getController().getMatchedRecordsById(ids);
	}

	@RequestMapping(path = "/api/v1/current-user", method = "get", produces = "application/json", accessLevel = AccessLevel.PUBLIC)
	public User getUser() throws Exception {
		initValues();
		return getController().getCurrentUser();
	}

	@RequestMapping(path = "/api/v1/send-email", method = "get", produces = "application/json", accessLevel = AccessLevel.PUBLIC)
	public void sendEmail() throws Exception {

		CurrentContext.setCurrentContext("ab4f2d6c-5914-47e9-8a5d-bb505ace9436");
		System.out.println(">>>>>>>>>>>"+CurrentContext.getCurrentOsyncId());
		ServiceAuthInfoEntity serviceAuth = serviceAuthRepo.findByAuthId("4d1e9d5c-91c0-4204-b926-9b7740d27941");
		System.out.println(" serviceAuth >>>>>>>"+serviceAuth);
		EmailSender emailObj = new EmailSender(serviceAuth.getUserEmail(),serviceAuth.getIntegId(),serviceAuth.getUserFullName(),"<strong style=\"color:red\">Authentication Failure</strong>",false);

		emailObj.sendEmail();

		ErrorEmailSender emailAdminObj = new ErrorEmailSender(serviceAuth.getUserEmail(),serviceAuth.getIntegId(),serviceAuth.getUserFullName(),"<strong style=\"color:red\"> Authentication Failure </strong> for the user <strong>"+serviceAuth.getUserFullName()+" ("+serviceAuth.getUserEmail()+")</strong>",true);
		emailAdminObj.sendEmail();
	}

	@RequestMapping(path = "/api/v1/createcron", method = "GET", produces = "application/json", accessLevel = AccessLevel.PUBLIC)
	public Long createCron(@RequestParam("osync_id") String osyncId, @RequestParam("interval") Integer interval)
			throws Exception {
		return OsyncCron.createCron(osyncId, interval);
	}
	
}
