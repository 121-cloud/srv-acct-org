/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.org.bizunit.user;


import java.util.ArrayList;
import java.util.List;

import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerRegistry;

public class UserComponent extends OtoCloudComponentImpl {
	
	@Override
	public String getName() {
		return "my-user";
	}

	@Override
	public List<OtoCloudEventHandlerRegistry> registerEventHandlers() {
		
		List<OtoCloudEventHandlerRegistry> ret = new ArrayList<OtoCloudEventHandlerRegistry>();
		
		UserCreationHandler userCreationHandler = new UserCreationHandler(this);
		ret.add(userCreationHandler);
		
		UserDeleteHandler userDeleteHandler = new UserDeleteHandler(this);
		ret.add(userDeleteHandler);
		
		UserQueryHandler userQueryHandler = new UserQueryHandler(this);
		ret.add(userQueryHandler);
		
		UserUpdateHandler userUpdateHandler = new UserUpdateHandler(this);
		ret.add(userUpdateHandler);
		
		UserPostQueryHandler userPostQueryHandler = new UserPostQueryHandler(this);
		ret.add(userPostQueryHandler);
		
		PartTimeUserCreationHandler partTimeUserCreationHandler = new PartTimeUserCreationHandler(this);
		ret.add(partTimeUserCreationHandler);
		
		UserJoinToAcctHandler userJoinToAcctHandler = new UserJoinToAcctHandler(this);
		ret.add(userJoinToAcctHandler);
		
		ExistUserInAcctHandler existUserInAcctHandler = new ExistUserInAcctHandler(this);
		ret.add(existUserInAcctHandler);
		
		return ret;
	}
	
	
}