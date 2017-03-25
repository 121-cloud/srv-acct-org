/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.org.bizunit.post;

import java.util.List;

import otocloud.acct.org.dao.BizUnitPostDAO;
import otocloud.acct.org.dao.UserDAO;
import otocloud.common.ActionURI;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudBusMessage;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;




public class BizUnitPostActivityDeleteHandler extends OtoCloudEventHandlerImpl<JsonObject> {

	public static final String DEP_DELETE = "delete-activity";

	/**
	 * Constructor.
	 *
	 * @param componentImpl
	 */
	public BizUnitPostActivityDeleteHandler(OtoCloudComponentImpl componentImpl) {
		super(componentImpl);
	}

	/**
	{
		"post_id":
		"post_activity_id":
	}
	 */
	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
		JsonObject body = msg.body();
        JsonObject post = body.getJsonObject("content");        
        Long post_activity_id = post.getLong("post_activity_id");
        Long post_id = post.getLong("post_id");
		
		BizUnitPostDAO bizUnitPostDAO = new BizUnitPostDAO(componentImpl.getSysDatasource());	
		
		bizUnitPostDAO.deleteActivity(post_activity_id, appSubscribeRet->{
			if(appSubscribeRet.succeeded()){	
				msg.reply(appSubscribeRet.result().toJson());
				
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
				
			}else{
				Throwable err = appSubscribeRet.cause();
				String errMsg = err.getMessage();
				componentImpl.getLogger().error(errMsg, err);
				
				msg.fail(400, errMsg);			
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
		
		ActionURI uri = new ActionURI(DEP_DELETE, HttpMethod.POST);
		handlerDescriptor.setRestApiURI(uri);
		
		return handlerDescriptor;		

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEventAddress() {
		return DEP_DELETE;
	}

}
