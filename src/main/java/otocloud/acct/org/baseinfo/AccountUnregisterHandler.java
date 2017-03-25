/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.org.baseinfo;

import otocloud.acct.org.dao.AccountDAO;
import otocloud.common.ActionURI;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudBusMessage;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;

/**
 * 
 * @author hugw
 *
 */
public class AccountUnregisterHandler extends OtoCloudEventHandlerImpl<JsonObject> {

	public static final String ACCOUNT_DELETE = "unregister";
	
	/**
	 * Constructor.
	 *
	 * @param componentImpl
	 */
	public AccountUnregisterHandler(OtoCloudComponentImpl componentImpl) {
		super(componentImpl);
	}


	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		
		componentImpl.getLogger().info(body.toString());
		
		//JsonObject params = body.getJsonObject("queryParams");		
		JsonObject sessionInfo = msg.getSession();
		
		Long accId = Long.parseLong(sessionInfo.getString("acct_id"));
			
		AccountDAO accountManagementDAO = new AccountDAO();
		accountManagementDAO.setDataSource(componentImpl.getSysDatasource());
		
		accountManagementDAO.unregisterAccount(accId, sessionInfo, daoRet -> {

			if (daoRet.failed()) {
				Throwable err = daoRet.cause();
				String errMsg = err.getMessage();
				componentImpl.getLogger().error(errMsg, err);	
				msg.fail(400, errMsg);
			} else {
				UpdateResult result = daoRet.result();		
				if (result.getUpdated() <= 0) {						
					String errMsg = "更新影响行数为0";
					componentImpl.getLogger().error(errMsg);									
					msg.fail(400, errMsg);
				} else {
					msg.reply("ok");
				}
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
		
		ActionURI uri = new ActionURI("", HttpMethod.DELETE);
		handlerDescriptor.setRestApiURI(uri);
		
		return handlerDescriptor;		

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEventAddress() {
		return ACCOUNT_DELETE;
	}

}
