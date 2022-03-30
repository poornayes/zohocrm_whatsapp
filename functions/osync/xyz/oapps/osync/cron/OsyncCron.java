package xyz.oapps.osync.cron;

import org.json.simple.JSONObject;

import com.zc.component.cron.CRONTYPE;
import com.zc.component.cron.ZCCron;
import com.zc.component.cron.ZCCronDetail;

import xyz.oapps.osync.db.OsyncConfig;
import xyz.oapps.osync.entity.CronDetailsEntity;
import xyz.oapps.osync.repo.AccountInfoRepository;

public class OsyncCron {
	static AccountInfoRepository accountRepo = new AccountInfoRepository();

	public static Long createCron(String osyncId, int intervalInMinutes) throws Exception {

		int hour = intervalInMinutes / 60;
		int minutes = intervalInMinutes % 60;
		ZCCronDetail periodicCron = ZCCronDetail.getInstance();
		periodicCron.setCronName(osyncId);
		periodicCron.setCronType(CRONTYPE.PERIODIC);
		periodicCron.setStatus(true);
		periodicCron.setCronFunctionId(getCronFuntionId());
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("osync_id", osyncId);
		periodicCron.Request.setParams(jsonObject);
		periodicCron.Config.setMinute(minutes).setHour(hour).setSeconds(0);
		ZCCronDetail cron = ZCCron.getInstance().createCron(periodicCron);
		return cron.getCronJobId();
	}

	public static ZCCronDetail getCron(Long cronId) throws Exception {
		return ZCCron.getInstance().getCron(cronId);
	}

	public static boolean pauseCron(Long cronId) throws Exception {
		ZCCronDetail cron = getCron(cronId);
		cron.setStatus(false);
		ZCCron.getInstance().updateCron(cron);
		return true;
	}
	
	public static boolean pauseCron(String osyncId) throws Exception {
		CronDetailsEntity cronDetails = accountRepo.findCronByOsyncId(osyncId);
		if(cronDetails == null) {
			return true;
		}
		return pauseCron(cronDetails.getCronJobId());
	}

	public static boolean restartCron(Long cronId, int intervalInMinutes) throws Exception {
		ZCCronDetail cron = getCron(cronId);
		cron.setStatus(true);
		int hour = intervalInMinutes / 60;
		int minutes = intervalInMinutes % 60;
		cron.Config.setMinute(minutes).setHour(hour).setSeconds(0);
		ZCCron.getInstance().updateCron(cron);
		return true;
	}

	private static Long getCronFuntionId() throws Exception {
		return OsyncConfig.getConfig("osync-cron-function-id", 4344000000151133l);
	}

	public static Long createOrUpdateCron(String osyncId, int intervalInMinutes) throws Exception {
		CronDetailsEntity cronDetails = accountRepo.findCronByOsyncId(osyncId);
		if (cronDetails == null) {
			Long cronId = createCron(osyncId, intervalInMinutes);
			cronDetails = new CronDetailsEntity();
			cronDetails.setOsyncId(osyncId);
			cronDetails.setCronJobId(cronId);
			accountRepo.createCron(cronDetails);

		} else {
			restartCron(cronDetails.getCronJobId(), intervalInMinutes);
		}
		return cronDetails.getCronJobId();
	}
}
