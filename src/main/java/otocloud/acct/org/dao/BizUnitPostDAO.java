/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.acct.org.dao;

import java.util.ArrayList;
import java.util.List;

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


public class BizUnitPostDAO extends OperatorDAO{
	
    public BizUnitPostDAO(JdbcDataSource dataSource) {
        super(dataSource);
    }
	
    public void getList(Long bizUnitId, Future<ResultSet> future) {
        
	   final String sql = "SELECT * FROM acct_biz_unit_post where acct_biz_unit_id=?";
	   JsonArray params = new JsonArray();
	   params.add(bizUnitId);
	
	   this.queryWithParams(sql, params, future);
    	
    }
	
	/**
	{
		post_code
		post_name
		d_org_role_id
		acct_biz_unit_id
		auth_role_id
		acct_id
		post_activity:[
			{			
				acct_app_activity_id	
				d_app_id	
				d_acct_app_id
				d_app_activity_id:
				d_app_activity_code:								
			}						
		]
	}
	*/
	public void create(JsonObject post, JsonObject sessionInfo, Handler<AsyncResult<UpdateResult>> done) {
		  
	  Future<UpdateResult> retFuture = Future.future();
	  retFuture.setHandler(done);	 
	  
	  Long userId = Long.parseLong(sessionInfo.getString("user_id"));
		  
	  String sql = "INSERT INTO acct_biz_unit_post(post_code,post_name,d_org_role_id,acct_biz_unit_id,auth_role_id,acct_id,entry_id,entry_datetime)VALUES(?,?,?,?,?,?,?,now())"; 
	  
      createDBConnect(conn -> conn.setAutoCommit(true, res -> {
          if (res.failed()) {
        	  retFuture.fail(res.cause());
              return;
          }

          conn.updateWithParams(sql, 
        		  new JsonArray()
					  .add(post.getString("post_code"))
					  .add(post.getString("post_name"))	
					  .add(post.getLong("d_org_role_id"))
					  .add(post.getLong("acct_biz_unit_id"))
					  .add(post.getLong("auth_role_id", 0L))
					  .add(post.getLong("acct_id"))				  
					  .add(userId),
        		  ret -> {
              if (ret.succeeded()) {
                  
    				  UpdateResult updateRet = ret.result();
     				  
    				  if(!post.containsKey("post_activity")){
    					//关闭连接
                          closeDBConnect(conn);		
                          retFuture.complete(updateRet);
                          return;
    				  }    				  

    				  
    				  JsonArray post_activities = post.getJsonArray("post_activity");
    				  if(post_activities.size() <= 0){
      					//关闭连接
                          closeDBConnect(conn);		
                          retFuture.complete(updateRet);
                          return;
    				  }
    				  
    				  Long acct_biz_unit_post_id = updateRet.getKeys().getLong(0);
    				  
    				  List<Future> futures = new ArrayList<Future>();

    				  
    				  post_activities.forEach(item->{
    					  JsonObject activityObject = (JsonObject)item;
    					  
    						Future<Void> itemFuture = Future.future();
    						futures.add(itemFuture);
    						
    						  String activitySql = "INSERT INTO acct_biz_unit_post_activity(acct_biz_unit_post_id,acct_app_activity_id,acct_id,d_acct_app_id,d_app_id,d_app_activity_id,d_app_activity_code,entry_id,entry_datetime)VALUES(?,?,?,?,?,?,?,?,now())";
    						  conn.updateWithParams(activitySql, 
    							  	new JsonArray()
    							  		.add(acct_biz_unit_post_id)
    							  		.add(activityObject.getLong("acct_app_activity_id"))
    							  		.add(post.getLong("acct_id"))
    							  		.add(activityObject.getLong("d_acct_app_id"))
    							  		.add(activityObject.getLong("d_app_id"))
    							  		.add(activityObject.getLong("d_app_activity_id"))
    							  		.add(activityObject.getString("d_app_activity_code"))  							  		
    							  		.add(userId),
    		    		    		  ret2 -> {		
    		    		    			  if(ret2.succeeded()){    		    		    				  		  
    		    		    				  itemFuture.complete();    		    		    				  
    		    		    			  }else{
        									  Throwable err = ret2.cause();
        									  err.printStackTrace();
        									  itemFuture.fail(err);
    		    		    			  }
    		    		    		  });
    					  
    					  
    				  });
    				  
    				  
    					CompositeFuture.join(futures).setHandler(ar -> {	
    						closeDBConnect(conn);
    						retFuture.complete(updateRet);
    					}); 
                  
              } else {
              	closeDBConnect(conn);
              	retFuture.fail(ret.cause());
              }
          });

      }), e ->{
    	  retFuture.fail(e);
      	  logger.error("连接数据库错误.", e);
      });


	}

	/**
	{
		acct_id
		acct_biz_unit_post_id
		post_activity:[
			{			
				acct_app_activity_id	
				d_app_id	
				d_acct_app_id
				d_app_activity_id:
				d_app_activity_code:								
			}						
		]
	}
	*/
	public void addActivity(JsonObject post, JsonObject sessionInfo, Handler<AsyncResult<Void>> done) {
		  
	  Future<Void> retFuture = Future.future();
	  retFuture.setHandler(done);	 
	  
	  Long userId = Long.parseLong(sessionInfo.getString("user_id"));		  
  
      createDBConnect(conn -> conn.setAutoCommit(true, res -> {
          if (res.failed()) {
        	  retFuture.fail(res.cause());
              return;
          }
 
     				  
			  if(!post.containsKey("post_activity")){
				//关闭连接
                  closeDBConnect(conn);		
                  retFuture.complete();
                  return;
			  }    				  

			  
			  JsonArray post_activities = post.getJsonArray("post_activity");
			  if(post_activities.size() <= 0){
				//关闭连接
                  closeDBConnect(conn);		
                  retFuture.complete();
                  return;
			  }
			  
			  Long acct_biz_unit_post_id = post.getLong("acct_biz_unit_post_id");
			  
			  List<Future> futures = new ArrayList<Future>();

			  
			  post_activities.forEach(item->{
				  JsonObject activityObject = (JsonObject)item;
				  
					Future<Void> itemFuture = Future.future();
					futures.add(itemFuture);
					
					  String activitySql = "INSERT INTO acct_biz_unit_post_activity(acct_biz_unit_post_id,acct_app_activity_id,acct_id,d_acct_app_id,d_app_id,d_app_activity_id,d_app_activity_code,entry_id,entry_datetime)VALUES(?,?,?,?,?,?,?,?,now())";
					  conn.updateWithParams(activitySql, 
						  	new JsonArray()
						  		.add(acct_biz_unit_post_id)
						  		.add(activityObject.getLong("acct_app_activity_id"))
						  		.add(post.getLong("acct_id"))
						  		.add(activityObject.getLong("d_acct_app_id"))
						  		.add(activityObject.getLong("d_app_id"))
						  		.add(activityObject.getLong("d_app_activity_id"))
						  		.add(activityObject.getString("d_app_activity_code"))  							  		
						  		.add(userId),
	    		    		  ret2 -> {		
	    		    			  if(ret2.succeeded()){    		    		    				  		  
	    		    				  itemFuture.complete();    		    		    				  
	    		    			  }else{
									  Throwable err = ret2.cause();
									  err.printStackTrace();
									  itemFuture.fail(err);
	    		    			  }
	    		    		  });
				  
				  
			  });
			  
			  
				CompositeFuture.join(futures).setHandler(ar -> {	
					closeDBConnect(conn);
					retFuture.complete();
				}); 
                  


      }), e ->{
    	  retFuture.fail(e);
      	  logger.error("连接数据库错误.", e);
      });


	}	
	
	
	public void modify(Long id, String post_code, String post_name, Handler<AsyncResult<UpdateResult>> done) {
		 
	  Future<UpdateResult> retFuture = Future.future();
	  retFuture.setHandler(done);
	  
	   String sql = "UPDATE acct_biz_unit_post set post_code=?,post_name=? WHERE id=?";
	   JsonArray params = new JsonArray();
	   params.add(post_code);
	   params.add(post_name);
	   params.add(id);
	
	  
	  this.updateWithParams(sql, params, retFuture);
	}

	public void delete(TransactionConnection conn, Long id, Handler<AsyncResult<UpdateResult>> done) {
		    Future<UpdateResult> innerFuture = Future.future();
		    innerFuture.setHandler(done);

			
			SQLConnection realConn = conn.getConn();
			
			String sqlStatement1 = "delete from acct_biz_unit_post_activity where acct_biz_unit_post_id=?";
			
			String sqlStatement2 = "delete from acct_biz_unit_post where id=?";
			
			String sqlStatement3 = "delete from acct_user_post where acct_biz_unit_post_id=?";

			
			JsonArray arg1 = new JsonArray();
			arg1.add(id);
		
			realConn.updateWithParams(sqlStatement1, arg1, handler1->{			
				  if(handler1.succeeded()){    	
					  realConn.updateWithParams(sqlStatement2, arg1, handler2->{						
						  if(handler2.succeeded()){  
							  realConn.updateWithParams(sqlStatement3, arg1, handler3->{						
								  if(handler3.succeeded()){    	
									  UpdateResult ret = handler3.result();								  
									  innerFuture.complete(ret);
								  }else{
									  Throwable err = handler3.cause();
									  err.printStackTrace();						  
									  innerFuture.fail(err);
								  }					
							});
						  }else{
							  Throwable err = handler2.cause();
							  err.printStackTrace();						  
							  innerFuture.fail(err);
						  }					
					});
				  }else{
					  Throwable err = handler1.cause();
					  err.printStackTrace();
					  innerFuture.fail(err);
				  }
				
			});
	}
	
	public void deleteActivity(Long post_activity_id, Handler<AsyncResult<UpdateResult>> done) {
		  Future<UpdateResult> retFuture = Future.future();
		  retFuture.setHandler(done);
		  
		   String sql = "delete from acct_biz_unit_post_activity where id=?";
		   JsonArray params = new JsonArray();
		   params.add(post_activity_id);
	
		  
		  this.deleteWithParams(sql, params, retFuture);	

	}
	
    public void getActivityList(Long postId, Future<ResultSet> future) {
        
	   final String sql = "SELECT * FROM view_acct_biz_unit_post_activity WHERE acct_biz_unit_post_id=?";
	   JsonArray params = new JsonArray();
	   params.add(postId);
	
	   this.queryWithParams(sql, params, future);

    	
    }
	

}
