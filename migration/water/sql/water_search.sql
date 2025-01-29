select json_build_object(
	'actualPipeSize', (select sizeininch from egwtr_pipesize psize where conndetails.pipesize=psize.id),
	'actualTaps', conndetails.nooftaps,
	'proposedPipeSize', (select sizeininch from egwtr_pipesize psize where conndetails.pipesize=psize.id),
	'proposedTaps', conndetails.nooftaps,
	'cityname', (SELECT CASE WHEN name like '%UAT%' THEN (SELECT split_part(name,'-',1) from eg_city) ELSE (select name from eg_city) END from eg_city),
	 'zone', zone.name,
	 'consumercode', conn.consumercode,
	 'id', (select code from eg_city)||'-'||conndetails.id||'-WC',
	 'applicantname', usr.name,
	 'connectionstatus', conndetails.connectionstatus,
	 'createddate', to_timestamp(to_char(conn.createddate::timestamp without time zone, 'YYYY-MM-DD'),'YYYY-MM-DDTHH24:MI:SSZ'),
	 'propertytype', proptype.name,
	 'guardianname', usr.guardian,
	 'channel', CASE WHEN conndetails.source is not null THEN conndetails.source ELSE 'COUNTER' END,
	 'applicationtype', apptype.name,
	 'locality', locality.name,
	 'pwssb', CASE (select value from eg_appconfig_values where key_id in (select id from eg_appconfig where key_name ='IS_PWSSB_ULB')) WHEN 'YES' THEN true ELSE false END,
	 'block', block.name,
	 'citycode', (select code from eg_city),
	 'emailid', usr.emailid,
	 'connectiontype', (select contype.name from egwtr_connection_type contype where contype.id=conndetails.connectiontype),
	 'applicationnumber', conndetails.applicationnumber,
	 'disposaldate', conndetails.disposalDate,
	 'usage', usage.name,
	 'applicationdate', conndetails.applicationdate,
	 'districtname', (select districtname from eg_city),
	 'applicationstatus', status.description,
	 'applicantaddress', (COALESCE(address.houseNoBldgApt||', ', '')||COALESCE(address.areaLocalitySector||', ','')||COALESCE(address.streetRoadLine||', ','')||COALESCE(address.landmark||', ','')||COALESCE(address.cityTownVillage||', ','')||COALESCE(address.postOffice||', ','')||COALESCE(address.subdistrict||', ','')||COALESCE(address.district||', ','')||COALESCE(address.state||', ','')||COALESCE(address.country||', ','')||COALESCE(address.pinCode,'')),
	 'regionname', (select regionname from eg_city) ,
	 'mobilenumber', usr.mobilenumber,
	 'category', wtrctgy.name,
	 'dcb',  (SELECT json_agg(dcb) FROM
	  ( SELECT to_char(install_master.start_date, 'YYYY-MM-DD') "fromdate",
				to_char(install_master.end_date, 'YYYY-MM-DD') "todate",
				dd.amount "taxamount",
				demand.amt_collected "taxcollection",
				dd.amount "penaltyamount",
                demand.amt_collected "penaltycollection",
				dd.amt_rebate "rebate"
				from egwtr_demand_connection demand_conn,
					 eg_demand demand,
					 eg_demand_details dd,
					 eg_installment_master install_master
				WHERE conndetails.id=demand_conn.connectiondetails
				and demand.id=demand_conn.demand and demand.id=dd.id_demand
				and demand.id_installment=install_master.id
			) dcb))

	 from egwtr_connection conn,
	 egwtr_connectiondetails conndetails,
	 egwtr_application_type apptype,
	 egwtr_usage_type usage,
	 eg_boundary locality,
	 eg_boundary zone,
	 eg_boundary block,
	 egwtr_property_type proptype,
	 egwtr_category wtrctgy,
	 egwtr_connection_owner_info ownerinfo,
	 eg_user usr,
	 eg_address address,

	 egw_status status
	 where conn.id=conndetails.connection
	 and apptype.id=conndetails.applicationtype
	 and usage.id=conndetails.usagetype
	 and block.id=conn.block
	 and locality.id=conn.locality
	 and zone.id=conn.zone
	 and conndetails.propertytype=proptype.id
	 and conndetails.category=wtrctgy.id
	 and ownerinfo.connection=conn.id
	 and usr.id=ownerinfo.owner
	 and address.id=conn.address
	 and status.id=conndetails.statusid
	 and conndetails.legacy=false limit 1;