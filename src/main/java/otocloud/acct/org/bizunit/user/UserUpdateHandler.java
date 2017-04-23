package otocloud.acct.org.bizunit.user;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import otocloud.common.ActionURI;
import otocloud.framework.core.CommandMessage;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;

/**
 * Created by lijing on 2015-10-20.
 */

public class UserUpdateHandler extends OtoCloudEventHandlerImpl<JsonObject> {
	

	private static final String ADDRESS = "update";
	

    public UserUpdateHandler(OtoCloudComponentImpl componentImpl) {
        super(componentImpl);
    }    
 
    
    /* 
     * { 
     * 		  id: 
	 *	      name: 用户名	 
	 *	      cell_no: 电话
	 *	      email: 邮箱	  
     * }
     * 
     */
    @Override
    public void handle(CommandMessage<JsonObject> msg) {
        
    	JsonObject body = msg.body();
    	
/*		JsonObject params = body.getJsonObject("queryParams");		
		
		Long userId = Long.parseLong(params.getString("id"));
    	
        JsonObject content = msg.body().getJsonObject("content");
    	
    	//Long userId = content.getLong("id", 0L);
    	String name = content.getString("name", null);
    	//String pwd = content.getString("password", "");
    	String cell_no = content.getString("cell_no", null);
    	String email = content.getString("email", null);    */
    	

/*        Future<UpdateResult> future = Future.future();
        
        UserDAO userDAO = new UserDAO(this.componentImpl.getSysDatasource());
        userDAO.update(userId, name, cell_no, email, future);

        future.setHandler(ret -> {
            if (ret.succeeded()) {
                msg.reply(ret.result().toJson());
            } else {
            	Throwable errThrowable = ret.cause();
    			String errMsgString = errThrowable.getMessage();
    			this.componentImpl.getLogger().error(errMsgString, errThrowable);
    			msg.fail(100, errMsgString);
            }
        });*/
    	
		String authSrvName = componentImpl.getDependencies().getJsonObject("auth_service").getString("service_name","");
		String address = authSrvName + ".user-management.update";

		
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


    @Override
    public String getEventAddress() {
        return ADDRESS;
    }

    @Override
    public HandlerDescriptor getHanlderDesc() {
        HandlerDescriptor handlerDescriptor = super.getHanlderDesc();
        ActionURI uri = new ActionURI(ADDRESS + "/:id", HttpMethod.PUT);
        handlerDescriptor.setRestApiURI(uri);
        return handlerDescriptor;
    }
}
