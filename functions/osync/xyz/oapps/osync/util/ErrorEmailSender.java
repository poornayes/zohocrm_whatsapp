package xyz.oapps.osync.util;

import org.json.JSONObject;

import xyz.oapps.osync.entity.IntegrationPropsEntity;
import xyz.oapps.osync.entity.ModuleInfoEntity;
import xyz.oapps.osync.entity.ServiceInfoEntity;
import xyz.oapps.osync.invoker.Invoker;
import xyz.oapps.osync.repo.IntegrationPropsRepository;
import xyz.oapps.osync.repo.ModuleInfoRepository;
import xyz.oapps.osync.repo.ServiceInfoRepository;

public class ErrorEmailSender {

	ServiceInfoRepository serviceInfoRepo = new ServiceInfoRepository();

	IntegrationPropsRepository intPropsRepo = new IntegrationPropsRepository();

	ModuleInfoRepository moduleMapRepo = new ModuleInfoRepository();


	String toEmailAddress;
	String integId;
	String osyncId;
	String leftServiceName;
	String leftModuleName;
	String rightServiceName;
	String rightModuleName;
	String contactName;
	String rootCauseDescription;
	boolean isOsyncAdmin;

	public ErrorEmailSender(String toEmailAddress, String integId , String contactName,String rootCauseDescription, boolean isAdmin) throws Exception {
		super();
		
		this.toEmailAddress = toEmailAddress;
		IntegrationPropsEntity findById = intPropsRepo.findById(integId);

		ServiceInfoEntity leftServiceInfo = serviceInfoRepo.findByServiceId(findById.getLeftServiceId());
		ServiceInfoEntity rightServiceInfo = serviceInfoRepo.findByServiceId(findById.getRightServiceId());

		ModuleInfoEntity leftModuleInfo = moduleMapRepo.findByModuleId(findById.getLeftModuleId());
		ModuleInfoEntity rightModuleInfo = moduleMapRepo.findByModuleId(findById.getRightModuleId());

		this.leftServiceName = leftServiceInfo.getName();
		this.leftModuleName = leftModuleInfo.getName();
		this.rightServiceName = rightServiceInfo.getName();
		this.rightModuleName = rightModuleInfo.getName();
		this.contactName = contactName;
		this.rootCauseDescription = rootCauseDescription;
		this.isOsyncAdmin = isAdmin;
		this.integId = integId;
		this.osyncId = findById.getOsyncId();
	}

	public boolean sendEmail() {

		try {
			Invoker invokerObj = new Invoker("700fb249-e9ca-42b9-838a-0144c09a0af0", false, true, true,
					"admin_to_send_email");

			JSONObject payloadJson = new JSONObject();
			payloadJson.put("fromAddress", "help@oapps.xyz");
			payloadJson.put("toAddress", this.isOsyncAdmin ? "admin@oapps.xyz" : this.toEmailAddress);
			payloadJson.put("subject",  this.isOsyncAdmin ? "Attention Required for "+this.contactName : "Attention Required!");
			payloadJson.put("mailFormat", "html");
			payloadJson.put("content", getMessageContent());

			String post = invokerObj.post(CommonUtil.MAIL_API_URL, null,null, payloadJson);
			return (post != null && !post.isEmpty());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}


	private String getMessageContent()
	{

		String headerContent = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" + 
				"\n" + 
				"    </head>\n" + 
				"    \n" + 
				"    <body bgcolor=\"#f5f5f5\" style=\"background-color:#f5f5f5; margin:0; padding:0;-webkit-font-smoothing: antialiased;width:100% !important;-webkit-text-size-adjust:none;\" topmargin=\"0\">\n" + 
				"\n" + 
				"    <table width=\"100%\" bgcolor=\"#f5f5f5\" style=\"background-color:#f5f5f5;padding-top: 20px;\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" + 
				"      <tbody><tr>\n" + 
				"        <td>&nbsp;</td>\n" + 
				"    \n" + 
				"        <td width=\"650\" align=\"center\" style=\"border-left:1px solid #d8d8d8;border-right:1px solid #d8d8d8;border-bottom:1px solid #d8d8d8;border-top:1px solid #d8d8d8;background-color: #ffffff;padding-bottom: 30px;box-shadow: 0 .5rem 1rem rgba(0,0,0,.15)!important;\">\n" + 
				"          \n" + 
				"          <table align=\"center\" width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" + 
				"            <tbody><tr>  \n" + 
				"               <td height=\"24\" style=\"font-size: 0px; line-height: 0px;\">&nbsp;</td>\n" + 
				"            </tr>\n" + 
				"          </tbody>\n" + 
				"        </table>";

		String customerContent = "<table align=\"center\" width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" + 
				"             <tbody><tr>      \n" + 
				"              <td width=\"67\" style=\"padding-top:18px;\">&nbsp;</td>\n" + 
				"              <td style=\"color:#333333;font-family: Helvetica, sans-serif;text-align:left;font-size:14px;line-height:20px;padding-top:18px;padding-bottom:18px;\">\n" + 
				"                <strong>Hi "+this.contactName+",</strong><br><br>\n" + 
				"    <strong>Greetings from OApps!</strong> <br><br>Sync between "+this.leftServiceName+" ("+this.leftModuleName+") and "+this.rightServiceName+" ("+this.rightModuleName+") has been stopped due to "+this.rootCauseDescription+".<br><br>";

		if(this.isOsyncAdmin) {
			customerContent += "<div>\n" + 
					"    <div>\n" + 
					"        <span> <strong> Osync Id </strong> - </span>\n" + 
					"        <span> "+this.osyncId+" </span>\n" + 
					"    </div>\n" + 
					"    <div>\n" + 
					"        <span> <strong> Integ Id </strong> - </span>\n" + 
					"        <span> "+this.integId+" </span>\n" + 
					"    </div>\n" + 
					"</div><br><br>";
		}

		customerContent+="We request your immediate attention on this.\n" + 
				"    \n" + 
				"    <br><br>\n" + 
				"    Thanks, \n" + 
				"    <br> <strong> OApps Team. </strong>\n" + 
				"    </td>          \n" + 
				"             <td width=\"34\" style=\"padding-top:18px;\">&nbsp;</td>\n" + 
				"             <td style=\"\n" + 
				"        vertical-align: text-top;\n" + 
				"        padding-top: 20px;\n" + 
				"    \"><img alt=\"Image4\" src=\"https://osync-advancedio-701822429.development.zohocatalyst.com/app/img/favicon.png\" width=\"\" height=\"50\" border=\"0\" hspace=\"0\" vspace=\"0\" style=\"display:block;vertical-align:top;\"></td>\n" + 
				"             <td width=\"67\" style=\"padding-top:18px;\">&nbsp;</td>\n" + 
				"            </tr>\n" + 
				"             </tbody></table>  ";



		String remainingContent = "<table class=\"medium_width\" align=\"center\" width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" + 
				"            <tbody><tr>      \n" + 
				"              <td class=\"desktop\" width=\"67\" style=\"padding-top:12px;\">&nbsp;</td> \n" + 
				"              <td align=\"right\" valign=\"top\" style=\"border-top:1px solid #d9d9d9; padding-top:12px;\">\n" + 
				"                      <table width=\"200\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" + 
				"                     <tbody><tr>\n" + 
				"                        <td style=\"color:#333333; font-family: Helvetica, sans-serif;text-align:left; font-size:13px; line-height:15px;\">Social Media Links</td>\n" + 
				"                        <td valign=\"top\" style=\"color:#006699; font-family: Helvetica, sans-serif;text-align:left; font-size:12px; line-height:16px;\">\n" + 
				"                            <table width=\"66\" align=\"right\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" + 
				"                               <tbody><tr>\n" + 
				"                                  <td width=\"24\"><a href=\"https://www.facebook.com/OAppSxyz-542419402917448/\" target=\"_blank\" class=\"theme-facebook-container\" style=\"height: 20px;\"><svg viewBox=\"0 0 53 97\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" style=\"width: 15;height: 15;\"><path d=\"M50.089,0.02 L37.624,0 C23.62,0 14.57,9.285 14.57,23.656 L14.57,34.563 L2.037,34.563 C0.954,34.563 0.077,35.441 0.077,36.524 L0.077,52.327 C0.077,53.41 0.955,54.287 2.037,54.287 L14.57,54.287 L14.57,94.163 C14.57,95.246 15.447,96.123 16.53,96.123 L32.882,96.123 C33.965,96.123 34.842,95.245 34.842,94.163 L34.842,54.287 L49.496,54.287 C50.579,54.287 51.456,53.41 51.456,52.327 L51.462,36.524 C51.462,36.004 51.255,35.506 50.888,35.138 C50.521,34.77 50.021,34.563 49.501,34.563 L34.842,34.563 L34.842,25.317 C34.842,20.873 35.901,18.617 41.69,18.617 L50.087,18.614 C51.169,18.614 52.046,17.736 52.046,16.654 L52.046,1.98 C52.046,0.899 51.17,0.022 50.089,0.02 L50.089,0.02 Z\"></path></svg></a></td>\n" + 
				"                                  <td width=\"24\"><a href=\"https://twitter.com/OAppS4\" target=\"_blank\" class=\"theme-twitter-container\"><svg viewBox=\"0 0 612 498\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" style=\"width: 15;height: 15;\"><path d=\"M612,59.258 C589.475,69.239 565.306,76.008 539.912,79.03 C565.841,63.503 585.689,38.875 595.096,9.619 C570.774,23.998 543.927,34.439 515.321,40.099 C492.414,15.662 459.831,0.441 423.691,0.441 C354.357,0.441 298.14,56.658 298.14,125.954 C298.14,135.782 299.249,145.381 301.391,154.56 C197.065,149.32 104.556,99.337 42.641,23.386 C31.818,41.896 25.661,63.464 25.661,86.487 C25.661,130.046 47.842,168.48 81.496,190.966 C60.921,190.278 41.57,184.618 24.629,175.21 L24.629,176.778 C24.629,237.584 67.92,288.332 125.322,299.882 C114.805,302.712 103.715,304.28 92.242,304.28 C84.135,304.28 76.295,303.477 68.608,301.947 C84.593,351.854 130.944,388.146 185.861,389.141 C142.914,422.795 88.762,442.796 29.945,442.796 C19.811,442.796 9.829,442.184 0.001,441.075 C55.568,476.756 121.537,497.56 192.439,497.56 C423.387,497.56 549.627,306.269 549.627,140.372 L549.206,124.119 C573.872,106.526 595.211,84.422 612,59.258 L612,59.258 Z\"></path></svg></a></td>\n" + 
				"                                  <td width=\"16\"><a href=\"https://www.instagram.com/oappstechnologies/\" target=\"_blank\" class=\"theme-instagram-container\"><svg viewBox=\"0 0 1792 1792\" xmlns=\"http://www.w3.org/2000/svg\" style=\"width: 15;height: 15;\"><path d=\"M1490 1426v-648h-135q20 63 20 131 0 126-64 232.5t-174 168.5-240 62q-197 0-337-135.5t-140-327.5q0-68 20-131h-141v648q0 26 17.5 43.5t43.5 17.5h1069q25 0 43-17.5t18-43.5zm-284-533q0-124-90.5-211.5t-218.5-87.5q-127 0-217.5 87.5t-90.5 211.5 90.5 211.5 217.5 87.5q128 0 218.5-87.5t90.5-211.5zm284-360v-165q0-28-20-48.5t-49-20.5h-174q-29 0-49 20.5t-20 48.5v165q0 29 20 49t49 20h174q29 0 49-20t20-49zm174-208v1142q0 81-58 139t-139 58h-1142q-81 0-139-58t-58-139v-1142q0-81 58-139t139-58h1142q81 0 139 58t58 139z\"></path></svg></a></td>\n" + 
				"                               </tr>\n" + 
				"                            </tbody></table>\n" + 
				"                       </td>\n" + 
				"                     </tr>\n" + 
				"                  </tbody></table>\n" + 
				"              </td>\n" + 
				"              <td class=\"desktop\" width=\"67\" style=\"padding-top:12px;\">&nbsp;</td>             \n" + 
				"            </tr>\n" + 
				"          </tbody></table>\n" + 
				"          \n" + 
				"        </td>\n" + 
				"        <td>&nbsp;</td>\n" + 
				"      </tr>\n" + 
				"    </tbody>\n" + 
				"</table>\n" + 
				"    \n" + 
				"</body>\n" + 
				"</html>";


		return headerContent+customerContent+remainingContent;
	}
}
