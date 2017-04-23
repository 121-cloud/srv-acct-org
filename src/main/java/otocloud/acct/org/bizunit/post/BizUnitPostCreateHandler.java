/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.org.bizunit.post;

import otocloud.common.ActionURI;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.CommandMessage;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;
import otocloud.acct.org.dao.BizUnitPostDAO;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;


public class BizUnitPostCreateHandler extends OtoCloudEventHandlerImpl<JsonObject> {

	public static final String DEP_CREATE = "create";

	/**
	 * Constructor.
	 *
	 * @param componentImpl
	 */
	public BizUnitPostCreateHandler(OtoCloudComponentImpl componentImpl) {
		super(componentImpl);
	}

	/**
	{
		post_code
		post_name
		d_org_role_id
		acct_biz_unit_id
		auth_role_id
		acct_id
		post_activity:[
			{			
				acct_app_activity_id	
				d_app_id	
				d_acct_app_id
				d_app_activity_id:
				d_app_activity_code:								
			}						
		]
	}
	*/
	@Override
	public void handle(CommandMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		
		componentImpl.getLogger().info(body.toString());
		
		JsonObject post = body.getJsonObject("content");
		JsonObject sessionInfo = msg.getSession();
			
		BizUnitPostDAO bizUnitPostDAO = new BizUnitPostDAO(componentImpl.getSysDatasource());
		//departmentDAO.setDataSource(componentImpl.getSysDatasource());		
		
		bizUnitPostDAO.create(post, sessionInfo, 
		daoRet -> {

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
					JsonArray ret = result.getKeys();
					Integer id = ret.getInteger(0);
					post.put("id", id);

					msg.reply(post);

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
		
		ActionURI uri = new ActionURI(DEP_CREATE, HttpMethod.POST);
		handlerDescriptor.setRestApiURI(uri);
		
		return handlerDescriptor;		

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEventAddress() {
		return DEP_CREATE;
	}

}
