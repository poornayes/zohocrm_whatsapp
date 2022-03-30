package xyz.oapps.osync.api;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import xyz.oapps.osync.OsyncException;
import xyz.oapps.osync.OsyncLogHandler;
import xyz.oapps.osync.annotation.PathVariable;
import xyz.oapps.osync.annotation.RequestBody;
import xyz.oapps.osync.annotation.RequestMapping;
import xyz.oapps.osync.annotation.RequestParam;
import xyz.oapps.osync.api.OsyncConstants.AccessLevel;
import xyz.oapps.osync.db.OsyncConfig;
import xyz.oapps.osync.entity.AccountInfoEntity;
import xyz.oapps.osync.entity.AccountUserInfoEntity;
import xyz.oapps.osync.entity.AuthorizationEntity;
import xyz.oapps.osync.entity.DigestAuthEntity;
import xyz.oapps.osync.entity.FieldMapEntity;
import xyz.oapps.osync.entity.IntegrationPropsEntity;
import xyz.oapps.osync.entity.IntegrationStatusEntity;
import xyz.oapps.osync.entity.ModuleInfoEntity;
import xyz.oapps.osync.entity.ServiceAuthInfoEntity;
import xyz.oapps.osync.entity.ServiceInfoEntity;
import xyz.oapps.osync.entity.SyncLogEntity;
import xyz.oapps.osync.repo.AccountInfoRepository;
import xyz.oapps.osync.repo.AdminRepository;
import xyz.oapps.osync.repo.AuthorizationRepo;
import xyz.oapps.osync.repo.IntegrationStatusRepository;
import xyz.oapps.osync.repo.ServiceInfoRepository;
import xyz.oapps.osync.repo.SyncLogEntityRepo;
import xyz.oapps.osync.util.EmailSender;

public class AdminController {

	private static final Logger log = Logger.getLogger(AdminController.class.getName());

	private ServiceInfoRepository serviceRepo = new ServiceInfoRepository();

	private AccountInfoRepository accRepo = new AccountInfoRepository();

	private AuthorizationRepo authRepo = new AuthorizationRepo();

	private AdminRepository adminRepo = new AdminRepository();
	
	SyncLogEntityRepo syncRepo = new SyncLogEntityRepo();

	IntegrationStatusRepository integstatusRepo = new IntegrationStatusRepository();


	@RequestMapping(path = "/adminapi/v1/ping", method = "get", produces = "application/json", accessLevel = AccessLevel.OSYNC_ADMIN)
	public boolean ping() throws Exception {
		return true;
	}

	@RequestMapping(path = "/adminapi/v1/login", method = "post", produces = "application/json", accessLevel = AccessLevel.PUBLIC)
	public boolean adminLogin(@RequestBody String payload) throws Exception {
		JSONObject json = new JSONObject(payload);
		String email = json.getString("email");
		String osyncId = OsyncConfig.getConfig("admin.account.osyncid", "this-is-not-a-valid-osyncid");
		AccountUserInfoEntity userInfo = accRepo.findUserByEmail(osyncId, email);
		if(userInfo == null) {
			throw new Exception("Error");
		}
		if (userInfo != null && userInfo.isAdminUser()) {
			long time = System.currentTimeMillis();
			time += 15 * 60 * 60 * 1000l;
			DigestAuthEntity digestAuth = DigestAuthValidator.createNewDigestAuth(userInfo.getOsyncId(), "user",
					userInfo.getEmail(), new Date(time), true);
			//String subject,String content, String toEmailAddress, String fromEmailAddress
			EmailSender es = new EmailSender("Your OTP",  "Prove yourself - : " + digestAuth.getDigestValue(),userInfo.getEmail(),
					"help@oapps.xyz");
			
			//EmailSender es = new EmailSender("Your OTP",  "Prove yourself - : " + digestAuth.getDigestValue(),"devibalan@oapps.xyz","help@oapps.xyz");
			es.sendEmail();
			return true;
		}
		throw new Exception("Error");
	}


	@RequestMapping(path = "/adminapi/v1/validate", method = "post", produces = "application/json", accessLevel = AccessLevel.PUBLIC)
	public AuthorizationEntity adminValidate(@RequestBody String payload) throws Exception {
		JSONObject json = new JSONObject(payload);
		String email = json.getString("email");
		String otp = json.getString("otp");
		String adminOsyncId = OsyncConfig.getConfig("admin.account.osyncid", "this-is-not-a-valid-osyncid");
		if (otp != null) {
			DigestAuthEntity digestAuth = DigestAuthValidator.validateDigestAuth(otp);
			if (digestAuth != null) {
				String osyncId = digestAuth.getOsyncId();
				if (adminOsyncId.equals(osyncId) && digestAuth.getEntityId().equals(email)) {
					String entityId = digestAuth.getEntityId();
					HttpServletResponse response = OsyncLogHandler.getCurrentResponse();
					AuthorizationEntity authEnt = authRepo.createAdminToken(entityId);
					RequestController.addAuthCookie(response, authEnt);
					return authEnt;
				} else {
					throw new OsyncException(OsyncException.Code.INVALID_CREDENTIALS);
				}
			}
		}
		throw new OsyncException(OsyncException.Code.INVALID_CREDENTIALS);
	}
	


	@RequestMapping(path = "/adminapi/v1/service", method = "post", produces = "application/json", accessLevel = AccessLevel.OSYNC_ADMIN)
	public ServiceInfoEntity addService(@RequestBody ServiceInfoEntity serviceInfoObj) throws Exception {
		ServiceInfoEntity sie = serviceRepo.insert(serviceInfoObj);
		log.log(Level.INFO, "New service created, {0} {1}", new Object[] { sie.getServiceId(), sie.getName() });
		return sie;

	}

	@RequestMapping(path = "/adminapi/v1/services", method = "get", produces = "application/json", accessLevel = AccessLevel.OSYNC_ADMIN)
	public List<ServiceInfoEntity> getServices() throws Exception {
		return serviceRepo.findAll();
	}

	@RequestMapping(path = "/adminapi/v1/modules", method = "get", produces = "application/json", accessLevel = AccessLevel.OSYNC_ADMIN)
	public List<ModuleInfoEntity> getModules() throws Exception {
		return adminRepo.findAllModules();
	}

	@RequestMapping(path = "/adminapi/v1/service/{service_id}", method = "put", produces = "application/json", accessLevel = AccessLevel.OSYNC_ADMIN)
	public ServiceInfoEntity updateService(@PathVariable("service_id") String serviceId,
			@RequestBody ServiceInfoEntity serviceInfoObj) throws Exception {
		ServiceInfoEntity sie = serviceRepo.update(serviceId, serviceInfoObj);
		log.log(Level.INFO, "New service created, {0} {1}", new Object[] { sie.getServiceId(), sie.getName() });
		return sie;

	}

	@RequestMapping(path = "/adminapi/v1/service/{service_id}", method = "get", produces = "application/json", accessLevel = AccessLevel.OSYNC_ADMIN)
	public ServiceInfoEntity getService(@PathVariable("service_id") String serviceId) throws Exception {
		ServiceInfoEntity sie = serviceRepo.findByServiceId(serviceId);
		if (sie != null) {
			System.out.println(sie.getServiceId() + ":::" + sie.getName());
		} else {
			System.out.println("Sie is  nulll");
		}
		return sie;
	}

	@RequestMapping(path = "/adminapi/v1/customer", method = "get", produces = "application/json", accessLevel = AccessLevel.OSYNC_ADMIN)
	public List<AccountInfoEntity> getCustomer(@RequestParam("searchText") String searchText) throws Exception {
		return adminRepo.findByType(searchText);
	}

	@RequestMapping(path = "/adminapi/v1/customer/integration/{integ_id}", method = "get", produces = "application/json", accessLevel = AccessLevel.OSYNC_ADMIN)
	public List<IntegrationPropsEntity> getIntegrationByIntegId(@PathVariable("integ_id") String integId)
			throws Exception {
		return adminRepo.getIntegrationByIntegId(integId);
	}

	@RequestMapping(path = "/adminapi/v1/customer/integration/{integ_id}/fields", method = "get", produces = "application/json", accessLevel = AccessLevel.OSYNC_ADMIN)
	public List<FieldMapEntity> getFieldsByIntegId(@PathVariable("integ_id") String integId) throws Exception {
		return adminRepo.getAllFieldsByIntegId(integId);
	}

	@RequestMapping(path = "/adminapi/v1/customer/integration/{integ_id}/reports", method = "get", produces = "application/json", accessLevel = AccessLevel.OSYNC_ADMIN)
	public List<SyncLogEntity> getReportsByIntegId(@PathVariable("integ_id") String integId) throws Exception {
		return adminRepo.findAllByIntegId(integId);
	}

	@RequestMapping(path = "/adminapi/v1/customer/records", method = "get", produces = "application/json", accessLevel = AccessLevel.OSYNC_ADMIN)
	public List<SyncLogEntity> getReportsBySyncLog() throws Exception {
		return adminRepo.findAll();
	}

	@RequestMapping(path = "/adminapi/v1/customer/integstatus/{integstatusId}", method="get", produces = "application/json" , accessLevel = AccessLevel.OSYNC_ADMIN)
	public List<IntegrationStatusEntity> getReportsByIntegStatus(@PathVariable("integstatusId") String integstatusId) throws Exception {
		return adminRepo.getReportsByIntegStatus(integstatusId);
	}
	
	@RequestMapping(path = "/adminapi/v1/customer/authinfo/{authinfoId}", method="get", produces = "application/json" , accessLevel = AccessLevel.OSYNC_ADMIN)
	public List<ServiceAuthInfoEntity> getReportsByServiceAuthInfo(@PathVariable("authinfoId") String authinfoId) throws Exception {
		return adminRepo.getReportsByServiceAuthInfo(authinfoId);
	}
	
}
