package otocloud.acct.org.bizunit.user;

import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import otocloud.acct.org.dao.UserDAO;
import otocloud.common.ActionURI;
import otocloud.framework.common.IgnoreAuthVerify;
import otocloud.framework.core.CommandMessage;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;

/**
 * 
 */
@IgnoreAuthVerify
public class ExistUserInAcctHandler extends OtoCloudEventHandlerImpl<JsonObject> {
	
	public static final String ADDRESS = "exist-user";

    public ExistUserInAcctHandler(OtoCloudComponentImpl componentImpl) {
        super(componentImpl);
    }

    /* 
     * { 
     * 	  auth_user_id: 
     * 	  acct_id
     * }
     * 
     */
    @Override
    public void handle(CommandMessage<JsonObject> msg) {
    	
        JsonObject content = msg.body().getJsonObject("content");
    	Long auth_user_id = content.getLong("auth_user_id");
    	Long acct_id = content.getLong("acct_id");
    	
    	
        Future<Boolean> verifyFuture = Future.future();
        //检查手机号字段是否重复.(重复表示已经注册过)
        UserDAO userDAO = new UserDAO(this.componentImpl.getSysDatasource());
        userDAO.existUserInAcct(auth_user_id, acct_id, verifyFuture);
        
        verifyFuture.setHandler(ret -> {
            if (ret.failed()) {
				Throwable err = ret.cause();
				String errMsg = err.getMessage();
				componentImpl.getLogger().error(errMsg, err);	
				msg.fail(400, errMsg);
                return;
            }

            boolean exists = ret.result();
            JsonObject reply = new JsonObject();
            reply.put("exists", exists);

            msg.reply(reply);
 
        });
    }

    /**
     * 事件总线地址.
     *
     * @return "服务名"."组件名".users.verify.cellNo
     */
    @Override
    public String getEventAddress() {
        return ADDRESS;
    }

    /**
     * REST API.
     *
     * @return get /api/"服务名"/"组件名"/users/verify/:cellNo
     */
    @Override
    public HandlerDescriptor getHanlderDesc() {
        HandlerDescriptor handlerDescriptor = super.getHanlderDesc();
        ActionURI uri = new ActionURI(ADDRESS, HttpMethod.GET);
        handlerDescriptor.setRestApiURI(uri);
        return handlerDescriptor;
    }
}
