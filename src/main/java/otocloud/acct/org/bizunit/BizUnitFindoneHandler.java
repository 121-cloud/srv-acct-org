package otocloud.acct.org.bizunit;

import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import otocloud.acct.org.dao.BizUnitDAO;
import otocloud.common.ActionURI;
import otocloud.framework.core.CommandMessage;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;


/**
 * 
 * zhangyef@yonyou.com on 2015-12-16.
 */
public class BizUnitFindoneHandler extends OtoCloudEventHandlerImpl<JsonObject> {
	
	public static final String ADDRESS = "findone";


    public BizUnitFindoneHandler(OtoCloudComponentImpl componentImpl) {
        super(componentImpl);
    }

    /* 
     * {
     * 	  acct_id,
     *    unit_code
     * }
     */
    @Override
    public void handle(CommandMessage<JsonObject> msg) {
        
        JsonObject body = msg.body();
		JsonObject content = body.getJsonObject("content");
		
		//JsonObject sessionInfo = msg.getSession();
		
		Long acctId = content.getLong("acct_id");
		String unit_code = content.getString("unit_code");

        Future<ResultSet> getFuture = Future.future();
        
        BizUnitDAO userDAO = new BizUnitDAO(this.componentImpl.getSysDatasource());
        userDAO.getBizUnit(acctId, unit_code, getFuture);
        
        getFuture.setHandler(ret -> {
            if (ret.succeeded()) {            	
                msg.reply(new JsonArray(ret.result().getRows()));
            } else {
            	Throwable errThrowable = ret.cause();
    			String errMsgString = errThrowable.getMessage();
    			this.componentImpl.getLogger().error(errMsgString, errThrowable);
    			msg.fail(100, errMsgString);
            }
        });

    }

    /**
     * "服务名".user-management.department.query
     *
     * @return
     */
    @Override
    public String getEventAddress() {
        return ADDRESS;
    }


    @Override
    public HandlerDescriptor getHanlderDesc() {
        HandlerDescriptor handlerDescriptor = super.getHanlderDesc();
        ActionURI uri = new ActionURI(ADDRESS, HttpMethod.POST);
        handlerDescriptor.setRestApiURI(uri);
        return handlerDescriptor;
    }
}