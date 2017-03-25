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
import otocloud.persistence.dao.TransactionConnection;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;

/**
 * 
 * @author hugw
 *
 */
public class AccountModifyHandler extends OtoCloudEventHandlerImpl<JsonObject> {

	public static final String ACCOUNT_MODIFIY = "modify";
	
	/**
	 * Constructor.
	 *
	 * @param componentImpl
	 */
	public AccountModifyHandler(OtoCloudComponentImpl componentImpl) {
		super(componentImpl);
	}

	/**
		{
			"acct_code": "lenovo"
			"acct_name":"联想",
			"industry_code":"3911",
			"ownership_code":"100",
			"area_code":"110108",
			"address":"上地7街38号",
			"contacts":联系人,
			"tel":"010-6956789",
			"email":"admin@lenovo.com",
			"website_url":"www.lenovo.com",
			"description":"世界五百强，PC业老大。",
		}
	 */
	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		
		componentImpl.getLogger().info(body.toString());
		
		//JsonObject params = body.getJsonObject("queryParams");
		
		JsonObject acctRegInfo = body.getJsonObject("content");
		JsonObject sessionInfo = msg.getSession();
		
		Long accId = Long.parseLong(sessionInfo.getString("acct_id"));
		
		//AccountDAO accountManagementDAO = new AccountDAO(componentImpl.getSysDatasource());
		
		JsonObject acct = new JsonObject();
		if(acctRegInfo.containsKey("acct_code")){
			acct.put("acct_code", acctRegInfo.getValue("acct_code"));
			acctRegInfo.remove("acct_code");
		}
		if(acctRegInfo.containsKey("acct_name")){
			acct.put("acct_name", acctRegInfo.getValue("acct_name"));
			acctRegInfo.remove("acct_name");
		}
		
		if(acctRegInfo.containsKey("parent_id")){
			acctRegInfo.remove("parent_id");
		}
		if(acctRegInfo.containsKey("acct_type")){
			acctRegInfo.remove("acct_type");
		}		
		if(acctRegInfo.containsKey("status")){
			acctRegInfo.remove("status");
		}
		
		JDBCClient jdbcClient = componentImpl.getSysDatasource().getSqlClient();
		jdbcClient.getConnection(conRes -> {
			if (conRes.succeeded()) {				
				SQLConnection conn = conRes.result();
				TransactionConnection.createTransactionConnection(conn, transConnRet->{
					if(transConnRet.succeeded()){
						TransactionConnection transConn = transConnRet.result();
						AccountDAO accountManagementDAO = new AccountDAO();						
						if(acct.size() > 0){						
							accountManagementDAO.modifyAccount(transConn, accId, acct, sessionInfo,  
							daoRet -> {	
								if (daoRet.failed()) {
									Throwable err = daoRet.cause();
									String errMsg = err.getMessage();
									componentImpl.getLogger().error(errMsg, err);									
									transConn.rollbackAndClose(closedRet->{												
										msg.fail(400, errMsg);
									});											
								} else {								
									if(acctRegInfo.size() > 0){						
										accountManagementDAO.modifyAccountInfo(transConn, accId, acctRegInfo, sessionInfo,   
										daoRet2 -> {	
											if (daoRet2.failed()) {
												Throwable err = daoRet2.cause();
												String errMsg = err.getMessage();
												componentImpl.getLogger().error(errMsg, err);									
												transConn.rollbackAndClose(closedRet->{												
													msg.fail(400, errMsg);
												});											
											} else {								
												UpdateResult result = daoRet2.result();												
												transConn.commitAndClose(closedRet->{
													msg.reply(result.toJson());												
												});		
																				
											}
										});
									}else{
										UpdateResult result = daoRet.result();
										transConn.commitAndClose(closedRet->{
											msg.reply(result.toJson());												
										});		
									}
								}
							});
						}else{
							if(acctRegInfo.size() > 0){						
								accountManagementDAO.modifyAccountInfo(transConn, accId, acctRegInfo, sessionInfo,   
								daoRet2 -> {	
									if (daoRet2.failed()) {
										Throwable err = daoRet2.cause();
										String errMsg = err.getMessage();
										componentImpl.getLogger().error(errMsg, err);									
										transConn.rollbackAndClose(closedRet->{												
											msg.fail(400, errMsg);
										});											
									} else {								
										UpdateResult result = daoRet2.result();
										transConn.commitAndClose(closedRet->{
											msg.reply(result.toJson());												
										});											
									}
								});
							}else{
								transConn.commitAndClose(closedRet->{
									msg.reply("数据无变化.");											
								});								
							}
						}
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
		
		ActionURI uri = new ActionURI("", HttpMethod.PUT);
		handlerDescriptor.setRestApiURI(uri);
		
		return handlerDescriptor;		

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEventAddress() {
		return ACCOUNT_MODIFIY;
	}

}
