/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.org.bizunit.post;

import otocloud.common.ActionURI;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudBusMessage;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;
import otocloud.acct.org.dao.BizUnitPostDAO;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;


public class BizUnitPostModifyHandler extends OtoCloudEventHandlerImpl<JsonObject> {

	public static final String DEP_MODIFY = "modify";

	/**
	 * Constructor.
	 *
	 * @param componentImpl
	 */
	public BizUnitPostModifyHandler(OtoCloudComponentImpl componentImpl) {
		super(componentImpl);
	}

	/**
	{
		post_code
		post_name
	}
	*/
	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		
		componentImpl.getLogger().info(body.toString());
		
		JsonObject params = body.getJsonObject("queryParams");
		
		JsonObject post = body.getJsonObject("content");
		String post_code = post.getString("post_code");
		String post_name = post.getString("post_name");
		//JsonObject sessionInfo = msg.getSession();
			
		BizUnitPostDAO bizUnitPostDAO = new BizUnitPostDAO(componentImpl.getSysDatasource());
		//departmentDAO.setDataSource(componentImpl.getSysDatasource());		
		
		Long id = Long.parseLong(params.getString("id"));
		
		bizUnitPostDAO.modify(id, post_code, post_name, 
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
					//JsonArray ret = result.getKeys();
					//Integer id = ret.getInteger(0);
					//department.put("id", id);

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
		
		ActionURI uri = new ActionURI(":id", HttpMethod.PUT);
		handlerDescriptor.setRestApiURI(uri);
		
		return handlerDescriptor;		

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEventAddress() {
		return DEP_MODIFY;
	}

}
