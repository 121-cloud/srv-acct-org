package otocloud.acct.org.app;

import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import otocloud.acct.org.dao.AppSubscribeDAO;
import otocloud.common.ActionURI;
//import otocloud.common.SessionSchema;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudBusMessage;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;


/**
 * 查询指定企业账户下的业务单元列表.
 * zhangyef@yonyou.com on 2015-12-16.
 */
public class NewAppListQueryHandler extends OtoCloudEventHandlerImpl<JsonObject> {
	
	public static final String QUERY = "new-applist-query";

    public NewAppListQueryHandler(OtoCloudComponentImpl componentImpl) {
        super(componentImpl);
    }

    /* 
     * {
     *   acct_id:
     * }
     */
    @Override
    public void handle(OtoCloudBusMessage<JsonObject> msg) {
        
        JsonObject body = msg.body();
    	JsonObject content = body.getJsonObject("content");
		
		Long acctId = content.getLong("acct_id");

        Future<ResultSet> getFuture = Future.future();
        
        AppSubscribeDAO appSubscribeDAO = new AppSubscribeDAO(componentImpl.getSysDatasource());	
        appSubscribeDAO.getNewAppsForAcct(acctId, getFuture);
        
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


    @Override
    public String getEventAddress() {
        return QUERY;
    }


    @Override
    public HandlerDescriptor getHanlderDesc() {
        HandlerDescriptor handlerDescriptor = super.getHanlderDesc();
        ActionURI uri = new ActionURI(QUERY, HttpMethod.POST);
        handlerDescriptor.setRestApiURI(uri);
        return handlerDescriptor;
    }
}
