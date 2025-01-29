select json_build_object(
'cityname', (SELECT CASE WHEN name like '%UAT%' THEN (SELECT split_part(name,'-',1) from eg_city) ELSE (select name from eg_city) END from eg_city),
'zone', zone.name,
'consumercode', conn.shsc_number,
'id', (select code from eg_city)||'-'||conndetails.id||'-SC',
'applicantname', usr.name,
'connectionstatus', conn.status,
'createddate', to_timestamp(to_char(conn.createddate::timestamp without time zone, 'YYYY-MM-DD'),'YYYY-MM-DDTHH24:MI:SSZ'),
'servicetype', 'Sewerage Charges',
'autoverifieddate', (select lastmodifieddate from eg_wf_state_history statehist where statehist.state_id=appdetails.state_id and lastmodifiedby=(select id from eg_user where username='system')),
'guardianname', usr.guardian,
'channel', CASE WHEN appdetails.source is not null THEN appdetails.source ELSE 'COUNTER' END,
'applicationtype', apptype.name,
'locality', locality.name,
'block', block.name,
'citycode', (select code from eg_city),
'emailid', usr.emailid,
'applicationnumber', appdetails.applicationnumber,
'disposaldate', appdetails.disposalDate,
'usage', usage.name,
'applicationdate', appdetails.applicationdate,
'districtname', (select districtname from eg_city),
'applicationstatus', status.description,
'mobilenumber', usr.mobilenumber,
'citygrade', (select grade from eg_city),
'noofseatsresidential',conndetails.noofclosets_residential,
'noofseatsnonresidential', conndetails.noofclosets_nonresidential,
'doorno', replace(address.housenobldgapt,'/','\'))
from egswtax_connection conn, egswtax_connectiondetail conndetails,
egswtax_applicationdetails appdetails,
egswtax_application_type apptype,
egswtax_usage_type usage,
eg_boundary locality, eg_boundary zone,
eg_boundary block, egswtax_connection_owner_info ownerinfo,
eg_user usr, eg_address address, egw_status status
where appdetails.connection=conn.id and
appdetails.connectiondetail=conndetails.id and
apptype.id=appdetails.applicationtype and
usage.id=conndetails.usagetype and block.id=conn.block and
locality.id=conn.locality and zone.id=conn.zone and
ownerinfo.connection=conn.id and usr.id=ownerinfo.owner and
address.id=conn.address and status.id=appdetails.status and conn.legacy=false ;