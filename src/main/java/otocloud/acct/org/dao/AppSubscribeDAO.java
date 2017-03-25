/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.org.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import otocloud.persistence.dao.JdbcDataSource;
import otocloud.persistence.dao.OperatorDAO;
import otocloud.persistence.dao.TransactionConnection;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;

public class AppSubscribeDAO extends OperatorDAO {	
	
    public AppSubscribeDAO(JdbcDataSource dataSource) {
        super(dataSource);
    }

	/**
	{
		"acct_id":
		"d_app_id":
		"app_version_id":
		"app_inst_group": 
		"activities":[
			{
				"app_activity_id":
			}
		]
	}
	*/
	public void subscribeApp(TransactionConnection conn,
			JsonObject subscribeInfo, JsonObject sessionInfo,
			Handler<AsyncResult<Map<Long, Long>>> done) {
		
		SQLConnection realConn = conn.getConn();

		Long userId = Long.parseLong(sessionInfo.getString("user_id"));
		Long acctId = subscribeInfo.getLong("acct_id");
		Long appId = subscribeInfo.getLong("d_app_id");
		Long appVerId = subscribeInfo.getLong("app_version_id");
		String appInst = subscribeInfo.getString("app_inst_group", "");
		String status = "A";
		if(appInst.isEmpty()){
			status = "U";
		}		

		Future<Map<Long, Long>> retFuture = Future.future();
		retFuture.setHandler(done);
		
		Map<Long, Long> activityMap = new HashMap<Long, Long>();

		String sql1 = "INSERT INTO acct_app(acct_id,d_app_id,app_version_id,app_inst_group,status,entry_id,entry_datetime)VALUES(?,?,?,?,?,now())";
		String sql2 = "INSERT INTO acct_app_activity(acct_app_id,d_app_id,app_activity_id,acct_id,entry_id,entry_datetime)VALUES(?,?,?,?,?,now())";

		  realConn.updateWithParams(sql1, 
				  	new JsonArray()
						  .add(acctId)
						  .add(appId)
						  .add(appVerId)
						  .add(appInst)
						  .add(status)
						  .add(userId),  
			  ret -> {		
				  if(ret.succeeded()){
					  UpdateResult updateRet = ret.result();
					  //JsonObject acctResult = new JsonObject(); 
					  Long id = updateRet.getKeys().getLong(0);	
					  subscribeInfo.put("id", id);
					  
					  JsonArray activities = subscribeInfo.getJsonArray("activities", null);
					  if(activities == null || activities.size() == 0){
						  retFuture.complete(activityMap);
					  }else{					  
						  Future<Void> depFuture = Future.future();
						  
						  subscribeActivity(conn, activityMap, userId, acctId, appId, id, sql2, activities, activities.size(), 0, depFuture);
						  
						  depFuture.setHandler(subscribeActivityRet->{
							  if(subscribeActivityRet.succeeded()){
								  retFuture.complete(activityMap);
							  }else{
								  Throwable err = subscribeActivityRet.cause();
								  retFuture.fail(err);
							  }
						  });	
					  }

				  }else{
					  Throwable err = ret.cause();
					  err.printStackTrace();
					  retFuture.fail(err);
				  }
			  }); 
	}
	
	//INSERT INTO acct_app_activity(acct_app_id,d_app_id,app_activity_id,acct_id,entry_id,entry_datetime)VALUES(?,?,?,?,?,now())	
	private void subscribeActivity(TransactionConnection conn, Map<Long, Long> activityMap, Long userId, Long acctId, Long appId, Long acct_app_id, String sql, JsonArray activities, Integer size, int index, Future<Void> depFuture) {
		JsonObject activity = activities.getJsonObject(index);
		Long activityId = activity.getLong("app_activity_id",0L);
		
		SQLConnection realConn = conn.getConn();
		  realConn.updateWithParams(sql, 
				  	new JsonArray()
				  		.add(acct_app_id)
				  		.add(appId)
		  				.add(activityId)
		  				.add(acctId)
				  		.add(userId),
		    		  ret2 -> {		
		    			  if(ret2.succeeded()){    	
		    				  Long id = ret2.result().getKeys().getLong(0);
		    				  activity.put("id", id);	
		    				  activityMap.put(activityId, id);
		    				  
		  						Integer nextIdx = index + 1;
		  						if (nextIdx < size)
		  							subscribeActivity(conn, activityMap, userId, acctId, appId, acct_app_id, sql, activities, size, nextIdx, depFuture);
		  						else if (nextIdx >= size) {
		  							depFuture.complete();
		  						}
		  						
		    			  }else{
		    				  Throwable err = ret2.cause();
		    				  err.printStackTrace();	
		    				  
		    				  depFuture.fail(err);
		    			  }
		    			  
		    		  });
		
	}
	
	
	
	public void addBizUnitAndPos(Long acctId, Long userId, Long appId, Map<Long, Long> activityMap, JsonArray bizUnitInfos, 
			Handler<AsyncResult<Void>> done) {

		Future<Void> retFuture = Future.future();
		retFuture.setHandler(done);		
		  
       createDBConnect(conn -> conn.setAutoCommit(true, res -> {
            if (res.failed()) {
            	retFuture.fail(res.cause());
                return;	            
            }

            bizUnitInfos.forEach(item->{
				  JsonObject bizUnitInfo = (JsonObject)item;		
				  addBizUnitAndPos(conn, acctId, userId, appId, activityMap, bizUnitInfo, 
							bizUnitRet->{								
							});

			  });		
			  
			  retFuture.complete();

        }), e ->{
        	retFuture.fail(e);
        	logger.error("连接数据库错误.", e);
        });		  
		
	}
	
	
	/**
		{
			"org_role_id": 
			"unit_code":
			"unit_name":
			"acct_biz_unit_post":[
				{
					auth_role_id：
					post_code:
					post_name:
					acct_biz_unit_post_activity:[
						{
							d_app_activity_id:
							d_app_activity_code:							
						}						
					]					
				
				}				
			]
		}
	*/
	private void addBizUnitAndPos(SQLConnection conn, Long acctId, Long userId, Long appId, Map<Long, Long> activityMap, JsonObject bizUnitInfo, 
			Handler<AsyncResult<Void>> done) {

		Future<Void> retFuture = Future.future();
		retFuture.setHandler(done);
		
		Long org_role_id = bizUnitInfo.getLong("org_role_id");

	  String sql3 = "INSERT INTO acct_biz_unit(unit_code,unit_name,acct_id,org_role_id,entry_id,entry_datetime)VALUES(?,?,?,?,?,now())";
	  String sql4 = "INSERT INTO acct_biz_unit_post(post_code,post_name,d_org_role_id,acct_biz_unit_id,auth_role_id,acct_id,entry_id,entry_datetime)VALUES(?,?,?,?,?,?,?,now())";
	  String sql5 = "INSERT INTO acct_biz_unit_post_activity(acct_biz_unit_post_id,acct_app_activity_id,d_app_id,d_app_activity_id,d_app_activity_code,acct_id,entry_id,entry_datetime)VALUES(?,?,?,?,?,?,?,now())";

        conn.updateWithParams(sql3, 
			  	new JsonArray()
					  .add(bizUnitInfo.getString("unit_code"))
					  .add(bizUnitInfo.getString("unit_name"))
					  .add(acctId)
					  .add(org_role_id)
					  .add(userId),  
		  ret -> {		
			  if(ret.succeeded()){
				  UpdateResult updateRet = ret.result();
				  //JsonObject acctResult = new JsonObject(); 
				  Long bizUnitId = updateRet.getKeys().getLong(0);	
				  bizUnitInfo.put("id", bizUnitId);
				  
				  JsonArray acct_biz_unit_posts = bizUnitInfo.getJsonArray("acct_biz_unit_post", null);
				  if(acct_biz_unit_posts == null || acct_biz_unit_posts.size() == 0){
					  retFuture.complete();
				  }else{	
					  acct_biz_unit_posts.forEach(item->{
						  JsonObject acct_biz_unit_post = (JsonObject)item;		
						  
				            conn.updateWithParams(sql4, 
								  	new JsonArray()
										  .add(acct_biz_unit_post.getString("post_code"))
										  .add(acct_biz_unit_post.getString("post_name"))
										  .add(org_role_id)
										  .add(bizUnitId)
										  .add(acct_biz_unit_post.getLong("auth_role_id"))
										  .add(acctId)												  
										  .add(userId),  
							  postRet -> {		
								  if(postRet.succeeded()){
									  UpdateResult postUpdateRet = postRet.result();
									  //JsonObject acctResult = new JsonObject(); 
									  Long postId = postUpdateRet.getKeys().getLong(0);	
									  acct_biz_unit_post.put("id", postId);
									  
									  JsonArray acct_biz_unit_post_activities = acct_biz_unit_post.getJsonArray("acct_biz_unit_post_activity", null);
									  if(acct_biz_unit_post_activities == null || acct_biz_unit_post_activities.size() == 0){
										  
									  }else{	
										  acct_biz_unit_post_activities.forEach(item2->{
											  JsonObject acct_biz_unit_post_activity = (JsonObject)item2;
											  Long app_activity_id = acct_biz_unit_post_activity.getLong("d_app_activity_id");
											  String app_activity_code = acct_biz_unit_post_activity.getString("d_app_activity_code");
											  
									            conn.updateWithParams(sql5, 
													  	new JsonArray()
															  .add(postId)
															  .add(activityMap.get(app_activity_id))
															  .add(appId)
															  .add(app_activity_id)
															  .add(app_activity_code)
															  .add(acctId)												  
															  .add(userId), 
												  postActRet -> {		
													  if(postActRet.succeeded()){
														  UpdateResult postActUpdateRet = postActRet.result();
														  //JsonObject acctResult = new JsonObject(); 
														  Long postActId = postActUpdateRet.getKeys().getLong(0);	
														  acct_biz_unit_post_activity.put("id", postActId);
													  }else{
														  Throwable err = postActRet.cause();
														  err.printStackTrace();
													  }
												  });
											  
										  });					  

									  }

								  }else{
									  Throwable err = postRet.cause();
									  err.printStackTrace();											  
								  }
							  }); 
						  
					  });		
					  
					  retFuture.complete();
				  }

			  }else{
				  Throwable err = ret.cause();
				  err.printStackTrace();
				  retFuture.fail(err);
			  }
		  }); 

		
	}
	

    public void getAppListByAcct(Long acctId, Future<ResultSet> future) {
        
	   final String sql = "SELECT * FROM view_acct_app WHERE acct_id=?";
	   JsonArray params = new JsonArray();
	   params.add(acctId);
	
	   this.queryWithParams(sql, params, future);
    	
    }
    
    public void getActivityList(Long acct_app_id, boolean load_auth_roles, Future<ResultSet> future) {
        
	   final String sql = "SELECT * FROM view_acct_activity WHERE acct_app_id=?";
	   JsonArray params = new JsonArray();
	   params.add(acct_app_id);
	   
	   Future<ResultSet> innerFuture = Future.future();
	
	   this.queryWithParams(sql, params, innerFuture);
	   
	   innerFuture.setHandler(result -> {
	       if (result.succeeded()) {
		       	ResultSet resultSet = result.result();
		       	
		       	if(load_auth_roles){
		       		if(resultSet.getRows() != null){
		       			
						List<Future> futures = new ArrayList<Future>();
						
						List<JsonObject> retJsonObjects = resultSet.getRows();
						
						retJsonObjects.forEach(item->{
							
							Future<Void> rolesFuture = Future.future();
							futures.add(rolesFuture);
							
							getActivityRoleList(item.getLong("app_activity_id"), rolesRet->{
								if (rolesRet.succeeded()) {
									item.put("auth_roles", rolesRet.result().getRows());	
									rolesFuture.complete();
								}else{
									rolesFuture.fail(rolesRet.cause());
								}								
							});
							
						});
		       			
						CompositeFuture.join(futures).setHandler(ar -> {
							future.complete(resultSet);
						});

		       		}else{
		       			future.complete(resultSet);	
		       		}
		       		
		       	}else{
		       		future.complete(resultSet);	
		       	}
	       } else {
	       		Throwable err = result.cause();								
	            future.fail(err);                
	       }
	   });    	
    	

    }
    
    
    public void getActivityRoleList(Long activityId, Handler<AsyncResult<ResultSet>> ret) {
        
	   final String sql = "SELECT * FROM view_activity_auth_role WHERE app_activity_id=?";
	   JsonArray params = new JsonArray();
	   params.add(activityId);
	
	   Future<ResultSet> innerFuture = Future.future();
	   innerFuture.setHandler(ret);
	
	   this.queryWithParams(sql, params, innerFuture);
    	
    }
	    
    
    
    public void permissionVerify(Long acctId, Long appId, Future<Boolean> future) {
        
        final String sql = "SELECT count(*) as num FROM acct_app WHERE acct_id=? AND d_app_id=? AND status='A'";
        JsonArray params = new JsonArray();
        params.add(acctId);
        params.add(appId);

        Future<ResultSet> innerFuture = Future.future();

        this.queryWithParams(sql, params, innerFuture);

        innerFuture.setHandler(result -> {
            if (result.succeeded()) {
            	ResultSet resultSet = result.result();
            	List<JsonObject> retDataArrays = resultSet.getRows();
            	if(retDataArrays != null && retDataArrays.size() > 0){
            		Long num = retDataArrays.get(0).getLong("num");
            		if(num > 0){
            			future.complete(true);
            		}else{
            			future.complete(false);
            		}
            	}else{
            		future.complete(false);
            	}

            } else {
            	Throwable err = result.cause();								
                future.fail(err);                
            }
        });

	}


	public void appUnSubscribe(Long id, JsonObject sessionInfo,
			Handler<AsyncResult<UpdateResult>> done) {

		Future<UpdateResult> retFuture = Future.future();
		retFuture.setHandler(done);
		
		Long userId = Long.parseLong(sessionInfo.getString("user_id"));

		String sql = "UPDATE acct_app SET status='D',update_id=?,update_datetime=now() WHERE id=?";

		this.updateWithParams(sql,
				new JsonArray().add(userId).add(id),
				retFuture);
	}
	
    public void getNewAppsForAcct(Long acct_id, Future<ResultSet> future) {
        
	   final String sql = "SELECT * FROM view_app_version where status='A' AND id not in(select acct_app.d_app_id from acct_app where acct_app.acct_id=?)";
	   JsonArray params = new JsonArray();
	   params.add(acct_id);
	
	   this.queryWithParams(sql, params, future);	
    	
    }

}
