/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.org.app;


import java.util.Map;

import otocloud.common.ActionURI;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudBusMessage;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;
import otocloud.persistence.dao.TransactionConnection;
import otocloud.acct.org.dao.AppSubscribeDAO;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;



public class AppSubscribeHandler extends OtoCloudEventHandlerImpl<JsonObject> {

	public static final String DEP_CREATE = "create";

	/**
	 * Constructor.
	 *
	 * @param componentImpl
	 */
	public AppSubscribeHandler(OtoCloudComponentImpl componentImpl) {
		super(componentImpl);
	}

	/**
	{
		"acct_id":
		"d_app_id":
		"app_version_id":
		"app_inst_group": 应用实例分组
		"activities":[
			{
				"app_activity_id":
			}
		],
		"biz_units":[
			{
				"org_role_id": 组织商业角色
				"unit_code":
				"unit_name":
				"acct_biz_unit_post":[
					{
						auth_role_id：用户角色
						post_code:
						post_name:
						acct_biz_unit_post_activity:[
							{
								d_app_activity_id:
								d_app_activity_code:							
							}						
						]					
					
					}				
				]
			}
		]
	}
	*/
	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		
		componentImpl.getLogger().info(body.toString());
		
		JsonObject subscribeInfo = body.getJsonObject("content");
		JsonObject sessionInfo = msg.getSession();
		
		Long acctId = subscribeInfo.getLong("acct_id");
		Long appId = subscribeInfo.getLong("d_app_id");
		Long appVerId = subscribeInfo.getLong("app_version_id");
		String appInst = subscribeInfo.getString("app_inst_group");
		
		Long userId = Long.parseLong(sessionInfo.getString("user_id"));
		
		JDBCClient jdbcClient = componentImpl.getSysDatasource().getSqlClient();
		jdbcClient.getConnection(conRes -> {
			if (conRes.succeeded()) {				
				SQLConnection conn = conRes.result();
				TransactionConnection.createTransactionConnection(conn, transConnRet->{
					if(transConnRet.succeeded()){
						TransactionConnection transConn = transConnRet.result();
						AppSubscribeDAO appSubscribeDAO = new AppSubscribeDAO(componentImpl.getSysDatasource());
						//订购应用
						appSubscribeDAO.subscribeApp(transConn, subscribeInfo, sessionInfo, appSubscribeRet->{
							if(appSubscribeRet.succeeded()){	
								Map<Long, Long> activityMap = appSubscribeRet.result();
								transConn.commitAndClose(closedRet->{
									
									JsonArray biz_units = subscribeInfo.getJsonArray("biz_units");
									if(biz_units == null || biz_units.size() == 0){
										msg.reply(subscribeInfo);
									}else{
										//添加业务单元和相关业务角色
										appSubscribeDAO.addBizUnitAndPos(acctId, userId, appId, activityMap, biz_units, bizUnitRet->{
											if(bizUnitRet.succeeded()){
												
												//应用引擎加载账户应用实例												
												String rfbSrvAddress = appInst + ".platform.app_inst.load"; 
													
												JsonObject instLoadMsg = new JsonObject()
													.put("acct_id", acctId)
													.put("app_version_id", appVerId);

												componentImpl.getEventBus().send(rfbSrvAddress,
														instLoadMsg, createInstRet->{
															if(createInstRet.succeeded()){
																							
																msg.reply(subscribeInfo);
															}else{		
																Throwable err = createInstRet.cause();	
																String errMsg = err.getMessage();
																componentImpl.getLogger().error(errMsg,err);																	
																msg.fail(400, errMsg);
															}	
															
												});	
												
											}else{
												Throwable err = bizUnitRet.cause();
												String errMsg = err.getMessage();
												componentImpl.getLogger().error(errMsg, err);
												msg.fail(400, errMsg);
											}											
										});
									}
									
								});	
							}else{
								Throwable err = appSubscribeRet.cause();
								String errMsg = err.getMessage();
								componentImpl.getLogger().error(errMsg, err);									

								transConn.rollbackAndClose(closedRet->{												
									msg.fail(400, errMsg);
								});	
							}							
						});						
					}else{
						Throwable err = transConnRet.cause();
						String errMsg = err.getMessage();
						componentImpl.getLogger().error(errMsg, err);	
						conn.close(closedRet->{
							msg.fail(400, errMsg);
						});			
					}
				});
			}else{
				Throwable err = conRes.cause();
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
		
		ActionURI uri = new ActionURI("", HttpMethod.POST);
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
