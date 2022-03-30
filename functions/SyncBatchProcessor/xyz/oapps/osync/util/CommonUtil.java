package xyz.oapps.osync.util;

import java.util.logging.Logger;

import org.apache.commons.lang3.RandomStringUtils;

import xyz.oapps.osync.CurrentContext;
import xyz.oapps.osync.entity.AccountInfoEntity;
import xyz.oapps.osync.repo.AccountInfoRepository;
import xyz.oapps.osync.repo.IntegrationStatusRepository;

public class CommonUtil {
	
	private static Logger log = Logger.getLogger(CommonUtil.class.getName());
	
	static IntegrationStatusRepository intStatusRepo = new IntegrationStatusRepository();
	
	static AccountInfoRepository accountRepo = new AccountInfoRepository();
	
	public static final String ADMIN_EMAIL_ADDRESS = "admin@oapps.xyz";
	public static final String MAIL_API_URL = "https://mail.zoho.com/api/accounts/2287719000000008002/messages";
	
	public static void doOsyncFailureProcess(String osyncId, String integId, String failureMessage) throws Exception {
		AccountInfoEntity accountInfo = accountRepo.findByOsyncId(osyncId);
		if(accountInfo != null && accountInfo.getEmail() != null && !accountInfo.getEmail().isEmpty()) {
			ErrorEmailSender emailObj = new ErrorEmailSender(accountInfo.getEmail(),integId,accountInfo.getName(),"<strong style=\"color:red\">"+failureMessage+"</strong>",false);
			emailObj.sendEmail();
			
			ErrorEmailSender emailAdminObj = new ErrorEmailSender(accountInfo.getEmail(),integId,accountInfo.getName(),"<strong style=\"color:red\"> "+failureMessage+" </strong> for the user <strong>"+accountInfo.getName()+" ("+accountInfo.getEmail()+")</strong>",true);
			emailAdminObj.sendEmail();
		}
	}
	
	public static void logData(String generatedString, String from , String logkey , Object logValue) {
		log.info(generatedString+ " ::::" + from+ " :::: " +logkey+ " :::: " + logValue);
	}
	
	public static void logOsyncInfo(String loggingFrom , Object dataToBeLogged) {
		String uniqueLogId =CurrentContext.getCurrentContext().getUniqueLogId();
		if(uniqueLogId == null || uniqueLogId.isEmpty()) {
			uniqueLogId =  getRandomString();
			CurrentContext.createOrGetContext().setUniqueLogId(uniqueLogId);
		}
		
		log.info(uniqueLogId+ ":: [ " +loggingFrom +" ] :: "+ dataToBeLogged);
	}
	
	public static String getRandomString() {
		int length = 10;
	    boolean useLetters = true;
	    boolean useNumbers = false;
	    return RandomStringUtils.random(length, useLetters, useNumbers);
	}
}
