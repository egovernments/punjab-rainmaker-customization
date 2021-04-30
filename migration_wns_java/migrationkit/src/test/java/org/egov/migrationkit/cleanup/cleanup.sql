set search_path to fazilka_prod;
delete from egwtr_migration;
delete from egswtax_migration;
delete from egwtr_cl_migration;
delete from egswtax_cl_migration;

--water
delete from eg_ws_roadcuttinginfo where tenantid='pb.fazilka';
delete from  eg_ws_meterreading where tenantid='pb.fazilka';
delete from eg_ws_plumberinfo where tenantid='pb.fazilka';
delete from eg_ws_connectionholder where tenantid='pb.fazilka';
delete from eg_ws_service_audit where connection_id in (select id from eg_ws_connection where tenantid='pb.fazilka');
delete from eg_ws_service where connection_id in (select id from eg_ws_connection where tenantid='pb.fazilka');
delete from eg_ws_applicationdocument where tenantid='pb.fazilka';
delete from eg_ws_connection_audit where tenantid='pb.fazilka';
delete from eg_ws_connection where tenantid='pb.fazilka';
--delete from eg_ws_schedular where tenantid='pb.fazilka';

--sewerage

delete from eg_sw_roadcuttinginfo where tenantid='pb.fazilka';
delete from eg_sw_plumberinfo where tenantid='pb.fazilka';
delete from eg_sw_connectionholder where tenantid='pb.fazilka';
delete from eg_sw_service_audit where connection_id in (select id from eg_sw_connection where tenantid='pb.fazilka');
delete from eg_sw_service where connection_id in (select id from eg_sw_connection where tenantid='pb.fazilka');
delete from eg_sw_applicationdocument where tenantid='pb.fazilka';
delete from eg_sw_connection_audit where tenantid='pb.fazilka';
delete from eg_sw_connection where tenantid='pb.fazilka';
--delete from eg_sw_schedular where tenantid='pb.fazilka';

--demands

delete from egbs_demanddetail_v1 where tenantid='pb.fazilka' and demandid in(select id from egbs_demand_v1 where businessservice in ('WS','SW','SW.ONE_TIME_FEE','WS.ONE_TIME_FEE') and tenantid='pb.fazilka');
delete from egbs_demand_v1 where businessservice in ('WS','SW','SW.ONE_TIME_FEE','WS.ONE_TIME_FEE') and tenantid='pb.fazilka';

--to delete the Advance carry forward

select * from egbs_demanddetail_v1 where taxheadcode='WS_ADVANCE_CARRYFORWARD' and demandid in (select id from egbs_demand_v1 where consumercode ='0603003086' and businessservice='WS');

delete from egbs_demanddetail_v1 where taxheadcode='WS_ADVANCE_CARRYFORWARD' and demandid in (select id from egbs_demand_v1 where consumercode ='0603003086' and businessservice='WS');

--Bill

delete from egbs_billaccountdetail_v1 where tenantid='pb.fazilka' and billdetail in(select id from egbs_billdetail_v1  where businessservice in ('WS','SW','SW.ONE_TIME_FEE','WS.ONE_TIME_FEE') and tenantid='pb.fazilka') and additionaldetails='{"manualmigratedbill":true}';
 
 create table existbills(
 billid CHARACTER VARYING (128)
 );

insert into existbills
  SELECT billid
  FROM egbs_billdetail_v1
  where businessservice in ('WS','SW','SW.ONE_TIME_FEE','WS.ONE_TIME_FEE') and tenantid='pb.fazilka'  and additionaldetails= '{"manualmigratedbill":true}' ;
 
  delete from egbs_billdetail_v1  where businessservice in ('WS','SW','SW.ONE_TIME_FEE','WS.ONE_TIME_FEE') and tenantid='pb.fazilka';
  delete from egbs_bill_v1 where id in (select billid from existbills) and tenantid='pb.fazilka'  and additionaldetails= '{"manualmigratedbill":true}' ;
 
  drop table existbills;
  --collections
 
   delete from egcl_billaccountdetail where tenantid ='pb.fazilka' and billdetailid  in (select id from egcl_billdetial where billid in(select id from egcl_bill where businessservice in ('WS','SW') and tenantid='pb.fazilka') and additionaldetails='{"manualmigratedbill":true}') ;
  delete  from egcl_billdetial where billid in(select id from egcl_bill where businessservice in ('WS','SW') and tenantid='pb.fazilka'  and additionaldetails='{"manualmigratedbill":true}' ) and additionaldetails='{"manualmigratedbill":true}';
 
 
  create table existpayments (
   paymentid  CHARACTER VARYING (128)
);
 
  insert into  existpayments
  SELECT paymentid
  FROM egcl_paymentdetail
  where businessservice in ('WS','SW') and tenantid='pb.fazilka' and billid in (select id from egcl_bill where businessservice in ('WS','SW') and tenantid='pb.fazilka' and additionaldetails='{"manualmigratedbill":true}' )  ;

 delete from egcl_bill where businessservice in ('WS','SW') and tenantid='pb.fazilka' and additionaldetails='{"manualmigratedbill":true}';
 
  delete from egcl_paymentdetail where paymentid in (select paymentid from existpayments) and businessservice in ('WS','SW') and tenantid='pb.fazilka'  ;
 
  delete from egcl_payment where id in (select paymentid from existpayments) and tenantid='pb.fazilka';
 
  drop table existpayments;
 
  --Property
 
  delete from eg_pt_address where propertyid in(select id from eg_pt_property where source='WATER_CHARGES' and tenantid='pb.fazilka');
  delete from eg_pt_owner where propertyid in(select id from eg_pt_property where source='WATER_CHARGES' and tenantid='pb.fazilka');
  delete from eg_pt_unit where propertyid in(select id from eg_pt_property where source='WATER_CHARGES' and tenantid='pb.fazilka');
  delete from eg_pt_institution where propertyid in(select id from eg_pt_property where source='WATER_CHARGES' and tenantid='pb.fazilka');
  delete from eg_pt_property_audit where propertyid in (select id from eg_pt_property where source='WATER_CHARGES' and tenantid='pb.fazilka') ;
  delete from eg_pt_property where source='WATER_CHARGES' and tenantid='pb.fazilka';


