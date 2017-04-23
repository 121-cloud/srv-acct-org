/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.org.bizunit.post;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import otocloud.acct.org.dao.BizUnitPostDAO;
import otocloud.common.ActionURI;
import otocloud.framework.core.CommandMessage;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;
import otocloud.persistence.dao.TransactionConnection;



public class BizUnitPostDeleteHandler extends OtoCloudEventHandlerImpl<JsonObject> {

	public static final String DEP_DELETE = "delete";

	/**
	 * Constructor.
	 *
	 * @param componentImpl
	 */
	public BizUnitPostDeleteHandler(OtoCloudComponentImpl componentImpl) {
		super(componentImpl);
	}

	/**
	{
		"id":
	}
	 */
	@Override
	public void handle(CommandMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		
		componentImpl.getLogger().info(body.toString());
		
		JsonObject params = body.getJsonObject("queryParams");
		
		Long id = Long.parseLong(params.getString("id"));			
			
		
		JDBCClient jdbcClient = componentImpl.getSysDatasource().getSqlClient();
		jdbcClient.getConnection(conRes -> {
			if (conRes.succeeded()) {				
				SQLConnection conn = conRes.result();
				TransactionConnection.createTransactionConnection(conn, transConnRet->{
					if(transConnRet.succeeded()){
						TransactionConnection transConn = transConnRet.result();
						BizUnitPostDAO bizUnitPostDAO = new BizUnitPostDAO(componentImpl.getSysDatasource());	
						
						bizUnitPostDAO.delete(transConn, id, appSubscribeRet->{
							if(appSubscribeRet.succeeded()){	
								//Map<Long, Long> activityMap = appSubscribeRet.result();
								transConn.commitAndClose(closedRet->{
									
									msg.reply(appSubscribeRet.result().toJson());
																		
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
		
		ActionURI uri = new ActionURI(":id", HttpMethod.DELETE);
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
