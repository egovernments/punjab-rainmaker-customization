package org.egov.migrationkit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swagger.client.model.WaterConnection;
@Transactional
@Service
public class RecordService {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
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
	
	public void updateWaterMigration(WaterConnection conn)
	{
		
	String qry=	Sqls.waterRecord_update;
	qry=qry.replace(":erpconn", "'"+conn.getConnectionNo()+"'");
	qry=qry.replace(":status", "'saved'");
	qry=qry.replace(":tenantId", "'"+conn.getTenantId()+"'");
	qry=qry.replace(":digitconn", conn.getConnectionNo()==null?"'null'":conn.getConnectionNo());
	qry=qry.replace(":digitpt","'"+conn.getPropertyId()+"'");
	jdbcTemplate.execute(qry);
		
	}
	
	public void recordWtrCollMigration(WaterConnection conn)
	{
		
	String qry=	Sqls.WATER_COLLECTION_INSERT;
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
	
	public void updateWtrCollMigration(WaterConnection conn)
	{
		
	String qry=	Sqls.WATER_COLLECTION_UPDATE;
	qry=qry.replace(":erpconn", "'"+conn.getConnectionNo()+"'");
	qry=qry.replace(":status", "'saved'");
	qry=qry.replace(":tenantId", "'"+conn.getTenantId()+"'");
	qry=qry.replace(":digitconn", conn.getConnectionNo()==null?"'null'":conn.getConnectionNo());
	qry=qry.replace(":digitpt","'"+conn.getPropertyId()+"'");
	jdbcTemplate.execute(qry);
		
	}


}
