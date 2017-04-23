package otocloud.acct.org.bizunit.post;

import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import otocloud.acct.org.dao.BizUnitPostDAO;
import otocloud.common.ActionURI;
import otocloud.framework.core.CommandMessage;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudComponentImpl;
import otocloud.framework.core.OtoCloudEventHandlerImpl;


/**
 * 查询指定企业账户下的业务单元列表.
 * zhangyef@yonyou.com on 2015-12-16.
 */
public class BizUnitPostQueryHandler extends OtoCloudEventHandlerImpl<JsonObject> {
	
	public static final String ADDRESS = "query";

    public BizUnitPostQueryHandler(OtoCloudComponentImpl componentImpl) {
        super(componentImpl);
    }

    /* 
     * {
     *    acct_biz_unit_id
     * }
     */
    @Override
    public void handle(CommandMessage<JsonObject> msg) {
        
        JsonObject body = msg.body();
        //JsonObject session = body.getJsonObject(SessionSchema.SESSION);

        JsonObject post = body.getJsonObject("content");
        
        Long bizUnitId = post.getLong("acct_biz_unit_id");

        Future<ResultSet> getFuture = Future.future();
        
        BizUnitPostDAO userDAO = new BizUnitPostDAO(this.componentImpl.getSysDatasource());
        userDAO.getList(bizUnitId, getFuture);
        
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

    /**
     * "服务名".user-management.department.query
     *
     * @return
     */
    @Override
    public String getEventAddress() {
        return ADDRESS;
    }

    /**
     * 注册REST API。
     *
     * @return get /api/"服务名"/"组件名"
     */
    @Override
    public HandlerDescriptor getHanlderDesc() {
        HandlerDescriptor handlerDescriptor = super.getHanlderDesc();
        ActionURI uri = new ActionURI(ADDRESS, HttpMethod.POST);
        handlerDescriptor.setRestApiURI(uri);
        return handlerDescriptor;
    }
}
