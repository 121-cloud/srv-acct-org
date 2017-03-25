/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.org.bizunit.post;

import java.util.List;

import otocloud.common.ActionURI;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudBusMessage;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;
import otocloud.acct.org.dao.BizUnitPostDAO;
import otocloud.acct.org.dao.UserDAO;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;



public class BizUnitPostActivityCreateHandler extends OtoCloudEventHandlerImpl<JsonObject> {

	public static final String DEP_CREATE = "activity-create";

	/**
	 * Constructor.
	 *
	 * @param componentImpl
	 */
	public BizUnitPostActivityCreateHandler(OtoCloudComponentImpl componentImpl) {
		super(componentImpl);
	}

	/**
	{
		acct_id
		acct_biz_unit_post_id
		post_activity:[
			{				
				acct_app_activity_id	
				d_acct_app_id
				d_app_id	
				d_app_activity_id:
				d_app_activity_code:								
			}						
		]
	}
	*/
	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		
		componentImpl.getLogger().info(body.toString());
		
		JsonObject post = body.getJsonObject("content");
		Long post_id = post.getLong("acct_biz_unit_post_id");
		
		JsonObject sessionInfo = msg.getSession();
			
		BizUnitPostDAO bizUnitPostDAO = new BizUnitPostDAO(componentImpl.getSysDatasource());
		//departmentDAO.setDataSource(componentImpl.getSysDatasource());		
		
		bizUnitPostDAO.addActivity(post, sessionInfo, 
		daoRet -> {
			if (daoRet.failed()) {
				Throwable err = daoRet.cause();
				String errMsg = err.getMessage();
				componentImpl.getLogger().error(errMsg, err);	
				msg.fail(400, errMsg);
			} else {
				msg.reply("ok");
				
				//清理用户菜单
				UserDAO userDAO = new UserDAO(componentImpl.getSysDatasource());
				
			       Future<ResultSet> userListRet = Future.future();
			       
			       userListRet.setHandler(result -> {
			            if (result.succeeded()) {			            	
			            	ResultSet resultSet = result.result();
			            	List<JsonObject> retObjects = resultSet.getRows();
			            	if(retObjects != null && retObjects.size() > 0){
							  //通知删除用户的功能菜单缓存
								String portal_service = componentImpl.getDependencies().getJsonObject("portal_service").getString("service_name","");
								String address = portal_service + ".user-menu-del.delete";
			            		retObjects.forEach(item->{

										 JsonObject contentObject = new JsonObject().put("acct_id", item.getLong("acct_id").toString())
												 .put("user_id", item.getLong("auth_user_id").toString());
										 
										 JsonObject commandObject = new JsonObject().put("content", contentObject);
										
										componentImpl.getEventBus().send(address,
												commandObject, cleanUserMenuRet->{
													if(cleanUserMenuRet.succeeded()){
						
													}else{		
														Throwable err = cleanUserMenuRet.cause();						
														String errMsg = err.getMessage();
														componentImpl.getLogger().error(errMsg, err);								
														
													}	
													
										});			            			
			            			
			            		});			            		
			            	}
			            } else {
							Throwable err = result.cause();
							String errMsg = err.getMessage();
							componentImpl.getLogger().error(errMsg, err);
			            }
			        });
			        
			       userDAO.getUserListByPost(post_id, userListRet);				
				
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
