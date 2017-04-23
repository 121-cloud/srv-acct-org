package otocloud.acct.org.bizunit.user;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import otocloud.common.ActionURI;
import otocloud.framework.core.CommandMessage;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;


public class UserJoinToAcctHandler extends OtoCloudEventHandlerImpl<JsonObject> {
	
	public static final String ADDRESS = "join-to-acct";
    
    
    public UserJoinToAcctHandler(OtoCloudComponentImpl component) {
        super(component);
    }
    
    
    /* 
     * {
     * 	  acct_id: 租户ID
     * 	  biz_unit_id: 业务单元ID
     * 	  d_org_role_id
     * 	  post_id: 岗位ID
     * 	  auth_role_id: 对应的角色 规格
     * 	  auth_user_id: 
     * }
     * 
     */
    @Override
    public void handle(CommandMessage<JsonObject> msg) {
 
        JsonObject body = msg.body();
 /*       JsonObject session = msg.getSession();
        
        JsonObject content = body.getJsonObject("content");
        Long acctId = content.getLong("acct_id");
*/ 
        
		String authSrvName = componentImpl.getDependencies().getJsonObject("auth_service").getString("service_name","");
		String address = authSrvName + ".user-management.join-to-acct";

		
		componentImpl.getEventBus().send(address,
				body, regUserRet->{
					if(regUserRet.succeeded()){
						JsonObject userInfo = (JsonObject)regUserRet.result().body(); 
						//userRegInfo.put("id", userInfo.getJsonObject("data").getInteger("userId"));						
						msg.reply(userInfo);							
					}else{		
						Throwable err = regUserRet.cause();						
						err.printStackTrace();		
						msg.fail(100, err.getMessage());
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