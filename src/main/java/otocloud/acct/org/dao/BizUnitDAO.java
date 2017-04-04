/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.org.dao;

import java.util.ArrayList;
import java.util.List;

import otocloud.persistence.dao.JdbcDataSource;
import otocloud.persistence.dao.OperatorDAO;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.CompositeFutureImpl;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;


public class BizUnitDAO extends OperatorDAO{
	
    public BizUnitDAO(JdbcDataSource dataSource) {
        super(dataSource);
    }
	
    public void getBizUnitList(Long acctId, Future<ResultSet> future) {
        
	   final String sql = "SELECT * FROM view_acct_biz_unit where acct_id=? AND status='A'";
	   JsonArray params = new JsonArray();
	   params.add(acctId);
	
	   this.queryWithParams(sql, params, future);

    }
    
    public void getActivityForBizUnit(Long acctId, Long org_role_id, Future<ResultSet> future) {
        
	   final String sql = "select f.id,f.app_id,f.app_name,f.activity_code,f.activity_name,f.activity_desc,f.d_is_platform,e.acct_app_id,e.acct_app_activity_id from "
	   		+ "(select d.app_activity_id,d.id as acct_app_activity_id,d.acct_app_id from "
	   		+ "((select a.app_id from (select app_id from app_org_role where org_role_id=?)as a,"
	   		+ "(select d_app_id from acct_app where acct_id=?)as b where a.app_id=b.d_app_id))as c inner join acct_app_activity as d on c.app_id=d.d_app_id where d.acct_id=?)as e"
	   		+ " inner join view_activity f on e.app_activity_id=f.id";
	   JsonArray params = new JsonArray();
	   params.add(org_role_id);
	   params.add(acctId);
	   params.add(acctId);
	
	   this.queryWithParams(sql, params, future);

    }
    
    
    public void getBizUnitByOrgRole(Long acctId, Long orgRoleId, Future<ResultSet> future) {
        
	   final String sql = "SELECT * FROM view_acct_biz_unit WHERE acct_id=? AND org_role_id=? AND status='A'";
	   JsonArray params = new JsonArray();
	   params.add(acctId);
	   params.add(orgRoleId);
	
	   this.queryWithParams(sql, params, future);

    	
    }
	
	public void create(JsonArray bizUnits, JsonObject sessionInfo, Handler<AsyncResult<Void>> done) {
		  
	  Future<Void> retFuture = Future.future();
	  retFuture.setHandler(done);	 
	  
	  Long userId = Long.parseLong(sessionInfo.getString("user_id"));
		  
	  String sql = "INSERT INTO acct_biz_unit(unit_code,unit_name,is_global,unit_manager,org_role_id,acct_id,entry_id,entry_datetime)VALUES(?,?,?,?,?,?,?,now())"; 
	  
		List<Future> futures = new ArrayList<Future>();		
		
		bizUnits.forEach(item->{
			
			JsonObject bizUnit = (JsonObject)item;
			
			Future<UpdateResult> future = Future.future();
			futures.add(future);			
			  
			  this.updateWithParams(sql, 
				  	new JsonArray()
						  .add(bizUnit.getString("unit_code"))
						  .add(bizUnit.getString("unit_name"))	
						  .add(bizUnit.getBoolean("is_global"))
						  .add(bizUnit.getLong("unit_manager", 0L))
						  .add(bizUnit.getLong("org_role_id"))
						  .add(bizUnit.getLong("acct_id"))
						  .add(userId),  
						  future);	
			
		});
			
		CompositeFuture.join(futures).setHandler(ar -> {
			CompositeFutureImpl comFutures = (CompositeFutureImpl)ar;
			if(comFutures.size() > 0){										
				for(int i=0;i<comFutures.size();i++){
					if(comFutures.succeeded(i)){						
					}else{						
						Throwable err = comFutures.cause(i);
						err.printStackTrace();
					}
				}
			}
			retFuture.complete();
		});
	   

	}

	
	public void modify(Long depId, JsonObject department, JsonObject sessionInfo, Handler<AsyncResult<UpdateResult>> done) {
		 
	  Future<UpdateResult> retFuture = Future.future();
	  retFuture.setHandler(done);
	  
	  JsonObject whereObj = new JsonObject()
	  		.put("id", depId);
	  
	  Long userId = Long.parseLong(sessionInfo.getString("user_id"));
	  
	  this.updateBy("acct_biz_unit", department, whereObj, userId, retFuture);
	}

	public void delete(Long depId, JsonObject sessionInfo, Handler<AsyncResult<UpdateResult>> done) {
		
		  Future<UpdateResult> retFuture = Future.future();
		  retFuture.setHandler(done);
		  
/*		  String sql = "DELETE FROM org_dept WHERE id=?";

		  this.deleteWithParams(sql,  
				  	new JsonArray()
						  .add(depId), retFuture);	  */
		  
		  Long userId = Long.parseLong(sessionInfo.getString("user_id"));
		  
		  String sql = "UPDATE acct_biz_unit SET status='D',delete_id=?,delete_datetime=now() WHERE id=?";

		  this.updateWithParams(sql, 
				  	new JsonArray()
						  .add(userId)
						  .add(depId),  
						  retFuture);	  
	}
	

}
