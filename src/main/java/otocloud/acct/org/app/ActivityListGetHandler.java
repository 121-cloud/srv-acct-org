package otocloud.acct.org.app;

import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import otocloud.acct.org.dao.AppSubscribeDAO;
import otocloud.common.ActionURI;
import otocloud.framework.common.IgnoreAuthVerify;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudBusMessage;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;


/**
 * TODO: 应用模块查询
 * @date 2016年11月15日
 * @author lijing
 */
@IgnoreAuthVerify
public class ActivityListGetHandler extends OtoCloudEventHandlerImpl<JsonObject> {
	
	public static final String ADDRESS = "activities-get";

	public ActivityListGetHandler(OtoCloudComponentImpl componentImpl) {
		super(componentImpl);
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
		
		ActionURI uri = new ActionURI(ADDRESS, HttpMethod.POST);
		handlerDescriptor.setRestApiURI(uri);
		
		return handlerDescriptor;		

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEventAddress() {
		return ADDRESS;
	}

	/**
	 * {
	 * 	  acct_app_id:
	 * 	  load_auth_roles: true 是否加载活动角色
	 * }
	 */
	//处理器
	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
		JsonObject content = msg.body().getJsonObject("content");
		Long acct_app_id = content.getLong("acct_app_id");
		boolean load_auth_roles = content.getBoolean("load_auth_roles", false);
		
		Future<ResultSet> getFuture = Future.future();
        
		AppSubscribeDAO userDAO = new AppSubscribeDAO(this.componentImpl.getSysDatasource());
        userDAO.getActivityList(acct_app_id, load_auth_roles, getFuture);
        
        getFuture.setHandler(ret -> {
            if (ret.succeeded()) {
                msg.reply(ret.result().getRows());
            } else {
            	Throwable errThrowable = ret.cause();
    			String errMsgString = errThrowable.getMessage();
    			this.componentImpl.getLogger().error(errMsgString, errThrowable);
    			msg.fail(100, errMsgString);
            }
        });
	}
	
}
