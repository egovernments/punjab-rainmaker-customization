package org.egov.migrationkit.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.egov.migrationkit.rowmapper.SWDocumentRowmapper;
import org.egov.migrationkit.rowmapper.WSDocumentRowmapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swagger.client.model.CollectionPayment;
import io.swagger.client.model.LocalDocument;
import io.swagger.client.model.SewerageConnection;
import io.swagger.client.model.WaterConnection;

@Transactional
@Service
public class RecordService {
	
	private static final String MODULE_NAME_WATER = "water";
	
	private static final String MODULE_NAME_SEWERAGE = "sewerage";
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Value("${egov.services.water.document.limit}")
	private long fileLimit = 0l;

	@Transactional
	public boolean recordWaterMigration(WaterConnection conn, String tenantId) {
		String checkQuery = Sqls.WATER_MIGRATION_SELECT;
		checkQuery = checkQuery.replace(":schema", tenantId);
		checkQuery = checkQuery.replace(":status", "'Saved'");
		checkQuery = checkQuery.replace(":erpconn", "'" + conn.getConnectionNo() + "'");
		checkQuery = checkQuery.replace(":erpid", "'" + conn.getId() + "'");

		int size = jdbcTemplate.queryForObject(checkQuery, Integer.class);

		if(size >= 1)
			return Boolean.TRUE;

		String qry = Sqls.WATER_MIGRATION_INSERT;
		qry = qry.replace(":schema", tenantId);
		qry = qry.replace(":erpid", "'" + conn.getId() + "'");
		qry = qry.replace(":erpconn", "'" + conn.getConnectionNo() + "'");
		qry = qry.replace(":erppt", "'" + conn.getPropertyId() + "'");
		qry = qry.replace(":status", "'initiated'");
		qry = qry.replace(":tenantId", "'" + conn.getTenantId() + "'");
		qry = qry.replace(":digitconn", "'null'");
		qry = qry.replace(":digitpt", "'null'");
		qry = qry.replace(":addtionaldetails", "'null'");
		jdbcTemplate.execute(qry);
		
		return Boolean.FALSE;

	}

	@Transactional
	public void updateWaterMigration(WaterConnection conn, String erpId, String tenantId, String uuid) {

		String qry = Sqls.WATER_MIGRATION_UPDATE;
		qry = qry.replace(":schema", tenantId);
		qry = qry.replace(":erpid", "'" + erpId + "'");
		qry = qry.replace(":status", "'Saved'");
		qry = qry.replace(":tenantId", "'" + conn.getTenantId() + "'");
		String connNo = conn.getConnectionNo() == null ? conn.getApplicationNo() : conn.getConnectionNo();
		qry = qry.replace(":digitconn", "'" + connNo + "'");
		qry = qry.replace(":digitpt", "'" + conn.getPropertyId() + "'");
		jdbcTemplate.execute(qry);

		String sql2 = Sqls.ProcessContent;
		sql2 = sql2.replaceAll(":id", UUID.randomUUID().toString());
		sql2 = sql2.replaceAll(":tenantId", conn.getTenantId());
		sql2 = sql2.replaceAll(":businessService", "NewWS1");
		sql2 = sql2.replaceAll(":businessId", conn.getApplicationNo());

		sql2 = sql2.replaceAll(":moduleName", "ws-services");
		sql2 = sql2.replaceAll(":userUUID", uuid);
		long epoch = System.currentTimeMillis();
		sql2 = sql2.replaceAll(":epocnow", String.valueOf(epoch));

		String sql3 = Sqls.PROCESSINSERT;
		sql3 = sql3.replaceAll(":val", sql2);
		sql3 = sql3.replaceAll(":schema", tenantId);
		jdbcTemplate.execute(sql3);

	}

	@Transactional
	public void updateSewerageMigration(SewerageConnection conn, String erpId, String tenantId, String uuid) {

		String qry = Sqls.SEWERAGE_MIGRATION_UPDATE;
		qry = qry.replace(":schema_tenantId", tenantId);
		qry = qry.replace(":erpid", "'" + erpId + "'");
		qry = qry.replace(":status", "'Saved'");
		qry = qry.replace(":tenantId", "'" + conn.getTenantId() + "'");
		qry = qry.replace(":digitconn", conn.getConnectionNo() == null ? "'null'" : "'" + conn.getConnectionNo() + "'");
		qry = qry.replace(":digitpt", "'" + conn.getPropertyId() + "'");
		jdbcTemplate.execute(qry);
		String sql2 = Sqls.ProcessContent;
		sql2 = sql2.replaceAll(":id", UUID.randomUUID().toString());
		sql2 = sql2.replaceAll(":tenantId", conn.getTenantId());
		sql2 = sql2.replaceAll(":businessService", "NewSW1");
		sql2 = sql2.replaceAll(":businessId", conn.getApplicationNo());
		sql2 = sql2.replaceAll(":moduleName", "sw-services");
		sql2 = sql2.replaceAll(":userUUID", uuid);
		long epoch = System.currentTimeMillis();
		sql2 = sql2.replaceAll(":epocnow", String.valueOf(epoch));

		String sql3 = Sqls.PROCESSINSERT;
		sql3 = sql3.replaceAll(":schema", tenantId);

		sql3 = sql3.replaceAll(":val", sql2);

		jdbcTemplate.execute(sql3);

	}

	@Transactional
	public boolean recordSewerageMigration(SewerageConnection conn, String tenantId) {
		
		String checkQuery = Sqls.SEWERAGE_MIGRATION_SELECT;
		checkQuery = checkQuery.replace(":schema_tenantId", tenantId);
		checkQuery = checkQuery.replace(":status", "'Saved'");
		checkQuery = checkQuery.replace(":erpconn", "'" + conn.getConnectionNo() + "'");
		checkQuery = checkQuery.replace(":erpid", "'" + conn.getId() + "'");

		int size = jdbcTemplate.queryForObject(checkQuery, Integer.class);

		if(size >= 1)
			return Boolean.TRUE;

		String qry = Sqls.SEWERAGE_MIGRATION_INSERT;
		qry = qry.replace(":schema_tenantId", tenantId);
		qry = qry.replace(":erpid", "'" + conn.getId() + "'");
		qry = qry.replace(":erpconn", "'" + conn.getConnectionNo() + "'");
		qry = qry.replace(":erppt", "'" + conn.getPropertyId() + "'");
		qry = qry.replace(":status", "'initiated'");
		qry = qry.replace(":tenantId", "'" + conn.getTenantId() + "'");
		qry = qry.replace(":digitconn", "'null'");
		qry = qry.replace(":digitpt", "'null'");
		qry = qry.replace(":addtionaldetails", "'null'");
		jdbcTemplate.execute(qry);
		
		return Boolean.FALSE;

	}

	@Transactional
	public boolean recordWtrCollMigration(CollectionPayment conn, String tenantId) {
		String checkQuery = Sqls.WATER_COLLECTION_MIGRATION_SELECT;
		checkQuery = checkQuery.replace(":erpconn", "'" + conn.getConsumerCode() + "'");
		checkQuery = checkQuery.replace(":schema", tenantId);
		checkQuery = checkQuery.replace(":erpreceiptnumber", "'" + conn.getPaymentDetails().get(0).getReceiptNumber() + "'");
		checkQuery = checkQuery.replace(":status", "'Saved'");
		int size = jdbcTemplate.queryForObject(checkQuery, Integer.class);

		if(size >= 1)
			return Boolean.TRUE;

		String qry = Sqls.WATER_COLLECTION_MIGRATION_INSERT;
		qry = qry.replace(":schema", tenantId);

		qry = qry.replace(":erpreceiptnumber", "'" + conn.getPaymentDetails().get(0).getReceiptNumber() + "'");
		qry = qry.replace(":erpconn", "'" + conn.getConsumerCode() + "'");
		qry = qry.replace(":erppt", "'" + conn.getBusinessService() + "'");
		qry = qry.replace(":status", "'initiated'");
		qry = qry.replace(":tenantId", "'" + conn.getTenantId() + "'");
		qry = qry.replace(":digitreceiptnumber", "'null'");
		qry = qry.replace(":digitpt", "'null'");
		qry = qry.replace(":addtionaldetails", "'null'");
		jdbcTemplate.execute(qry);
		
		return Boolean.FALSE;

	}

	@Transactional
	public boolean recordSwgCollMigration(CollectionPayment conn, String tenantId) {
		String checkQuery = Sqls.SEWERAGE_COLLECTION_MIGRATION_SELECT;
		checkQuery = checkQuery.replace(":erpconn", "'" + conn.getConsumerCode() + "'");
		checkQuery = checkQuery.replace(":schema", tenantId);
		checkQuery = checkQuery.replace(":erpreceiptnumber", "'" + conn.getPaymentDetails().get(0).getReceiptNumber() + "'");
		checkQuery = checkQuery.replace(":status", "'Saved'");
		int size = jdbcTemplate.queryForObject(checkQuery, Integer.class);

		if(size >= 1)
			return Boolean.TRUE;


		String qry = Sqls.SEWERAGE_COLLECTION_MIGRATION_INSERT;
		qry = qry.replace(":schema", tenantId);
		qry = qry.replace(":erpreceiptnumber", "'" + conn.getPaymentDetails().get(0).getReceiptNumber() + "'");
		qry = qry.replace(":erpconn", "'" + conn.getConsumerCode() + "'");
		qry = qry.replace(":erppt", "'" + conn.getBusinessService() + "'");
		qry = qry.replace(":status", "'initiated'");
		qry = qry.replace(":tenantId", "'" + conn.getTenantId() + "'");
		qry = qry.replace(":digitreceiptnumber", "'null'");
		qry = qry.replace(":digitpt", "'null'");
		qry = qry.replace(":addtionaldetails", "'null'");
		jdbcTemplate.execute(qry);
		
		return Boolean.FALSE;

	}

	@Transactional
	public void updateWtrCollMigration(CollectionPayment conn, String tenantId, String receiptNumber) {

		String qry = Sqls.WATER_COLLECTION_MIGRATION_UPDATE;

		qry = qry.replace(":schema", tenantId);
		qry = qry.replace(":erpconn", "'" + conn.getConsumerCode() + "'");
		qry = qry.replace(":status", "'Saved'");
		qry = qry.replace(":tenantId", "'" + conn.getTenantId() + "'");
		qry = qry.replace(":digitreceiptnumber", "'" + receiptNumber + "'");
		qry = qry.replace(":digitpt", "'" + conn.getBusinessService() + "'");
		jdbcTemplate.execute(qry);

	}

	@Transactional
	public void updateSwgCollMigration(CollectionPayment conn, String tenantId, String receiptNumber) {

		String qry = Sqls.SEWERAGE_COLLECTION_UPDATE;
		qry = qry.replace(":schema", tenantId);
		qry = qry.replace(":erpconn", "'" + conn.getConsumerCode() + "'");
		qry = qry.replace(":status", "'Saved'");
		qry = qry.replace(":tenantId", "'" + conn.getTenantId() + "'");
		qry = qry.replace(":digitreceiptnumber", "'" + receiptNumber + "'" );
		qry = qry.replace(":digitpt", "'" + conn.getBusinessService() + "'");
		jdbcTemplate.execute(qry);

	}

	@Transactional
	public void initiate(String tenantId) {

		jdbcTemplate.execute("set search_path to " + tenantId);

		// jdbcTemplate.execute("drop table if exists egwtr_migration" +
		// tenantId);
		jdbcTemplate.execute(Sqls.WATER_MIGRATION_TABLE);
		jdbcTemplate.execute(Sqls.PROCESSINSERTTABLE);

		jdbcTemplate.execute("create sequence if not exists seq_mobilenumber");
	}

	@Transactional
	public void initiateSewrage(String tenantId) {
		jdbcTemplate.execute("set search_path to " + tenantId);
		// jdbcTemplate.execute("drop table if exists egwtr_migration" +
		// tenantId);
		jdbcTemplate.execute(Sqls.SEWERAGE_MIGRATION_TABLE);
		jdbcTemplate.execute(Sqls.PROCESSINSERTTABLE);
		jdbcTemplate.execute("create sequence if not exists seq_mobilenumber");
	}

	@Transactional
	public void initiateCollection(String tenantId) {
		jdbcTemplate.execute("set search_path to " + tenantId);
		// jdbcTemplate.execute("drop table if exists egwtr_migration" +
		// tenantId);
		jdbcTemplate.execute(Sqls.WATER_COLLECTION_MIGRATION_TABLE);
	}

	@Transactional
	public void recordError(String module, String tenantId, String message, String id) {
		String tableName = null;
		if (module.equalsIgnoreCase("water"))
			tableName = "egwtr_migration";
		else if (module.equalsIgnoreCase("sewerage"))
			tableName = "egswtax_migration";
		else if (module.equalsIgnoreCase("Wtrcollection"))
			tableName = "egwtr_cl_migration";
		else if (module.equalsIgnoreCase("Swcollection"))
			tableName = "egswtax_cl_migration";
		String cleanedMessage = "";
		if (message != null)
			cleanedMessage = message.replace("'", "");
		
		if(module.equalsIgnoreCase("Wtrcollection") || module.equalsIgnoreCase("Swcollection")) {
			jdbcTemplate.execute("update " + tenantId + "." + tableName + " set errorMessage='" + cleanedMessage
					+ "' where erpreceiptnumber='" + id + "'");
		}

		else {
		jdbcTemplate.execute("update " + tenantId + "." + tableName + " set errorMessage='" + cleanedMessage
				+ "' where erpid='" + id + "'");
		
		}

	}

	@Transactional
	public void setStatus(String module, String tenantId, String status, String id) {
		String tableName = null;
		if (module.equalsIgnoreCase("water"))
			tableName = "egwtr_migration";
		else if (module.equalsIgnoreCase("sewerage"))
			tableName = "egswtax_migration";
		jdbcTemplate.execute(
				"update " + tenantId + "." + tableName + " set status='" + status + "' where erpid='" + id + "'");

	}

	@Transactional
	public void setMob(String module, String tenantId, Long mob, String id) {
		String tableName = null;
		if (module.equalsIgnoreCase("water"))
			tableName = "egwtr_migration";
		else if (module.equalsIgnoreCase("sewerage"))
			tableName = "egswtax_migration";
		jdbcTemplate
				.execute("update " + tenantId + "." + tableName + " set mob='" + mob + "' where erpid='" + id + "'");

	}

	@Transactional
	public Long nextSequence(String tenantId) {
		jdbcTemplate.execute("set search_path to " + tenantId);
		Long no = jdbcTemplate.queryForObject("Select nextval('seq_mobilenumber') ", Long.class);
		return no;
	}

	public List<LocalDocument> getFiles(String connId, String module) {
		List<LocalDocument> documents = new ArrayList<>();
		if (module.equalsIgnoreCase("water")) {
			String sql = Sqls.WATERDOCUMENTSQL;
			sql = sql.replaceAll(":connId", connId);

			documents = jdbcTemplate.queryForList(sql, LocalDocument.class);

		}
		return documents;

	}
	
	public List<LocalDocument> getAllWSFilesByTenantId(String tenantId) {
		List<LocalDocument> documents = new ArrayList<>();
		jdbcTemplate.execute("set search_path to " + tenantId);
		String query = null;
		jdbcTemplate.execute(Sqls.WATER_DOCUMENTS_TABLE);
		query = Sqls.ALL_WATER_DOCUMENTS_QUERY;

		if (fileLimit > 0)
			query = query + " limit " + fileLimit;

		documents = jdbcTemplate.query(query, new WSDocumentRowmapper());
		return documents;
	}
	
	public List<LocalDocument> getAllSWFilesByTenantId(String tenantId) {
		List<LocalDocument> documents = new ArrayList<>();
		jdbcTemplate.execute("set search_path to " + tenantId);
		String query = null;
		jdbcTemplate.execute(Sqls.SEWERAGE_DOCUMENTS_TABLE);
		query = Sqls.ALL_SEWERAGE_DOCUMENTS_QUERY;

		if (fileLimit > 0)
			query = query + " limit " + fileLimit;

		documents = jdbcTemplate.query(query, new SWDocumentRowmapper());
		return documents;
	}
	public String getCityCodeByName(final String city) {
		return jdbcTemplate.queryForObject("select code from eg_city where lower(name)='" + city + "'", String.class);
	}

	public void saveMigratedFilestoreDetails(String oldFileStore, String newFileStore, String connectionNo,
			boolean success, String error, String tenantId) {

		String qry = Sqls.WATER_DOCUMENT_MIGRATION_INSERT;

		qry = qry.replace(":erpconn", "'" + connectionNo + "'");
		qry = qry.replace(":digitconn", "'" + connectionNo + "'");
		qry = qry.replace(":tenantId", "'" + tenantId + "'");
		qry = qry.replace(":erpfilestore", "'" + oldFileStore + "'");
		qry = qry.replace(":digitfilestore", "'" + newFileStore + "'");
		qry = qry.replace(":status", success ? "'SUCCESS'" : "'FAILED'");
		qry = qry.replace(":additiondetails", success ? "'Saved'" : "'Failed'");
		qry = qry.replace(":errorMessage", success ? "'null'" : "'" + error + "'");

		jdbcTemplate.execute(qry);

	}


}
