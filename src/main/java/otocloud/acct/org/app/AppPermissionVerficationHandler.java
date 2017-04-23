/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.org.app;

import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import otocloud.acct.org.dao.AppSubscribeDAO;
import otocloud.common.ActionURI;
import otocloud.framework.core.CommandMessage;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;


public class AppPermissionVerficationHandler extends OtoCloudEventHandlerImpl<JsonObject> {

	public static final String ADDRESS = "app-permission-verfication";

	/**
	 * Constructor.
	 *
	 * @param componentImpl
	 */
	public AppPermissionVerficationHandler(OtoCloudComponentImpl componentImpl) {
		super(componentImpl);
	}

	/**
	{
		"acct_id":
		"app_id":
	}
	*/
	@Override
	public void handle(CommandMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		
		componentImpl.getLogger().info(body.toString());
		
		JsonObject subscribeInfo = body.getJsonObject("content");
		
		Long acctId = subscribeInfo.getLong("acct_id");
		Long appId = subscribeInfo.getLong("app_id");
		
		Future<Boolean> getFuture = Future.future();
		
		AppSubscribeDAO appSubscribeDAO = new AppSubscribeDAO(componentImpl.getSysDatasource());	
		
		appSubscribeDAO.permissionVerify(acctId, appId, getFuture);
		
		getFuture.setHandler(ret-> {
			if (ret.failed()) {
				Throwable err = ret.cause();
				String errMsg = err.getMessage();
				componentImpl.getLogger().error(errMsg, err);	
				msg.fail(400, errMsg);
			} else {
				Boolean isOk = ret.result();	
				msg.reply(new JsonObject().put("result", isOk));
			}
		});


	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public HandlerDescriptor getHanlderDesc() {		
		
		HandlerDescriptor handlerDescriptor = super.getHanlderDesc();
		
		//参数
/*		List<ApiParameterDescriptor> paramsDesc = new ArrayList<ApiParameterDescriptor>();
		paramsDesc.add(new ApiParameterDescriptor("targetacc",""));		
		paramsDesc.add(new ApiParameterDescriptor("soid",""));		
		handlerDescriptor.setParamsDesc(paramsDesc);	*/
		
		ActionURI uri = new ActionURI(ADDRESS, HttpMethod.POST);
		handlerDescriptor.setRestApiURI(uri);
		
		return handlerDescriptor;		

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEventAddress() {
		return ADDRESS;
	}

}
