package org.egov.migrationkit.service;

public class Sqls {

	public static final String waterQuery="select json_build_object( 'actualPipeSize', (select sizeininch from egwtr_pipesize psize where conndetails.pipesize=psize.id), 'actualTaps', conndetails.nooftaps, 'proposedPipeSize', (select sizeininch from egwtr_pipesize psize where conndetails.pipesize=psize.id), 'proposedTaps', conndetails.nooftaps, 'cityname', (SELECT CASE WHEN name like '%UAT%' THEN (SELECT split_part(name,'-',1) from eg_city) ELSE (select name from eg_city) END from eg_city), 'zone', zone.name, 'consumercode', conn.consumercode, 'id', (select code from eg_city)||'-'||conndetails.id||'-WC', 'applicantname', usr.name, 'connectionstatus', conndetails.connectionstatus, 'createddate', to_char(conn.createddate::timestamp without time zone, 'YYYY-MM-DD'), 'propertytype', proptype.name, 'guardianname', usr.guardian, 'channel', CASE WHEN conndetails.source is not null THEN conndetails.source ELSE 'COUNTER' END, 'applicationtype', apptype.name, 'locality', locality.code, 'pwssb', CASE (select value from eg_appconfig_values where key_id in (select id from eg_appconfig where key_name ='IS_PWSSB_ULB')) WHEN 'YES' THEN true ELSE false END, 'block', block.name, 'citycode', (select code from eg_city), 'emailid', usr.emailid, 'connectiontype', (select contype.name from egwtr_connection_type contype where contype.id=conndetails.connectiontype), 'applicationnumber', conndetails.applicationnumber, 'disposaldate', conndetails.disposalDate, 'usage', usage.name, 'applicationdate', conndetails.applicationdate, 'districtname', (select districtname from eg_city), 'applicationstatus', status.description, 'applicantaddress.id', address.id, 'regionname', (select regionname from eg_city) , 'mobilenumber', usr.mobilenumber, 'category', wtrctgy.name, 'waterSource', (select source.code from egwtr_water_source source where source.id=conndetails.watersource), 'executiondate', (select extract(epoch from conndetails.executiondate) * 1000), 'dcb', (SELECT json_agg(dcb) FROM ( select to_char(inst.start_date, 'YYYY-MM-DD') \"from_date\", to_char(inst.end_date, 'YYYY-MM-DD') \"to_date\", inst.id \"insta_id\", d.is_history \"is_history\", drm.code \"demand_reason\", dd.amount \"amount\", dd.amt_collected \"collected_amount\", inst.financial_year \"financial_year\" from egwtr_demand_connection cdemand, eg_demand d, eg_demand_details dd, eg_installment_master inst, eg_demand_reason_master drm, eg_demand_reason dr where conndetails.id=cdemand.connectiondetails and cdemand.demand=d.id and d.id=dd.id_demand and dr.id_installment=inst.id and dd.id_demand_reason=dr.id and dr.id_demand_reason_master=drm.id and inst.id=dr.id_installment and d.is_history='N' order by inst.start_date ) dcb), 'road_category', (SELECT json_agg(road_category) FROM ( SELECT road_category.name \"road_name\", estimatedetails.area \"road_area\", estimatedetails.unitrate \"unitrate\", estimatedetails.amount \"amount\" from egwtr_estimation_details estimatedetails, egwtr_road_category road_category WHERE conndetails.id=estimatedetails.connectiondetailsid and estimatedetails.roadcategory=road_category.id ) road_category) ) from egwtr_connection conn, egwtr_connectiondetails conndetails, egwtr_application_type apptype, egwtr_usage_type usage, eg_boundary locality, eg_boundary zone, eg_boundary block, egwtr_property_type proptype, egwtr_category wtrctgy, egwtr_connection_owner_info ownerinfo, eg_user usr, eg_address address, egw_status status where conn.id=conndetails.connection and apptype.id=conndetails.applicationtype and usage.id=conndetails.usagetype and block.id=conn.block and locality.id=conn.locality and zone.id=conn.zone and conndetails.propertytype=proptype.id and conndetails.category=wtrctgy.id and ownerinfo.connection=conn.id and usr.id=ownerinfo.owner and address.id=conn.address and status.id=conndetails.statusid and conndetails.legacy=false offset 3 limit 10";
	
	
	public static final String waterRecord_table="create table  if not exists  egwtr_migration "
			+ " ( erpid varchar(64),erpconn varchar(64) ,digitconn varchar(64) ,erppt varchar(64),digitpt varchar(64),status varchar(64),tenantId varchar(64),additiondetails varchar(1000) );"
			+ "";
	
	public static final String waterRecord_insert="insert into  egwtr_migration  "
			+ "(erpid ,erpconn  ,digitconn  ,erppt ,digitpt ,status ,tenantId ,additiondetails ) values (:erpid,:erpconn,:digitconn,:erppt,:digitpt,:status"
			+ ", :tenantId,:addtionaldetails);";
	
	public static final String waterRecord_update="update  egwtr_migration  "+
			"set digitconn=:digitconn , digitpt=:digitpt,status=:status where erpconn=:erpconn and tenantId=:tenantId";
		
	public static final String address="select id,housenobldgapt as plotno,landmark,citytownvillage as city,district,arealocalitysector as region,state,country,pincode, buildingName,streetroadline as street from eg_address where id=:id ;";
	
	public static final String WATER_COLLECTION_TABLE="create table  if not exists  egwtr_cl_migration "
			+ " ( erpid varchar(64),erpconn varchar(64) ,digitconn varchar(64) ,erppt varchar(64),digitpt varchar(64),status varchar(64),tenantId varchar(64),additiondetails varchar(1000) );"
			+ "";
	
	public static final String WATER_COLLECTION_INSERT="insert into  egwtr_cl_migration  "
			+ "(erpid ,erpconn  ,digitconn  ,erppt ,digitpt ,status ,tenantId ,additiondetails ) values (:erpid,:erpconn,:digitconn,:erppt,:digitpt,:status"
			+ ", :tenantId,:addtionaldetails);";
	
	public static final String WATER_COLLECTION_UPDATE="update  egwtr_cl_migration  "+
			"set digitconn=:digitconn , digitpt=:digitpt,status=:status where erpconn=:erpconn and tenantId=:tenantId";
	
	
	
	public static final String sewerageQuery="select json_build_object(\n"
			+ "'cityname', (SELECT CASE WHEN name like '%UAT%' THEN (SELECT split_part(name,'-',1) from eg_city) ELSE (select name from eg_city) END from eg_city),\n"
			+ "'zone', zone.name,\n"
			+ "'consumercode', conn.shsc_number,\n"
			+ "'id', (select code from eg_city)||'-'||conndetails.id||'-SC',\n"
			+ "'applicantname', usr.name,\n"
			+ "'connectionstatus', conn.status,\n"
			+ "'createddate', to_timestamp(to_char(conn.createddate::timestamp without time zone, 'YYYY-MM-DD'),'YYYY-MM-DDTHH24:MI:SSZ'),\n"
			+ "'servicetype', 'Sewerage Charges',\n"
			+ "'autoverifieddate', (select lastmodifieddate from eg_wf_state_history statehist where statehist.state_id=appdetails.state_id and lastmodifiedby=(select id from eg_user where username='system')),\n"
			+ "'guardianname', usr.guardian,\n"
			+ "'channel', CASE WHEN appdetails.source is not null THEN appdetails.source ELSE 'COUNTER' END,\n"
			+ "'applicationtype', apptype.name,\n"
			+ "'locality', locality.name,\n"
			+ "'block', block.name,\n"
			+ "'citycode', (select code from eg_city),\n"
			+ "'emailid', usr.emailid,\n"
			+ "'applicationnumber', appdetails.applicationnumber,\n"
			+ "'disposaldate', appdetails.disposalDate,\n"
			+ "'usage', usage.name,\n"
			+ "'applicationdate', appdetails.applicationdate,\n"
			+ "'districtname', (select districtname from eg_city),\n"
			+ "'applicationstatus', status.description,\n"
			+ "'mobilenumber', usr.mobilenumber,\n"
			+ "'citygrade', (select grade from eg_city),\n"
			+ "'noofseatsresidential',conndetails.noofclosets_residential,\n"
			+ "'noofseatsnonresidential', conndetails.noofclosets_nonresidential,\n"
			+ "'doorno', replace(address.housenobldgapt,'/','\\'))\n"
			+ "from egswtax_connection conn, egswtax_connectiondetail conndetails,\n"
			+ "egswtax_applicationdetails appdetails,\n"
			+ "egswtax_application_type apptype,\n"
			+ "egswtax_usage_type usage,\n"
			+ "eg_boundary locality, eg_boundary zone,\n"
			+ "eg_boundary block, egswtax_connection_owner_info ownerinfo,\n"
			+ "eg_user usr, eg_address address, egw_status status\n"
			+ "where appdetails.connection=conn.id and\n"
			+ "appdetails.connectiondetail=conndetails.id and\n"
			+ "apptype.id=appdetails.applicationtype and\n"
			+ "usage.id=conndetails.usagetype and block.id=conn.block and\n"
			+ "locality.id=conn.locality and zone.id=conn.zone and\n"
			+ "ownerinfo.connection=conn.id and usr.id=ownerinfo.owner and\n"
			+ "address.id=conn.address and status.id=appdetails.status and conn.legacy=false ;";
	
	public static final String SEWERAGE_CONNECTION_TABLE="";
//	
//	public static final String sewerageRecord_table="create table  if not exists  egswr_migration "
//			+ " ( erpid varchar(64),erpconn varchar(64) ,digitconn varchar(64) ,erppt varchar(64),digitpt varchar(64),status varchar(64),tenantId varchar(64),additiondetails varchar(1000) );"
//			+ "";
//	
//	public static final String sewerageRecord_insert="insert into  egsw_migration  "
//			+ "(erpid ,erpconn  ,digitconn  ,erppt ,digitpt ,status ,tenantId ,additiondetails ) values (:erpid,:erpconn,:digitconn,:erppt,:digitpt,:status"
//			+ ", :tenantId,:addtionaldetails);";
//	
//	public static final String sewerageRecord_update="update  egsw_migration  "+
//			"set digitconn=:digitconn , digitpt=:digitpt,status=:status where erpconn=:erpconn and tenantId=:tenantId";
//		
//	public static final String address="select id,housenobldgapt as plotno,landmark,citytownvillage as city,district,arealocalitysector as region,state,country,pincode, buildingName,streetroadline as street from eg_address where id=:id ;";
//	
//	public static final String SEWERAGE_COLLECTION_TABLE="create table  if not exists  egwtr_cl_migration "
//			+ " ( erpid varchar(64),erpconn varchar(64) ,digitconn varchar(64) ,erppt varchar(64),digitpt varchar(64),status varchar(64),tenantId varchar(64),additiondetails varchar(1000) );"
//			+ "";
//	
//	public static final String SEWERAGE_COLLECTION_INSERT="insert into  egwtr_cl_migration  "
//			+ "(erpid ,erpconn  ,digitconn  ,erppt ,digitpt ,status ,tenantId ,additiondetails ) values (:erpid,:erpconn,:digitconn,:erppt,:digitpt,:status"
//			+ ", :tenantId,:addtionaldetails);";
//	
//	public static final String SEWERAGE_COLLECTION_UPDATE="update  egsw_cl_migration  "+
//			"set digitconn=:digitconn , digitpt=:digitpt,status=:status where erpconn=:erpconn and tenantId=:tenantId";
	
}
