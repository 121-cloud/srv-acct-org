/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.org;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

import otocloud.acct.org.app.AcctAppComponent;
import otocloud.acct.org.baseinfo.AccountManagerComponent;
import otocloud.acct.org.bizunit.BizUnitComponent;
import otocloud.acct.org.bizunit.post.BizUnitPostComponent;
import otocloud.acct.org.bizunit.user.UserComponent;
import otocloud.framework.core.OtoCloudComponent;
import otocloud.framework.core.OtoCloudServiceForVerticleImpl;
import otocloud.persistence.dao.MongoDataSource;



/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月12日
 * @author lijing@yonyou.com
 */
public class AccountOrgService extends OtoCloudServiceForVerticleImpl {	
	
    private MongoDataSource authSrvMongoDataSource;
    
	public MongoDataSource getAuthSrvMongoDataSource() {
		return authSrvMongoDataSource;
	}

	@Override
	public void afterInit(Future<Void> initFuture) {		
        //如果有mongo_client配置,放入上下文当中.
        if (this.srvCfg.containsKey("auth_mongo_client")) {
            JsonObject mongoClientCfg = this.srvCfg.getJsonObject("auth_mongo_client");
	        if(mongoClientCfg != null){
	        	authSrvMongoDataSource = new MongoDataSource();
	        	authSrvMongoDataSource.init(vertxInstance, mongoClientCfg);				
	        }
        }
        
        super.afterInit(initFuture);        
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<OtoCloudComponent> createServiceComponents() {
		
		List<OtoCloudComponent> components = new ArrayList<OtoCloudComponent>();
		
		AccountManagerComponent component = new AccountManagerComponent();
		components.add(component);
		
		AcctAppComponent acctAppComponent = new AcctAppComponent();
		components.add(acctAppComponent);
		
		BizUnitComponent bizUnitComponent = new BizUnitComponent();
		components.add(bizUnitComponent);
		
		BizUnitPostComponent bizUnitPostComponent = new BizUnitPostComponent();
		components.add(bizUnitPostComponent);
		
		UserComponent userComponent = new UserComponent();
		components.add(userComponent);
				
		return components;
	}  
    
	@Override
	public String getServiceName() {
		return "otocloud-acct-org";
	}

}