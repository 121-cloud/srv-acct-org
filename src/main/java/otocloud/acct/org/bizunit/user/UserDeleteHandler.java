package otocloud.acct.org.bizunit.user;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.sql.SQLConnection;
import otocloud.acct.org.AccountOrgService;
import otocloud.acct.org.dao.UserDAO;
import otocloud.common.ActionURI;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudBusMessage;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;
import otocloud.persistence.dao.TransactionConnection;

/**
 * Created by zhangye on 2015-10-27.
 */
public class UserDeleteHandler extends OtoCloudEventHandlerImpl<JsonObject> {
	
	public static final String ADDRESS = "delete";

    public UserDeleteHandler(OtoCloudComponentImpl componentImpl) {
        super(componentImpl);
    }

    /* 
     * { 
     * 	  acct_id:
     * 	  acct_biz_unit_post_id: 
     * 	  auto_delete_acct_user: true //如果用户无岗位了，则是否自动将此用户从租户中删除
     * }
     * 
     */
    @Override
    public void handle(OtoCloudBusMessage<JsonObject> msg) {
    	
    	JsonObject body = msg.body();
    	
		JsonObject params = body.getJsonObject("queryParams");	
		
        JsonObject content = body.getJsonObject("content");    	

    	Boolean auto_delete_acct_user = content.getBoolean("auto_delete_acct_user");
    	Long acct_biz_unit_post_id = content.getLong("acct_biz_unit_post_id");
    	Long acct_id = content.getLong("acct_id");
		
		Long userId = Long.parseLong(params.getString("id"));

		JDBCClient jdbcClient = componentImpl.getSysDatasource().getSqlClient();
		jdbcClient.getConnection(conRes -> {
			if (conRes.succeeded()) {				
				SQLConnection conn = conRes.result();
				TransactionConnection.createTransactionConnection(conn, transConnRet->{
					if(transConnRet.succeeded()){
						TransactionConnection transConn = transConnRet.result();
						UserDAO userDAO = new UserDAO(componentImpl.getSysDatasource());	
						
						userDAO.delete(transConn, userId, acct_biz_unit_post_id, acct_id, auto_delete_acct_user, appSubscribeRet->{
							if(appSubscribeRet.succeeded()){	
								//Map<Long, Long> activityMap = appSubscribeRet.result();
								transConn.commitAndClose(closedRet->{
									JsonObject retJsonObject = appSubscribeRet.result();
									Boolean need_clean_user = retJsonObject.getBoolean("need_clean_user", false);
									JsonObject retObj = retJsonObject.getJsonObject("result");
									
									msg.reply(retObj);
									
									if(need_clean_user){
									
										AccountOrgService accountOrgService = (AccountOrgService)this.componentImpl.getService();
										MongoClient authMongoClient = accountOrgService.getAuthSrvMongoDataSource().getMongoClient();
										
										  //删除用户激活数据
										  if(authMongoClient != null){
											  JsonObject query = new JsonObject().put("acct_id", acct_id)
													  .put("user_id", userId);
											  authMongoClient.removeDocument("UsersActivation", query, userActResultHandler-> {
					                                if(userActResultHandler.failed()){                                    
					                    				Throwable errThrowable = userActResultHandler.cause();
					                    				errThrowable.printStackTrace();					                    				
					                                }
					                          });
											  
										  }
										  
										  //通知删除用户的功能菜单缓存
											String portal_service = componentImpl.getDependencies().getJsonObject("portal_service").getString("service_name","");
											String address = portal_service + ".user-menu-del.delete";
											 JsonObject contentObject = new JsonObject().put("acct_id", acct_id.toString())
													 .put("user_id", userId.toString());
											 
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
     * 最终的REST API是：put /api/"服务名"/"组件名"/users/:openId
     *
     * @return
     */
    @Override
    public HandlerDescriptor getHanlderDesc() {
        HandlerDescriptor handlerDescriptor = super.getHanlderDesc();
        ActionURI uri = new ActionURI(ADDRESS + "/:id", HttpMethod.DELETE);
        handlerDescriptor.setRestApiURI(uri);
        return handlerDescriptor;
    }

    /**
     * 事件总线上注册的最终地址是："服务名"."组件名".users.delete
     * @return
     */
    @Override
    public String getEventAddress() {
        return ADDRESS;
    }
}
