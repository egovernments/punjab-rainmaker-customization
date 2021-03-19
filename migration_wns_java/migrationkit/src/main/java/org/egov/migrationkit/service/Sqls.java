package org.egov.migrationkit.service;

public class Sqls {

	public static final String waterQuery="select json_build_object( 'actualPipeSize', (select sizeininch from egwtr_pipesize psize where conndetails.pipesize=psize.id), 'actualTaps', conndetails.nooftaps, 'proposedPipeSize', (select sizeininch from egwtr_pipesize psize where conndetails.pipesize=psize.id), 'proposedTaps', conndetails.nooftaps, 'cityname', (SELECT CASE WHEN name like '%UAT%' THEN (SELECT split_part(name,'-',1) from eg_city) ELSE (select name from eg_city) END from eg_city), 'zone', zone.name, 'connectionNo', conn.consumercode, 'id', (select code from eg_city)||'-'||conndetails.id||'-WC', 'applicantname', usr.name, 'connectionstatus', conndetails.connectionstatus, 'createddate', to_char(conn.createddate::timestamp without time zone, 'YYYY-MM-DD'), 'connectionCategory', proptype.name, 'guardianname', usr.guardian, 'channel', CASE WHEN conndetails.source is not null THEN conndetails.source ELSE 'COUNTER' END, 'applicationtype', apptype.name, 'locality', locality.code, 'pwssb', CASE (select value from eg_appconfig_values where key_id in (select id from eg_appconfig where key_name ='IS_PWSSB_ULB')) WHEN 'YES' THEN true ELSE false END, 'block', block.name, 'citycode', (select code from eg_city), 'emailid', usr.emailid, 'connectionType', (select CASE WHEN contype.name like '%Non%' THEN 'Non Metered' ELSE 'Metered' END from egwtr_connection_type contype where contype.id=conndetails.connectiontype), 'applicationnumber', conndetails.applicationnumber, 'disposaldate', conndetails.disposalDate, 'usage', usage.name, 'applicationdate', conndetails.applicationdate, 'districtname', (select districtname from eg_city), 'applicationstatus', status.description, 'applicantaddress.id', address.id, 'regionname', (select regionname from eg_city) , 'mobilenumber', usr.mobilenumber, 'category', wtrctgy.name, 'waterSource', (select source.code from egwtr_water_source source where source.id=conndetails.watersource), 'connectionExecutionDate', (select extract(epoch from conndetails.executiondate) * 1000), 'dcb', (SELECT json_agg(dcb) FROM ( select to_char(inst.start_date, 'YYYY-MM-DD') \"from_date\", to_char(inst.end_date, 'YYYY-MM-DD') \"to_date\", inst.id \"insta_id\", d.is_history \"is_history\", drm.code \"demand_reason\", dd.amount \"amount\", dd.amt_collected \"collected_amount\", inst.financial_year \"financial_year\" from egwtr_demand_connection cdemand, eg_demand d, eg_demand_details dd, eg_installment_master inst, eg_demand_reason_master drm, eg_demand_reason dr where conndetails.id=cdemand.connectiondetails and cdemand.demand=d.id and d.id=dd.id_demand and dr.id_installment=inst.id and dd.id_demand_reason=dr.id and dr.id_demand_reason_master=drm.id and inst.id=dr.id_installment and d.is_history='N' order by inst.start_date ) dcb), 'road_category', (SELECT json_agg(road_category) FROM ( SELECT road_category.name \"road_name\", estimatedetails.area \"road_area\", estimatedetails.unitrate \"unitrate\", estimatedetails.amount \"amount\" from egwtr_estimation_details estimatedetails, egwtr_road_category road_category WHERE conndetails.id=estimatedetails.connectiondetailsid and estimatedetails.roadcategory=road_category.id ) road_category) ) from egwtr_connection conn, egwtr_connectiondetails conndetails, egwtr_application_type apptype, egwtr_usage_type usage, eg_boundary locality, eg_boundary zone, eg_boundary block, egwtr_property_type proptype, egwtr_category wtrctgy, egwtr_connection_owner_info ownerinfo, eg_user usr, eg_address address, egw_status status where conn.id=conndetails.connection and apptype.id=conndetails.applicationtype and usage.id=conndetails.usagetype and block.id=conn.block and locality.id=conn.locality and zone.id=conn.zone and conndetails.propertytype=proptype.id and conndetails.category=wtrctgy.id and ownerinfo.connection=conn.id and usr.id=ownerinfo.owner and address.id=conn.address and status.id=conndetails.statusid ";
	
   public static final String waterQueryold="select json_build_object( 'actualPipeSize', (select sizeininch from egwtr_pipesize psize where conndetails.pipesize=psize.id), 'actualTaps', conndetails.nooftaps, 'proposedPipeSize', (select sizeininch from egwtr_pipesize psize where conndetails.pipesize=psize.id), 'proposedTaps', conndetails.nooftaps, 'cityname', (SELECT CASE WHEN name like '%UAT%' THEN (SELECT split_part(name,'-',1) from eg_city) ELSE (select name from eg_city) END from eg_city), 'zone', zone.name, 'consumercode', conn.consumercode, 'id', (select code from eg_city)||'-'||conndetails.id||'-WC', 'applicantname', usr.name, 'connectionstatus', conndetails.connectionstatus, 'createddate', to_char(conn.createddate::timestamp without time zone, 'YYYY-MM-DD'), 'propertytype', proptype.name, 'guardianname', usr.guardian, 'channel', CASE WHEN conndetails.source is not null THEN conndetails.source ELSE 'COUNTER' END, 'applicationtype', apptype.name, 'locality', locality.code, 'pwssb', CASE (select value from eg_appconfig_values where key_id in (select id from eg_appconfig where key_name ='IS_PWSSB_ULB')) WHEN 'YES' THEN true ELSE false END, 'block', block.name, 'citycode', (select code from eg_city), 'emailid', usr.emailid, 'connectiontype', (select contype.name from egwtr_connection_type contype where contype.id=conndetails.connectiontype), 'applicationnumber', conndetails.applicationnumber, 'disposaldate', conndetails.disposalDate, 'usage', usage.name, 'applicationdate', conndetails.applicationdate, 'districtname', (select districtname from eg_city), 'applicationstatus', status.description, 'applicantaddress.id', address.id, 'regionname', (select regionname from eg_city) , 'mobilenumber', usr.mobilenumber, 'category', wtrctgy.name, 'waterSource', (select source.code from egwtr_water_source source where source.id=conndetails.watersource), 'executiondate', (select extract(epoch from conndetails.executiondate) * 1000), 'dcb', (SELECT json_agg(dcb) FROM ( select to_char(inst.start_date, 'YYYY-MM-DD') \"from_date\", to_char(inst.end_date, 'YYYY-MM-DD') \"to_date\", inst.id \"insta_id\", d.is_history \"is_history\", drm.code \"demand_reason\", dd.amount \"amount\", dd.amt_collected \"collected_amount\", inst.financial_year \"financial_year\" from egwtr_demand_connection cdemand, eg_demand d, eg_demand_details dd, eg_installment_master inst, eg_demand_reason_master drm, eg_demand_reason dr where conndetails.id=cdemand.connectiondetails and cdemand.demand=d.id and d.id=dd.id_demand and dr.id_installment=inst.id and dd.id_demand_reason=dr.id and dr.id_demand_reason_master=drm.id and inst.id=dr.id_installment and d.is_history='N' order by inst.start_date ) dcb), 'road_category', (SELECT json_agg(road_category) FROM ( SELECT road_category.name \"road_name\", estimatedetails.area \"road_area\", estimatedetails.unitrate \"unitrate\", estimatedetails.amount \"amount\" from egwtr_estimation_details estimatedetails, egwtr_road_category road_category WHERE conndetails.id=estimatedetails.connectiondetailsid and estimatedetails.roadcategory=road_category.id ) road_category) ) from egwtr_connection conn, egwtr_connectiondetails conndetails, egwtr_application_type apptype, egwtr_usage_type usage, eg_boundary locality, eg_boundary zone, eg_boundary block, egwtr_property_type proptype, egwtr_category wtrctgy, egwtr_connection_owner_info ownerinfo, eg_user usr, eg_address address, egw_status status where conn.id=conndetails.connection and apptype.id=conndetails.applicationtype and usage.id=conndetails.usagetype and block.id=conn.block and locality.id=conn.locality and zone.id=conn.zone and conndetails.propertytype=proptype.id and conndetails.category=wtrctgy.id and ownerinfo.connection=conn.id and usr.id=ownerinfo.owner and address.id=conn.address and status.id=conndetails.statusid and conndetails.legacy=false   and consumercode not in (select erpconn from egwtr_migration where status='Saved')";
	
	
	public static final String waterQueryFormatted="select json_build_object ( 'pipeSize', (select sizeininch from egwtr_pipesize psize where conndetails.pipesize=psize.id), "+
			"  'noOfTaps'	, conndetails.nooftaps	,	 "+
			"  'proposedPipeSize'	, (select sizeininch from egwtr_pipesize psize where conndetails.pipesize=psize.id)		, "+
			"  'proposedTaps'	, conndetails.nooftaps	,	 "+
			"  'cityname',	 (SELECT CASE WHEN name like '%UAT%' THEN (SELECT split_part(name	,'-',	1) from eg_city) ELSE (select name from eg_city) END from eg_city), "+
		    "   'cityCode', (select code from eg_city),\n"+
			"  'zone',	 zone.name		, "+
			"  'connectionNo'	, conn.consumercode		, "+
			"  'id'	, conndetails.id	, "+
			"  'applicantname'	, usr.name		, "+
			"  'connectionStatus',	 conndetails.connectionstatus	,	 "+
			"  'createddate',	 to_char(conn.createddate::timestamp without time zone	, 'YYYY-MM-DD')	, "+
			"  'propertyType'	, proptype.name		, "+
			"  'guardianname',	 usr.guardian	,	 "+
			"  'channel'	, CASE WHEN conndetails.source is not null THEN conndetails.source ELSE 'COUNTER' END	,	 "+
			"  'applicationType',	 apptype.name	,	 "+
			"  'billingType',	 conndetails.billingtype	,	 "+
			"  'billingAmount',	 conndetails.billamount ,	"+
			"	'estimationLetterDate', conndetails.estimationnoticedate,"+
			"	'estimationFileStoreId',conndetails.estimationnoticefilestoreid,"+
			"	'averageMeterReading',conndetails.averagemeterreading,"+
			"	'initialMeterReading',mtr.initialreading,"+
			"   'meterId',mtr.meterserialnumber,"+
			"	'meterMake',mtr.metermake ,"+
			"	'othersFee','null', "+
			"	'ledgerId',conndetails.ledgernumber,"+
			"  'locality',	 locality.code	,	 "+
			"  'pwssb'	, CASE (select value from eg_appconfig_values where key_id in (select id from eg_appconfig where key_name ='IS_PWSSB_ULB')) WHEN 'YES' THEN true ELSE false END	,	 "+
			"  'block',	 block.name		, "+
			"  'citycode'	, (select code from eg_city)	,	 "+
			"  'emailId',	 usr.emailid	,	 "+
			"  'connectionType'	, (select contype.name from egwtr_connection_type contype where contype.id=conndetails.connectiontype)	,	 "+
			"  'applicationNumber'	, conndetails.applicationnumber	,	 "+
			"  'disposaldate'	, conndetails.disposalDate	,	 "+
			"  'usage',	 usage.name	,	 "+
			"  'applicationdate',	 conndetails.applicationdate	,	 "+
			"  'districtname'	, (select districtname from eg_city)	,	 "+
			"  'applicationstatus'	, status.description	,	 "+
			"  'applicantaddress.id',	 address.id		, "+
			"  'regionname'	, (select regionname from eg_city) 	,	 "+
			"  'mobilenumber'	, usr.mobilenumber	,	 "+
			"  'connectionCategory',	 wtrctgy.name	,	 "+
			"  'waterSource'	, (select source.code from egwtr_water_source source where source.id=conndetails.watersource)	,	 "+
			"  'connectionExecutionDate'	, (select extract(epoch from conndetails.executiondate) * 1000)		, "+
			"  'dcb',	 (SELECT json_agg(dcb) FROM ( select to_char(inst.start_date	,	 "+
			"  'YYYY-MM-DD') \"from_date\"	,		 "+
			"  to_char(inst.end_date	,		 "+
			"  'YYYY-MM-DD') \"to_date\"	,		 "+
			"  inst.id \"insta_id\"		,	 "+
			"  d.is_history \"is_history\"		,	 "+
			"  drm.code \"demand_reason\"	,		 "+
			"  dd.amount \"amount\"			, "+
			"  dd.amt_collected \"collected_amount\"		,	 "+
			"  inst.financial_year \"financial_year\" from egwtr_demand_connection cdemand	,		 "+
			"  eg_demand d		,	 "+
			"  eg_demand_details dd	,		 "+
			"  eg_installment_master inst	,		 "+
			"  eg_demand_reason_master drm		,	 "+
			"  eg_demand_reason dr where conndetails.id=cdemand.connectiondetails and cdemand.demand=d.id and d.id=dd.id_demand "+
		    " and dr.id_installment=inst.id and dd.id_demand_reason=dr.id and dr.id_demand_reason_master=drm.id and inst.id=dr.id_installment "+
		    " and d.is_history='N' order by inst.start_date ) dcb )	,		 "+
			"  'road_category'	,		 "+
			"  (SELECT json_agg(road_category) FROM ( SELECT road_category.name \"road_name\"		,	 "+
			"  estimatedetails.area \"road_area\"	,		 "+
			"  estimatedetails.unitrate \"unitrate\"	,		 "+
			"  estimatedetails.amount \"amount\"  from egwtr_estimation_details estimatedetails	,		 "+
			"  egwtr_road_category road_category WHERE conndetails.id=estimatedetails.connectiondetailsid and " +
		    " estimatedetails.roadcategory=road_category.id ) road_category ) ) from egwtr_connection conn	,		 "+
			"  egwtr_connectiondetails conndetails left outer join egwtr_meter_details mtr on mtr.connectiondetails=conndetails.id	,		 "+
			"  egwtr_application_type apptype	,		 "+
			"  egwtr_usage_type usage		,	 "+
			"  eg_boundary locality		,	 "+
			"  eg_boundary zone		,	 "+
			"  eg_boundary block	,		 "+
			"  egwtr_property_type proptype		,	 "+
			"  egwtr_category wtrctgy	,		 "+
			"  egwtr_connection_owner_info ownerinfo	,		 "+
			"  eg_user usr	,		 "+
			"  eg_address address	,	 	 "+ 
			"  egw_status status where conn.id=conndetails.connection and apptype.id=conndetails.applicationtype and"
			+ " usage.id=conndetails.usagetype and block.id=conn.block and locality.id=conn.locality and zone.id=conn.zone and"
			+ " conndetails.propertytype=proptype.id and conndetails.category=wtrctgy.id and ownerinfo.connection=conn.id "
			+ " and usr.id=ownerinfo.owner and address.id=conn.address and status.id=conndetails.statusid   "
			+ " and conndetails.id not in (select erpid::bigint from egwtr_migration where status in "
			+ "('Saved','Demand_Created' ) )  "
			+ " order by conndetails.id limit 1000 ; ";   
	
	
	//public static final String ledgerId= "Select ledgerid as ledgerId from  egwtr_stg_connection;";
	//public static final String meterMake= "Select metermake as meterMake from egwtr_meter_details;";
	//public static final String initialMeterReading= "Select initialmeterreading as initialMeterReading from egwtr_meter_details;";
	//public static final String othersFee= "Select otherfee as othersFee from egwtr_connection_conversion_type;";
	//public static final String connectionCategory= "Select connectiontype as connectionCategory from egwtr_connectiondetails;";
	//public static final String billingType= "Select billingtype as billingType from egwtr_connectiondetails;";
	//public static final String billingAmount= "Select billamount as billingAmount from egwtr_connectiondetails;";
	//public static final String estimationLetterDate= "Select estimationnoticedate as estimationLetterDate from egwtr_connectiondetails;";
	//public static final String estimationFileStoreId= "Select estimationnoticefilestoreid as estimationFileStoreId   from egwtr_connectiondetails;";
	//public static final String averageMake= "Select averagemeterreading as averageMake from egwtr_connectiondetails;";
	
	public static final String waterRecord_table="create table if not exists egwtr_migration "
			+ " ( erpid varchar(64),erpconn varchar(64) ,digitconn varchar(64) ,erppt varchar(64),digitpt varchar(64),status varchar(64),tenantId varchar(64),additiondetails varchar(1000),errorMessage varchar(4000), mob varchar(11) );"
			+ "";
	
	public static final String waterRecord_insert="insert into :schema.egwtr_migration "
			+ "(erpid ,erpconn  ,digitconn  ,erppt ,digitpt ,status ,tenantId ,additiondetails ) values (:erpid,:erpconn,:digitconn,:erppt,:digitpt,:status"
			+ ", :tenantId,:addtionaldetails);";
	
	public static final String waterRecord_update="update  :schema.egwtr_migration  "+
			"set digitconn=:digitconn , digitpt=:digitpt,status=:status where erpid=:erpid ";
		
	public static final String address="select id,housenobldgapt as plotno,landmark,citytownvillage as city,district,arealocalitysector as region,state,country,pincode, buildingName,streetroadline as street from :schema_tenantId.eg_address where id=:id ;";
	

	
	public static final String WATER_COLLECTION_TABLE="create table  if not exists  egwtr_cl_migration(erpid varchar(64),erpconn varchar(64) ,digitconn varchar(64) ,erppt varchar(64),digitpt varchar(64),status varchar(64),tenantId varchar(64),additiondetails varchar(1000),errorMessage varchar(4000) );"
			+ "";
	
	public static final String WATER_COLLECTION_INSERT="insert into  :schema.egwtr_cl_migration(erpid ,erpconn  ,digitconn  ,erppt ,digitpt ,status ,tenantId ,additiondetails ) values (:erpid,:erpconn,:digitconn,:erppt,:digitpt,:status"
			+ ", :tenantId,:addtionaldetails)";
	
	public static final String WATER_COLLECTION_UPDATE="update  :schema.egwtr_cl_migration"+
			" set digitconn=:digitconn , digitpt=:digitpt,status=:status where erpconn=:erpconn and tenantId=:tenantId";
		
	public static final String WATER_COLLECTION_QUERY = "select json_build_object( 'paymentMode','cash', "
			+ "'paymentStatus', 'New', 'businessService', 'WS', 'transactionNumber',ih.transactionnumber, 'transactionDate', "
			+ " (select extract(epoch from ih.transactiondate) * 1000), 'paidBy', ch.payeename, 'mobileNumber', owner.mobilenumber, "
			+ " 'payerName', owner.name, 'consumerCode', ch.consumercode, 'payerEmail', owner.emailid, 'payerId', owner.id, "
			+ " 'totalAmountPaid', ch.totalamount, 'totalDue', ch.totalamount, 'instrumentDate',"
			+ " (select extract(epoch from ih.instrumentdate) * 1000) , 'instrumentNumber', ih.instrumentNumber, 'instrumentStatus',"
			+ " status.code, 'paymentDetails', (json_agg(json_build_object('totalDue', ch.totalamount, 'totalAmountPaid' , ch.totalamount,"
			+ " 'businessService', 'WS')ORDER BY ch.id ) ) ) as payments_info from egcl_collectionheader ch INNER JOIN egcl_servicedetails"
			+ " billingservice ON ch.servicedetails=billingservice.id and billingservice.code !='STAX' INNER JOIN egcl_collectioninstrument "
			+ " ci ON ch.id=ci.collectionheader INNER JOIN egf_instrumentheader ih ON ci.instrumentheader=ih.id INNER JOIN egf_instrumenttype "
			+ " it ON ih.instrumenttype=it.id INNER JOIN egw_status status ON ch.status=status.id and ch.status in (select id from egw_status"
			+ "  where moduletype='ReceiptHeader' and code not in ('PENDING','FAILED')) INNER JOIN egwtr_connection wtrcon ON "
			+ " ch.consumercode=wtrcon.consumercode INNER JOIN egwtr_connection_owner_info connowner ON wtrcon.id=connowner.connection "
			+ " INNER JOIN egwtr_connectiondetails conndetails ON wtrcon.id=conndetails.connection INNER JOIN eg_user owner "
			+ " ON owner.id=connowner.owner"
			+ " and conndetails.id in (select erpid::bigint from egwtr_migration where status in ('Demand_Created') )"
			+ " and wtrcon.consumercode not in (select erpconn from egwtr_cl_migration where status  in ('Saved') )"
			+ " GROUP BY it.type,ih.transactionnumber, ih.transactiondate, ch.payeename, owner.mobilenumber, "
			+ " owner.name, ch.consumercode, owner.emailid, owner.id,ch.totalamount, ih.instrumentdate, ih.instrumentNumber,status.code";
				
	
	public static final String sewerageQuery="select json_build_object("
			+ "'cityname', (SELECT CASE WHEN name like '%UAT%' THEN (SELECT split_part(name,'-',1) from eg_city) ELSE (select name from eg_city) END from eg_city),\n"
			+ "'zone', zone.name,\n"
			+ "'cityCode', (select code from eg_city),\n"
			+ "'connectionNo', conn.shsc_number,\n"
			+ "'id', conndetails.id,\n"
			+ "'applicantname', usr.name,\n"
			+ "'connectionstatus', conn.status,\n"
			+ "'createddate', to_timestamp(to_char(conn.createddate::timestamp without time zone, 'YYYY-MM-DD'),'YYYY-MM-DDTHH24:MI:SSZ'),\n"
			+ "'servicetype', 'Sewerage Charges',\n"
			+ "'autoverifieddate', (select lastmodifieddate from eg_wf_state_history statehist where statehist.state_id=appdetails.state_id and lastmodifiedby IN (select id from eg_user where username='system') LIMIT 1),\n"
			+ "'guardianname', usr.guardian,\n"
			+ "'channel', CASE WHEN appdetails.source is not null THEN appdetails.source ELSE 'COUNTER' END,\n"
			+ "'applicationtype', apptype.name,\n"
			+ "'billingType',	 conndetails.billingtype	,\n"
			+ "'locality', locality.code,\n"
			+ "'billingAmount',	 conndetails.billamount ,\n"	
			+ " 'estimationLetterDate', appdetails.estimationdate,\n"
			+ " 'estimationFileStoreId',appdetails.filestoreid, \n"
			+  "'othersFee','null', \n"
			+  "'ledgerId',conndetails.ledgernumber,\n"
			+ "'block', block.name,\n"
			+ "'citycode', (select code from eg_city),\n"
			+ "'emailid', usr.emailid,\n"
			+ "'applicatioNumber', appdetails.applicationnumber,\n"
			+ "'disposaldate', appdetails.disposalDate,\n"
			+ "'usage', usage.name,\n"
			+ "'applicationdate', appdetails.applicationdate,\n"
			+ "'districtname', (select districtname from eg_city),\n"
			+ "'applicationstatus', status.description,\n" 
			+ "'applicantaddress.id',	 address.id		,\n" 
			+ "'mobilenumber', usr.mobilenumber,\n"
			+ "'citygrade', (select grade from eg_city),\n"
			+ "'noOfWaterClosets',(select case when conndetails.noofclosets_residential is not null then conndetails.noofclosets_residential "
			+ " else  conndetails.noofclosets_nonresidential end ) ,\n"
			+ "'noofseatsnonresidential', conndetails.noofclosets_nonresidential,\n"
			+ "'doorno', replace(address.housenobldgapt,'/','\\') ,\n" +
			"  'dcb',	 (SELECT json_agg(dcb) FROM ( select to_char(inst.start_date	,	 "+
			"  'YYYY-MM-DD')  \"from_date\"	,		 "+
			"  to_char(inst.end_date	,		 "+
			"  'YYYY-MM-DD')  \"to_date\"	,		 "+
			"  inst.id \"insta_id\"		,	 "+
			"  d.is_history \"is_history\"		,	 "+
			"  drm.code \"demand_reason\"	,		 "+
			"  dd.amount \"amount\"			, "+
			"  dd.amt_collected \"collected_amount\"		,	 "+
			"  inst.financial_year \"financial_year\" from egswtax_demand_connection cdemand	,		 "+
			"  eg_demand d		,	 "+
			"  eg_demand_details dd	,		 "+
			"  eg_installment_master inst	,		 "+
			"  eg_demand_reason_master drm		,	 "+
			"  eg_demand_reason dr where appdetails.id=cdemand.applicationdetail and appdetails.connectiondetail=conndetails.id and cdemand.demand=d.id and d.id=dd.id_demand "+
		    " and dr.id_installment=inst.id and dd.id_demand_reason=dr.id and dr.id_demand_reason_master=drm.id and inst.id=dr.id_installment "+
		    " and d.is_history='N' order by inst.start_date ) dcb )	)	 "
			+ "from egswtax_connection conn, egswtax_connectiondetail conndetails,\n"
			+ "egswtax_applicationdetails appdetails,\n"
			+ "egswtax_application_type apptype,\n"
			+ "egswtax_usage_type usage,\n"
			+ "eg_boundary locality, eg_boundary zone,\n"
			+ "eg_boundary block, egswtax_connection_owner_info ownerinfo,\n"
			+ "eg_user usr, eg_address address, egw_status status\n"
			+ "where appdetails.connection=conn.id and\n"
			+ "appdetails.connectiondetail=conndetails.id and \n"
			+ "apptype.id=appdetails.applicationtype and\n"
			+ "usage.id=conndetails.usagetype and block.id=conn.block and\n"
			+ "locality.id=conn.locality and zone.id=conn.zone and\n"
			+ " ownerinfo.connection=conn.id and usr.id=ownerinfo.owner and\n"
			+ "  address.id=conn.address and status.id=appdetails.status  "
		    +   " and conn.shsc_number is not null "
			//+" :locCondition "
			+ " and conndetails.id not in (select erpid::bigint from egswtax_migration where status"
			+ " in ('Saved','Demand_Created' ) ) order by conndetails.id limit 1000;";
	
	
	
	public static final String SEWERAGE_CONNECTION_TABLE="create table  if not exists  egswtax_migration "
			+ " ( erpid varchar(64),erpconn varchar(64) ,digitconn varchar(64) ,erppt varchar(64),digitpt varchar(64),status varchar(64),tenantId varchar(64),additiondetails varchar(1000),errorMessage varchar(4000)  );"
			+ "";
	
	public static final String sewerageRecord_insert="insert into  :schema_tenantId.egswtax_migration  "
			+ "(erpid ,erpconn  ,digitconn  ,erppt ,digitpt ,status ,tenantId ,additiondetails ) values (:erpid,:erpconn,:digitconn,:erppt,:digitpt,:status"
			+ ", :tenantId,:addtionaldetails);";
	
	public static final String sewerageRecord_update="update  :schema_tenantId.egswtax_migration  "+
			"set digitconn=:digitconn , digitpt=:digitpt,status=:status where erpid=:erpid ";
		
//	public static final String address="select id,housenobldgapt as plotno,landmark,citytownvillage as city,district,arealocalitysector as region,state,country,pincode, buildingName,streetroadline as street from eg_address where id=:id ;";
//	
	public static final String SEWERAGE_COLLECTION_TABLE="create table  if not exists  egswtax_cl_migration "
			+ " ( erpid varchar(64),erpconn varchar(64) ,digitconn varchar(64) ,erppt varchar(64),digitpt varchar(64),status varchar(64),tenantId varchar(64),additiondetails varchar(1000),errorMessage varchar(4000) );"
			+ "";
	public static final String SEWERAGE_COLLECTION_QUERY="select json_build_object(\n"
			+ "'paymentMode','cash',\n"
			+ "'paymentStatus', 'New',\n"
			+ "'businessService', 'SW',\n"
			+ "'transactionNumber',ih.transactionnumber,\n"
			+ "'transactionDate', (select extract(epoch from ih.transactiondate) * 1000),\n"
			+ "'paidBy', ch.payeename,\n"
			+ "'mobileNumber', owner.mobilenumber,\n"
			+ "'payerName', owner.name, \n"
			+ "'consumerCode', ch.consumercode,\n"
			+ "'payerEmail', owner.emailid,\n"
			+ "'payerId', owner.id,\n"
			+ "'totalAmountPaid', ch.totalamount,\n"
			+ "'totalDue', ch.totalamount,\n"
			+ "'instrumentDate', (select extract(epoch from ih.instrumentdate) * 1000) ,\n"
			+ "'instrumentNumber', ih.instrumentNumber,\n"
			+ "'instrumentStatus', status.code,\n"
			+ "'paymentDetails', (json_agg(json_build_object('totalDue', ch.totalamount, 'totalAmountPaid' , ch.totalamount, 'businessService', 'SW')ORDER BY ch.id ) )\n"
			+ ") as payments_info\n"
			+ "from egcl_collectionheader ch \n"
			+ "INNER JOIN egcl_servicedetails billingservice ON ch.servicedetails=billingservice.id and billingservice.code !='WT'\n"
			+ "INNER JOIN egcl_collectioninstrument ci ON ch.id=ci.collectionheader \n"
			+ "INNER JOIN egf_instrumentheader ih ON ci.instrumentheader=ih.id \n"
			+ "INNER JOIN egf_instrumenttype it ON ih.instrumenttype=it.id \n"
			+ "INNER JOIN egw_status status ON ch.status=status.id and ch.status in (select id from egw_status where moduletype='ReceiptHeader' and code not in ('PENDING','FAILED')) \n"
			+ "INNER JOIN egswtax_connection wtrcon ON ch.consumercode=wtrcon.shsc_number \n"
			+ "INNER JOIN egswtax_connection_owner_info connowner ON wtrcon.id=connowner.connection \n"
			+ "INNER JOIN egswtax_applicationdetails conndetails ON wtrcon.id=conndetails.connection \n"
			+ "INNER JOIN eg_user owner ON owner.id=connowner.owner  \n"
			+ " where conndetails.id in (select erpid::bigint from egswtax_migration where status in ('Demand_Created' ) ) "
			+ " GROUP BY it.type,ih.transactionnumber, ih.transactiondate, ch.payeename, owner.mobilenumber, owner.name, ch.consumercode, owner.emailid, owner.id,ch.totalamount, ih.instrumentdate, ih.instrumentNumber,status.code \n"
			+ " UNION ALL \n"
			+ "select json_build_object(\n"
			+ "'paymentMode','cash',\n"
			+ "'paymentStatus', 'New',\n"
			+ "'businessService', 'SW.ONE_TIME_FEE',\n"
			+ "'transactionNumber',ih.transactionnumber,\n"
			+ "'transactionDate', (select extract(epoch from ih.transactiondate) * 1000),\n"
			+ "'paidBy', ch.payeename,\n"
			+ "'mobileNumber', owner.mobilenumber,\n"
			+ "'payerName', owner.name,\n"
			+ "'consumerCode', CASE WHEN wtrcon.shsc_number is not null THEN wtrcon.shsc_number ELSE conndetails.applicationnumber END,\n"
			+ "'payerEmail', owner.emailid,\n"
			+ "'payerId', owner.id,\n"
			+ "'totalAmountPaid', ch.totalamount,\n"
			+ "'totalDue', ch.totalamount,\n"
			+ "'instrumentDate', (select extract(epoch from ih.instrumentdate) * 1000) ,\n"
			+ "'instrumentNumber', ih.instrumentNumber,\n"
			+ "'instrumentStatus', status.code,\n"
			+ "'paymentDetails', (json_agg(json_build_object('totalDue', ch.totalamount, 'totalAmountPaid' , ch.totalamount, 'businessService', 'SW.ONE_TIME_FEE') ORDER BY ch.id ) )\n"
			+ ") as payments_info\n"
			+ "from egcl_collectionheader ch \n"
			+ "INNER JOIN egcl_servicedetails billingservice ON ch.servicedetails=billingservice.id and billingservice.code !='WT'\n"
			+ "INNER JOIN egcl_collectioninstrument ci ON ch.id=ci.collectionheader \n"
			+ "INNER JOIN egf_instrumentheader ih ON ci.instrumentheader=ih.id \n"
			+ "INNER JOIN egf_instrumenttype it ON ih.instrumenttype=it.id \n"
			+ "INNER JOIN egw_status status ON ch.status=status.id and ch.status in (select id from egw_status where moduletype='ReceiptHeader' and code not in ('PENDING','FAILED')) \n"
			+ "INNER JOIN egswtax_applicationdetails conndetails ON ch.consumercode=conndetails.applicationnumber \n"
			+ "INNER JOIN egswtax_connection wtrcon ON wtrcon.id=conndetails.connection\n"
			+ "INNER JOIN egswtax_connection_owner_info connowner ON wtrcon.id=connowner.connection \n"
			+ "INNER JOIN eg_user owner ON owner.id=connowner.owner \n"
			+ " where  conndetails.id in (select erpid::bigint from egswtax_migration where status in ('Demand_Created') ) "
			+ "GROUP BY it.type,ih.transactionnumber, ih.transactiondate, ch.payeename, owner.mobilenumber, owner.name, ch.consumercode, owner.emailid, owner.id,ch.totalamount, ih.instrumentdate, ih.instrumentNumber,status.code,conndetails.applicationnumber,  wtrcon.shsc_number;";
	
	public static final String SEWERAGE_COLLECTION_INSERT="insert into  :schema.egswtax_cl_migration  "
			+ "(erpid ,erpconn  ,digitconn  ,erppt ,digitpt ,status ,tenantId ,additiondetails ) values (:erpid,:erpconn,:digitconn,:erppt,:digitpt,:status"
			+ ", :tenantId,:addtionaldetails);";
	
	public static final String SEWERAGE_COLLECTION_UPDATE="update  :schema.egswtax_cl_migration  "+
			"set digitconn=:digitconn , digitpt=:digitpt,status=:status where erpconn=:erpconn and tenantId=:tenantId";
	
	public static final String ProcessContent="INSERT INTO eg_wf_processinstance_v2( id,tenantid,businessService,businessId,moduleName,"
			+ "action,status,comment, assigner, stateSla,businessServiceSla, previousStatus, createdby, lastmodifiedby, createdtime, lastmodifiedtime)"
			+" values ('':id'','':tenantId'','':businessService'' ,'':businessId'','':moduleName'',''ACTIVATE_CONNECTION'',"
			+" ''d5e544f2-eac3-4dd6-b151-9045acad61c2'',null,'':userUUID'',null,0,null,'':userUUID'','':userUUID'',:epocnow,:epocnow);";

	public static final String PROCESSINSERT="Insert into :schema.processinsert (stmt) values('\":val\"');"; 
	public static final String PROCESSINSERTTABLE="create table if not exists processinsert (stmt varchar(1000) ); ";
	
	
//	public static final String additional_details= "Select id, ledgerid as ledgerId, connectiontype as connectionCategory, billingtype as billingType, billamount as billingAmount, estimationnoticedate as estimationLetterDate, estimationFileStoreId as estimationnoticefilestoreid, averagemeterreading as averageMake, metermake as meterMake, initialmeterreading as initialMeterReading, otherfee as othersFee \n"
//	+ "\n"
//	+ "from egwtr_connectiondetails,egwtr_meter_details,egwtr_stg_connection,egwtr_connection_conversion_type\n"
//	+ "\n"
//	+ "where id=:id;"; 
	
	public static final String WATERDOCUMENTSQL="select app.connectiondetailsid,dname.documentname,f.filestoreid,f.filename,f.contenttype "+
	                       " from egwtr_application_documents app, egwtr_documents d,eg_filestoremap f ,egwtr_document_names dname "+
	                       " where f.id=d.filestoreid  and d.applicationdocumentsid=app.id and app.documentnamesid=dname.id "
	                       + " and app.connectiondetailsid=:connNo 	and documentname!='DemandBill' order by app.connectiondetailsid ";
	
	
	
	
	
}
