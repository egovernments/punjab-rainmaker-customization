set search_path to fazilka_prod ;
create or replace function migrate_manual_bills( tenantId varchar)
   returns text as $$ 
declare 
     billId varchar(64);
	 billdetailid  varchar(64);
	 digitStatus  varchar(64);
     demandId_digit varchar(64);
     demand_detail_digit	varchar(64);
     concode varchar(64);
	 mobilenumber varchar(10) default '9999119999';
	 titles text default 'Success';
	 service varchar(64);
	 head varchar(64);
	 rec   record;
	 props record;
	 bill_detail   record;
	demands record;
	 cur_bills cursor for
	 		 select * from eg_bill bill where bill.id_bill_type=1 and bill.service_code in ('WT','STAX')  
	 		 and bill.is_cancelled='N' and bill.is_history='N'  
	 		 union
		   select bill.* from eg_bill bill, egcl_collectionheader ch where ch.referencenumber::bigint=bill.id and 
		   bill.service_code in ('WT','STAX') 
		       and id_bill_type=(select id from eg_bill_type where code='AUTO')  and  is_cancelled='N' and is_history='N' 
		       and ch.status = (select id from egw_status where moduletype='ReceiptHeader' and description='Approved') 
					  ;
		       
   

begin
   -- open the cursor
   open cur_bills;
	
   Loop
       fetch cur_bills into rec;
        exit when not found;
 
        concode:=rec.consumer_id;

      raise notice 'erp service code %s',rec.service_code;

      if(rec.service_code  = 'WT' ) then
          service:='WS';
    	select mobilenumber from egwtr_migration where erpid::bigint=rec.id  limit 1 into mobilenumber ;
	
      else 
          service:='SW';
         select mobilenumber from egswtax_migration where erpid::bigint=rec.id  limit 1 into mobilenumber ;
      
      end if;    
      
       if(rec.description like 'Water Application Number%')
       then
       service:='WS.ONE_TIME_FEE' ;
       select consumercode from egwtr_connection where id in (select connection from egwtr_connectiondetails where 	   applicationnumber=rec.consumer_id) 
       into concode;
       end if;
       if(rec.description like 'Sewerage Application Number%')
       then
        select shsc_number from egswtax_connection where id in (select connection from egswtax_applicationdetails where
        applicationnumber=rec.consumer_id)  into concode;
       service:='SW.ONE_TIME_FEE' ;
       end if;


       

SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring) into billId ;
raise notice 'Bill id in digit %',billId;
if(rec.id_bill_type = 2 )
then
digitStatus:='PAID';
else 
digitStatus='EXPIRED';
end if ;

if(rec.last_date > now() ) then
  digitStatus='ACTIVE';

end if ;

         INSERT INTO public.egbs_bill_v1 (
            id, tenantid, payername, payeraddress, payeremail, isactive, 
            iscancelled, createdby, createddate, lastmodifiedby, lastmodifieddate, 
            mobilenumber, status, additionaldetails)
    VALUES (
	billId, tenantId, rec.citizen_name, rec.citizen_address, rec.emailid, True, False,'6ccc8719-5b0a-4d24-924e-ec6d2a674b28',
	Extract(epoch FROM rec.create_date) * 1000, '6ccc8719-5b0a-4d24-924e-ec6d2a674b28', Extract(epoch FROM rec.modified_date) * 1000,
	mobilenumber, digitStatus, '{"remigrated":true}') ;

 raise notice 'bill id is %s',rec.id ;
 
 select * from eg_bill bill,eg_demand demand ,eg_installment_master inst  
where demand.id_installment=inst.id and bill.id_demand=demand.id and   bill.id=rec.id into bill_detail ;

SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring) into billdetailid ;
raise notice ' bill_detail.start_date %s  ',bill_detail.start_date ;
raise notice ' bill_detail.end_date %s  ',bill_detail.end_date ;

raise notice 'service code %s %s',service,concode  ;
select * from public.egbs_demand_v1 demand where   ( demand.consumercode=concode or demand.consumercode=(select applicationno from public.eg_ws_connection where connectionno=concode))  and demand.businessservice =service   into demands;
raise notice 'demands %s',demands.taxperiodfrom  ;
raise notice 'demands %s',demands.taxperiodto  ;
  
  


select demand.id from public.egbs_demand_v1 demand where    
  ( demand.consumercode=concode or demand.consumercode=(select applicationno from public.eg_ws_connection where connectionno=concode)) and demand.businessservice =service
and demand.taxperiodfrom>= Extract(epoch FROM bill_detail.start_date   ) * 1000 
and demand.taxperiodfrom<= Extract(epoch FROM bill_detail.end_date   ) * 1000 
and demand.taxperiodto>=Extract(epoch FROM bill_detail.start_date    ) * 1000  
and demand.taxperiodto<=Extract(epoch FROM bill_detail.end_date    ) * 1000 
 
into demandId_digit ;

raise notice 'found from first %s',demandId_digit ;

if( demandId_digit is null) then 

select demand.id   from public.egbs_demand_v1 demand where    ( demand.consumercode=concode or demand.consumercode=(select applicationno from public.eg_ws_connection where connectionno=concode)) 
and demand.businessservice =service
and  Extract(epoch FROM rec.issue_date    ) * 1000 between demand.taxperiodfrom and demand.taxperiodto into demandId_digit;
raise notice 'found from 2nd %s',demandId_digit;
end if;


if( demandId_digit is null) then 
select demand.id   from public.egbs_demand_v1 demand where    ( demand.consumercode=concode or demand.consumercode=(select applicationno from public.eg_ws_connection where connectionno=concode)) 
and demand.businessservice =service
order by demand.taxperiodfrom desc limit 1 into demandId_digit ;
raise notice 'found from 3rd %s',demandId_digit;
end if;

if(demandId_digit is not null)
then
 


INSERT INTO public.egbs_billdetail_v1(
            id, tenantid, billid, businessservice, billno, billdate, consumercode, 
            consumertype, billdescription, displaymessage, minimumamount, 
            totalamount, callbackforapportioning, partpaymentallowed, collectionmodesnotallowed, 
            createdby, createddate, lastmodifiedby, lastmodifieddate, receiptdate, 
            receiptnumber, fromperiod, toperiod, demandid, isadvanceallowed, 
            expirydate, additionaldetails)
    VALUES (billdetailid, tenantId, billId, service, rec.bill_no, Extract(epoch FROM rec.issue_date) * 1000, concode, null, 
            null, null, rec.min_amt_payable, rec.total_amount, 
            null, null, null, '6ccc8719-5b0a-4d24-924e-ec6d2a674b28', 
            Extract(epoch FROM rec.create_date) * 1000,  '6ccc8719-5b0a-4d24-924e-ec6d2a674b28', Extract(epoch FROM rec.create_date) * 1000, 
           null, null, Extract(epoch FROM bill_detail.start_date) * 1000, Extract(epoch FROM bill_detail.end_date) * 1000, demandId_digit, null, 
            Extract(epoch FROM rec.last_date) * 1000, '{"remigrated":true}');
        
for props in (select * from eg_bill_details detail ,eg_demand_reason reason,eg_installment_master inst ,eg_demand_reason_master master  
where reason.id_installment=inst.id and detail.id_demand_reason=reason.id   and master.id=reason.id_demand_reason_master  and  detail.id_bill=rec.id   )
Loop
 begin
	 
raise notice  '% detail %s  bill-no %s  and detail-id %s',props.code,props.description ,rec.bill_no ,props.id ;

 if( props.code = 'METERCHARGES') then head:= 'WS_METER_TESTING_FEE' ; end if;
if( props.code = 'PENALTY') then head:= 'WS_TIME_PENALTY' ; end if;
if( props.code = 'BREAKDOWN_PENALTY') then head:= 'WS_BREAKDOWN_PENALTY' ; end if;
if( props.code = 'INTEREST') then head:= 'WS_TIME_INTEREST' ; end if;
if( props.code = 'WTAXCHARGES') then head:= 'WS_CHARGE' ; end if;
if( props.code = 'METERRENT') then head:= 'WS_METER_RENT' ; end if;
if( props.code = 'WTADJUSTMENT') then head:= 'WS_TAX_ADJUSTMENT' ; end if;
if( props.code = 'DOORTODOORCOLLECTIONCHARGES') then head:= 'WS_DOOR_TO_DOOR_COLLECTION_CHARGES' ; end if;
if( props.code = 'TITLETRANSFERFEE') then head:= 'WS_TITLE_TRANSFER_FEE' ; end if;
if( props.code = 'ADDITIONALFEEFORTITLETRANSFER') then head:= 'WS_ADDITIONAL_TITLE_TRANSFER_FEE' ; end if;
if( props.code = 'WTAXSUPERVISION') then head:= 'WS_SUPERVISION_CHARGE' ; end if;
if( props.code = 'WTADVANCE') then head:= 'WS_ADVANCE_CARRYFORWARD' ; end if;
if( props.code = 'WTAXSECURITY') then head:= 'WS_SECURITY_DEPOSIT' ; end if;
if( props.code = 'WTAXOTHERS') then head:= 'WS_OTHER_FEE' ; end if;
if( props.code = 'WTAXROADCUTTING') then head:= 'WS_ROAD_CUTTING_CHARGE' ; end if;
if( props.code = 'WTAXAPPLICATION') then head:= 'WS_FORM_FEE' ; end if;
if( props.code = 'CONNECTIONTYPECONVERSIONFEE') then head:= 'WS_CONNECTION_TYPE_CONVERSION_FEE' ; end if;
     
if( props.code = 'SEWERAGEADVANCE') then head:='SW_ADVANCE_CARRYFORWARD' ; end if;
if( props.code = 'STAXSECURITY') then head:='SW_SECURITY_DEPOSIT' ; end if;
if( props.code = 'STAXOTHERS') then head:='SW_OTHER_FEE' ; end if;
if( props.code = 'STAXROADCUTTING') then head:='SW_ROAD_CUTTING_CHARGE' ; end if;
if( props.code = 'STAXAPPLICATION') then head:='SW_FORM_FEE' ; end if;
if( props.code = 'STAXSUPERVISION') then head:='SW_SUPERVISION_CHARGE' ; end if;
if( props.code = 'SEWERAGETAX') then head:='SW_CHARGE' ; end if;
 
if( props.code = 'SWTAXADJUSTMENT') then head:='SW_TAX_ADJUSTMENT' ; end if;
if( props.code = 'PENALTY' and  service = 'SW') then head:='SW_TIME_PENALTY' ; end if;
if( props.code = 'TITLETRANSFERFEE' and  service = 'SW' ) then head:='SW_TITLE_TRANSFER_FEE' ; end if;
if( props.code = 'INTEREST' and  service = 'SW') then head:='SW_TIME_INTEREST' ; end if;
if( props.code = 'ADDITIONALFEEFORTITLETRANSFER' and  service = 'SW') then head:='SW_ADDITIONAL_TITLE_TRANSFER_FEE' ; end if;
if( props.code = 'DOORTODOORCOLLECTIONCHARGES' and  service = 'SW') then head:='SW_DOOR_TO_DOOR_COLLECTION_CHARGES' ; end if;
if( props.code = 'DONATIONCHARGE') then head:='SW_DONATION_CHARGE' ; end if;
if( props.code = 'INSPECTIONCHARGE') then head:='SW_INSPECTION_CHARGE' ; end if;
if( props.code = 'ESTIMATIONCHARGE') then head:='SW_ESTIMATION_CHARGE' ; end if;

--raise notice 'tax head code from erp bill%s',head ;
 
select id from public.egbs_demanddetail_v1 where demandid = demandId_digit
and taxheadcode=head into demand_detail_digit;
if (demand_detail_digit is not null)
then

raise notice 'found detailid  %s',demand_detail_digit;
 
         INSERT INTO public.egbs_billaccountdetail_v1(
            id, tenantid, billdetail, glcode, orderno, accountdescription, 
            creditamount, debitamount, isactualdemand, purpose, createdby, 
            createddate, lastmodifiedby, lastmodifieddate, cramounttobepaid, 
            taxheadcode, amount, adjustedamount, demanddetailid, additionaldetails)
    VALUES ( uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), tenantId, billdetailid, props.glcode, props.order_no,
	   props.description, 
            props.cr_amount, props.dr_amount, False, props.purpose, '6ccc8719-5b0a-4d24-924e-ec6d2a674b28', 
             Extract(epoch FROM rec.create_date) * 1000, '6ccc8719-5b0a-4d24-924e-ec6d2a674b28', Extract(epoch FROM rec.create_date) * 1000, props.cr_amount, 
           head, props.cr_amount, 0, demand_detail_digit, '{"remigrated":true}');
end if;
end ;
   end Loop;
end if ;

 end Loop;
   -- close the cursor
   close cur_bills;

   return titles;

end; $$

language plpgsql;




--select migrate_manual_bills('pb.fazilka');
--delete from public.egbs_billaccountdetail_v1 where additionaldetails ='{"remigrated":true}';
--delete from public.egbs_billdetail_v1 where additionaldetails ='{"remigrated":true}';
--delete from public.egbs_bill_v1 where additionaldetails ='{"remigrated":true}';

	


