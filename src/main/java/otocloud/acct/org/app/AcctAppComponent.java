/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.org.app;


import java.util.ArrayList;
import java.util.List;

import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerRegistry;

public class AcctAppComponent extends OtoCloudComponentImpl {
	
	@Override
	public String getName() {
		return "my-app-subscribe";
	}

	@Override
	public List<OtoCloudEventHandlerRegistry> registerEventHandlers() {
		
		List<OtoCloudEventHandlerRegistry> ret = new ArrayList<OtoCloudEventHandlerRegistry>();
		
		AppSubscribeHandler appSubscribeHandler = new AppSubscribeHandler(this);
		ret.add(appSubscribeHandler);
		
		AppUnSubscribeHandler appUnSubscribeHandler = new AppUnSubscribeHandler(this);
		ret.add(appUnSubscribeHandler);

		AppSubscribeQueryHandler appSubscribeQueryHandler = new AppSubscribeQueryHandler(this);
		ret.add(appSubscribeQueryHandler);
		
		AppPermissionVerficationHandler appPermVerficationHandler = new AppPermissionVerficationHandler(this);
		ret.add(appPermVerficationHandler);
		
		ActivityListGetHandler activityListGetHandler = new ActivityListGetHandler(this);
		ret.add(activityListGetHandler);
		
		NewAppListQueryHandler newAppListQueryHandler = new NewAppListQueryHandler(this);
		ret.add(newAppListQueryHandler);

		
		return ret;
	}
	
	
}