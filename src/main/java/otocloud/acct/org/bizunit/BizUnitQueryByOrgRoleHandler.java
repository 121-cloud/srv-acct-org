package otocloud.acct.org.bizunit;

import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import otocloud.acct.org.dao.BizUnitDAO;
import otocloud.common.ActionURI;
import otocloud.framework.core.CommandMessage;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;


/**
 * 查询指定企业账户下的业务单元列表.
 * zhangyef@yonyou.com on 2015-12-16.
 */
public class BizUnitQueryByOrgRoleHandler extends OtoCloudEventHandlerImpl<JsonObject> {
	
	public static final String ADDRESS = "query-by-orgrole";

    public BizUnitQueryByOrgRoleHandler(OtoCloudComponentImpl componentImpl) {
        super(componentImpl);
    }

    /* 
     * {
     * 	  acct_id:
     * 	  org_role_id:
     * }
     */
    @Override
    public void handle(CommandMessage<JsonObject> msg) {
        
        JsonObject body = msg.body();
		JsonObject content = body.getJsonObject("content");
		
		Long acctId = content.getLong("acct_id");
		Long orgRoleId = content.getLong("org_role_id");

        Future<ResultSet> getFuture = Future.future();
        
        BizUnitDAO userDAO = new BizUnitDAO(this.componentImpl.getSysDatasource());
        userDAO.getBizUnitByOrgRole(acctId, orgRoleId, getFuture);
        
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
