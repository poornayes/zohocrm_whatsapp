package xyz.oapps.osync;

import xyz.oapps.osync.entity.ModuleInfoEntity;
import xyz.oapps.osync.entity.ServiceInfoEntity;
import xyz.oapps.osync.handler.freshworks.freshsales.FreshSalesHandler;
import xyz.oapps.osync.handler.salesLoft.SalesLoftHandler;
import xyz.oapps.osync.handler.zoho.crm.ZohoCRMHandler;
import xyz.oapps.osync.handler.outreach.OutreachHandler;
import xyz.oapps.osync.handler.monday.MondayHandler;

public class SyncHandlerRepo {

	public static SyncHandler getInstance(ServiceInfoEntity service, ModuleInfoEntity module, String osyncId, String integId, boolean isLeft) {
		SyncHandler syncHandler = null;
		switch (service.getName().toLowerCase()) {
		case "zohocrm":
			syncHandler = new ZohoCRMHandler();
			break;
		case "freshsales":
			syncHandler = new FreshSalesHandler();
			break;
		case "monday":
			syncHandler = new MondayHandler();
			break;
		case "salesloft":
			syncHandler = new SalesLoftHandler();
			break;
		case "outreach":
			syncHandler = new OutreachHandler();
		}
		syncHandler.setService(service);
		syncHandler.setModule(module);
		syncHandler.setIntegId(integId);
		syncHandler.osyncId = osyncId;
		syncHandler.setLeft(isLeft);
		
		
		
		return syncHandler;

	}

}