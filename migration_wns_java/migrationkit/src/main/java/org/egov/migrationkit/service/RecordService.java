package org.egov.migrationkit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swagger.client.model.CollectionPayment;
import io.swagger.client.model.WaterConnection;
@Transactional
@Service
public class RecordService {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Transactional
	public void recordWaterMigration(WaterConnection conn)
	{
		
	String qry=	Sqls.waterRecord_insert;
	qry=qry.replace(":erpid", "'"+conn.getId()+"'");
	qry=qry.replace(":erpconn", "'"+conn.getConnectionNo()+"'");
	qry=qry.replace(":erppt", "'"+conn.getPropertyId()+"'");
	qry=qry.replace(":status", "'pushed'");
	qry=qry.replace(":tenantId", "'"+conn.getTenantId()+"'");
	qry=qry.replace(":digitconn", "'null'");
	qry=qry.replace(":digitpt", "'null'");
	qry=qry.replace(":addtionaldetails", "'null'");
	jdbcTemplate.execute(qry); 
		
	}
	@Transactional
	public void updateWaterMigration(WaterConnection conn)
	{
		
	String qry=	Sqls.waterRecord_update;
	qry=qry.replace(":erpid", "'"+conn.getId()+"'");
	qry=qry.replace(":status", "'saved'");
	qry=qry.replace(":tenantId", "'"+conn.getTenantId()+"'");
	qry=qry.replace(":digitconn", conn.getConnectionNo()==null?"'null'":"'"+conn.getConnectionNo()+"'");
	qry=qry.replace(":digitpt","'"+conn.getPropertyId()+"'");
	jdbcTemplate.execute(qry);
		
	}
	@Transactional
	public void recordWtrCollMigration(CollectionPayment conn)
	{
		
	String qry=	Sqls.WATER_COLLECTION_INSERT;
	qry=qry.replace(":erpid", "'"+conn.getId()+"'");
	qry=qry.replace(":erpconn", "'"+conn.getConsumerCode()+"'");
	qry=qry.replace(":erppt", "'"+conn.getBusinessService()+"'");
	qry=qry.replace(":status", "'pushed'");
	qry=qry.replace(":tenantId", "'"+conn.getTenantId()+"'");
	qry=qry.replace(":digitconn", "'null'");
	qry=qry.replace(":digitpt", "'null'");
	qry=qry.replace(":addtionaldetails", "'null'");
	jdbcTemplate.execute(qry); 
		
	}
	@Transactional
	public void updateWtrCollMigration(CollectionPayment conn)
	{
		
	String qry=	Sqls.WATER_COLLECTION_UPDATE;
	qry=qry.replace(":erpconn", "'"+conn.getConsumerCode()+"'");
	qry=qry.replace(":status", "'saved'");
	qry=qry.replace(":tenantId", "'"+conn.getTenantId()+"'");
	qry=qry.replace(":digitconn", conn.getConsumerCode()==null?"'null'":conn.getConsumerCode());
	qry=qry.replace(":digitpt","'"+conn.getBusinessService()+"'");
	jdbcTemplate.execute(qry);
		
	}
	@Transactional
	public void initiate(String tenantId)
	{
		
		
		
	jdbcTemplate.execute("set search_path to " + tenantId);
	//jdbcTemplate.execute("drop table if exists egwtr_migration" + tenantId);
	jdbcTemplate.execute(Sqls.waterRecord_table);
	}
	public void recordError(String module,String message, String id) {
		String tableName=null;
		if(module.equalsIgnoreCase("water"))
			tableName="egwtr_migration";
		jdbcTemplate.execute("update "+tableName+" set error= '" +message+"' where erpid='"+id+"'"); 
		
	} 

}
