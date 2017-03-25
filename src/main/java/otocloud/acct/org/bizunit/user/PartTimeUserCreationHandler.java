package otocloud.acct.org.bizunit.user;


import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;
import otocloud.acct.org.dao.UserDAO;
import otocloud.common.ActionURI;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudBusMessage;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;



public class PartTimeUserCreationHandler extends OtoCloudEventHandlerImpl<JsonObject> {
	
	public static final String ADDRESS = "create-parttimer";
	
    public static final String USERS_ACTIVATION = "UsersActivation";
    
    
    public PartTimeUserCreationHandler(OtoCloudComponentImpl component) {
        super(component);
    }
    
    
    /* 
     * {
     * 	  acct_id: 租户ID
     * 	  biz_unit_id: 业务单元ID
     * 	  post_id: 岗位ID
     * 	  auth_role_id: 对应的角色 规格
     * 	  auth_user_id: 用户ID
     * }
     * 
     */
    @Override
    public void handle(OtoCloudBusMessage<JsonObject> msg) {
 
        JsonObject body = msg.body();
       
        JsonObject content = body.getJsonObject("content");
        
        Long acctId = content.getLong("acct_id");
        Long bizUnitId = content.getLong("biz_unit_id");
        Long postId = content.getLong("post_id");
        Long authRoleId = content.getLong("auth_role_id", 0L);
        Long auth_user_id = content.getLong("auth_user_id");
        
        JsonObject session = msg.getSession();
        Long userId = Long.parseLong(session.getString("user_id"));
 
        Future<UpdateResult> createUserfuture = Future.future();
        UserDAO userDAO = new UserDAO(this.componentImpl.getSysDatasource());
        userDAO.addAcctPostForPartTimer(acctId, bizUnitId, postId, authRoleId, auth_user_id, userId, createUserfuture);
        createUserfuture.setHandler(userResult -> {
            if (userResult.succeeded()) {
               
                try{                        
                	msg.reply(userResult.result().toJson());
                	
/*                    //生成激活码
                    String activateCode = generateActivationCode(auth_user_id, acctId);
                    JsonObject activateInfo = new JsonObject();
                    activateInfo.put("acct_id", acctId);
                    activateInfo.put("user_id", auth_user_id);
                    activateInfo.put("activation_code", activateCode);

                    //将一次性激活码存入数据库
                    AccountOrgService authService = (AccountOrgService)this.componentImpl.getService();
                    authService.getAuthSrvMongoDataSource().getMongoClient().insert(USERS_ACTIVATION, activateInfo, insertRet -> {
                        if(insertRet.failed()){                                    
            				Throwable errThrowable = insertRet.cause();
            				String errMsgString = errThrowable.getMessage();
            				this.componentImpl.getLogger().error("无法将用户激活码保存到 Mongo 数据库中." + errMsgString, errThrowable);
            				msg.fail(100, errMsgString);
                        }
                    });*/
                	
                }catch (Exception e) {
        			String errMsgString = e.getMessage();
        			this.componentImpl.getLogger().error(errMsgString, e);
        			msg.fail(100, errMsgString);		
                }
            } else {
				Throwable errThrowable = userResult.cause();
				String errMsgString = errThrowable.getMessage();
				this.componentImpl.getLogger().error("用户创建失败." + errMsgString, errThrowable);
				msg.fail(100, errMsgString);		
            }
        });


    }
    

    /**
     * 服务名.组件名.users.operators.post
     *
     * @return otocloud-auth.user-management.users.operators.post
     */
    @Override
    public String getEventAddress() {
        return ADDRESS;
    }

    /**
     * post /api/"服务名"/"组件名"/api/fun
     *
     * @return /api/otocloud-auth/user-management/users/operators
     */
    @Override
    public HandlerDescriptor getHanlderDesc() {
        HandlerDescriptor handlerDescriptor = super.getHanlderDesc();
        ActionURI uri = new ActionURI(ADDRESS, HttpMethod.POST);
        handlerDescriptor.setRestApiURI(uri);
        return handlerDescriptor;
    }

}