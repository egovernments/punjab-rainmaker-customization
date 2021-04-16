 
create or replace function migrate_manual_bills( tenantId varchar)
   returns text as $$ 
declare 
         billId varchar(64);
	 billdetailid  varchar(64);
	 mobilenumber varchar(10) default '9999119999';
	 titles text default 'Success';
	 service varchar(64);
	 head varchar(64);
	 rec   record;
	 props record;
	 bill_detail   record;
	 cur_bills cursor for
		   select * from eg_bill where id_bill_type=1 and service_code in ('WT','STAX')  order by id  ;
   

begin
   -- open the cursor
   open cur_bills;
	
   loop
   


      fetch cur_bills into rec;


    
      exit when not found;
      if(rec.service_code  = 'WT') then
          service:='WS';
	select mobilenumber from egwtr_migration where erpid::bigint=rec.id  limit 1 into mobilenumber ;
      else 
          service:='SW';
         select mobilenumber from egswtax_migration where erpid::bigint=rec.id  limit 1 into mobilenumber ;
      end if;    

       

SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring) into billId ;


         INSERT INTO public.egbs_bill_v1 (
            id, tenantid, payername, payeraddress, payeremail, isactive, 
            iscancelled, createdby, createddate, lastmodifiedby, lastmodifieddate, 
            mobilenumber, status, additionaldetails)
    VALUES (
billId, tenantId, rec.citizen_name, rec.citizen_address, rec.emailid, True, False,'6ccc8719-5b0a-4d24-924e-ec6d2a674b28',Extract(epoch FROM rec.create_date) * 1000,
            '6ccc8719-5b0a-4d24-924e-ec6d2a674b28', Extract(epoch FROM rec.modified_date) * 1000, mobilenumber, 'ACTIVE', '{"manualmigratedbill":true}') ;

 select * from eg_bill_details detail ,eg_demand_reason reason,eg_installment_master inst  
where reason.id_installment=inst.id and detail.id_demand_reason=reason.id and   id_bill=rec.id into bill_detail ;
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring) into billdetailid ;

INSERT INTO public.egbs_billdetail_v1(
            id, tenantid, billid, businessservice, billno, billdate, consumercode, 
            consumertype, billdescription, displaymessage, minimumamount, 
            totalamount, callbackforapportioning, partpaymentallowed, collectionmodesnotallowed, 
            createdby, createddate, lastmodifiedby, lastmodifieddate, receiptdate, 
            receiptnumber, fromperiod, toperiod, demandid, isadvanceallowed, 
            expirydate, additionaldetails)
    VALUES (billdetailid, tenantId, billId, service, rec.bill_no, Extract(epoch FROM rec.issue_date) * 1000, rec.consumer_id, null, 
            null, null, rec.min_amt_payable, rec.total_amount, 
            null, null, null, '6ccc8719-5b0a-4d24-924e-ec6d2a674b28', 
            Extract(epoch FROM rec.create_date) * 1000,  '6ccc8719-5b0a-4d24-924e-ec6d2a674b28', Extract(epoch FROM rec.create_date) * 1000, 
           null, null, Extract(epoch FROM bill_detail.start_date) * 1000, Extract(epoch FROM bill_detail.end_date) * 1000, null, null, 
            Extract(epoch FROM rec.last_date) * 1000, '{"manualmigratedbill":true}');

for props in (select * from eg_bill_details detail ,eg_demand_reason reason,eg_installment_master inst ,eg_demand_reason_master master  
where reason.id_installment=inst.id and detail.id_demand_reason=reason.id and master.id=reason.id_demand_reason_master and   id_bill=rec.id   )
Loop
 begin

 if( props.code = 'METERCHARGES') then head:= 'WS_METER_TESTING_FEE' ; end if;
if( props.code = 'PENALTY') then head:= 'WS_TIME_PENALTY' ; end if;
if( props.code = 'BREAKDOWN_PENALTY') then head:= 'WS_TIME_PENALTY' ; end if;
if( props.code = 'INTEREST') then head:= 'WS_TIME_INTEREST' ; end if;
if( props.code = 'WTAXCHARGES') then head:= 'WS_CHARGE' ; end if;
if( props.code = 'METERRENT') then head:= 'WS_OTHER_FEE' ; end if;
if( props.code = 'WTADJUSTMENT') then head:= 'WS_OTHER_FEE' ; end if;
if( props.code = 'DOORTODOORCOLLECTIONCHARGES') then head:= 'WS_OTHER_FEE' ; end if;
if( props.code = 'TITLETRANSFERFEE') then head:= 'WS_OTHER_FEE' ; end if;
if( props.code = 'ADDITIONALFEEFORTITLETRANSFER') then head:= 'WS_OTHER_FEE' ; end if;
if( props.code = 'WTAXSUPERVISION') then head:= 'WS_OTHER_FEE' ; end if;
if( props.code = 'WTADVANCE') then head:= 'WS_ADVANCE_CARRYFORWARD' ; end if;
if( props.code = 'WTAXSECURITY') then head:= 'WS_SECURITY_DEPOSIT' ; end if;
if( props.code = 'WTAXOTHERS') then head:= 'WS_OTHER_FEE' ; end if;
if( props.code = 'WTAXROADCUTTING') then head:= 'WS_ROAD_CUTTING_CHARGE' ; end if;
if( props.code = 'WTAXAPPLICATION') then head:= 'WS_FORM_FEE' ; end if;
if( props.code = 'CONNECTIONTYPECONVERSIONFEE') then head:= 'WS_OTHER_FEE' ; end if;
     
if( props.code = 'SEWERAGEADVANCE') then head:='SW_ADVANCE_CARRYFORWARD' ; end if;
if( props.code = 'STAXSECURITY') then head:='SW_SECURITY_DEPOSIT' ; end if;
if( props.code = 'STAXOTHERS') then head:='SW_OTHER_FEE' ; end if;
if( props.code = 'STAXROADCUTTING') then head:='SW_ROAD_CUTTING_CHARGE' ; end if;
if( props.code = 'STAXAPPLICATION') then head:='SW_OTHER_FEE' ; end if;
if( props.code = 'STAXSUPERVISION') then head:='SW_OTHER_FEE' ; end if;
if( props.code = 'SEWERAGETAX') then head:='SW_CHARGE' ; end if;
if( props.code = 'SWTAXADJUSTMENT') then head:='SW_OTHER_FEE' ; end if;
if( props.code = 'PENALTY' and  service = 'SW' ) then head:='SW_TIME_PENALTY' ; end if;
 
   
         INSERT INTO public.egbs_billaccountdetail_v1(
            id, tenantid, billdetail, glcode, orderno, accountdescription, 
            creditamount, debitamount, isactualdemand, purpose, createdby, 
            createddate, lastmodifiedby, lastmodifieddate, cramounttobepaid, 
            taxheadcode, amount, adjustedamount, demanddetailid, additionaldetails)
    VALUES ( uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), tenantId, billdetailid, props.glcode, props.order_no,
	   props.description, 
            props.cr_amount, props.dr_amount, False, props.purpose, '6ccc8719-5b0a-4d24-924e-ec6d2a674b28', 
             Extract(epoch FROM rec.create_date) * 1000, '6ccc8719-5b0a-4d24-924e-ec6d2a674b28', Extract(epoch FROM rec.create_date) * 1000, props.cr_amount, 
           head, props.cr_amount, 0, null, '{"manualmigratedbill":true}');
   end ;
   end Loop;
           



   
 end loop;
   -- close the cursor
   close cur_bills;

   return titles;

end; $$

language plpgsql;


select migrate_manual_bills('pb.fazilka');




