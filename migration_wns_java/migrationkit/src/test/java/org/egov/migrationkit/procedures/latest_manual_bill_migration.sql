set search_path to dhilwan_prod ;

CREATE TABLE ws_latest_bill_data (erpbillid bigint,consumercode character varying(256), billno character varying(256), businessservice character varying(256));


create or replace function migrate_latest_manual_bills( digittenantId varchar)
returns text as $$
DECLARE
	connections record;
	all_connections record;
	billId varchar(64);
	billdetail_id varchar(64);
	billaccount_id varchar(64);
	digitStatus  varchar(64);
	consumer_id_v character varying(256);
	billno_v character varying(256);
	businessservice_v character varying(256);
	service varchar(64);
	mobilenumber varchar(10) default '9999119999';
	demands record;
	demanddetails record;
	total_amount numeric;
	billdetail_total_amount numeric;
	addetails jsonb;
	BEGIN

	FOR all_connections IN (select bill.*  from eg_bill bill where bill.id_bill_type=(select id from eg_bill_type where code='MANUAL') and bill.service_code in ('WT','STAX') 
	  and bill.is_cancelled='N' and bill.is_history='N' and consumer_id in ('0603000887','06030080490')
	  union 
	  select bill.* from eg_bill bill, egcl_collectionheader ch where ch.referencenumber::bigint=bill.id and bill.service_code in ('WT','STAX') and id_bill_type=(select id from eg_bill_type where code='AUTO') and consumer_id in ('0603000887','06030080490') and ch.status = (select id from egw_status where moduletype='ReceiptHeader' and description='Approved'))
	LOOP
		raise notice '% all_connections.id for all_connections.consumercode is %',all_connections.id, all_connections.consumer_id ;
	    FOR connections IN (select b.* from eg_bill b where b.consumer_id=all_connections.consumer_id and b.service_code=all_connections.service_code order by b.create_date desc limit 1)
	    LOOP
	    	raise notice '% connections_id for connections.consumercode is %',connections.id, connections.consumer_id ;
			if(select exists(select 1 from ws_latest_bill_data where consumercode=connections.consumer_id and billno=connections.bill_no and businessservice=connections.service_code) =false) THEN

				select consumer_id, service_code, bill_no into consumer_id_v, businessservice_v, billno_v from eg_bill b where b.consumer_id=connections.consumer_id and b.service_code=connections.service_code order by b.create_date desc limit 1;

				-- if(select exists(select 1 from ws_latest_bill_data where consumercode=consumer_id_v and businessservice=businessservice_v) =false) THEN
				-- INSERT INTO ws_latest_bill_data (consumercode, billno, businessservice) values (consumer_id_v, billno_v, businessservice_v);
				-- END IF;

				if(connections.id_bill_type =(select id from eg_bill_type where code='MANUAL')) THEN
				 digitStatus='ACTIVE';
				 addetails:='{"migrated":true,"bill_type":"manual"}';
				 else
				 digitStatus='PAID';
				 addetails:='{"migrated":true,"bill_type":"auto"}';
				end if ;

				if(connections.service_code  = 'WT' ) then
				    service:='WS';
				    select mobilenumber from egwtr_migration where erpid::bigint=connections.id  limit 1 into mobilenumber ;
				else 
				    service:='SW';
				    select mobilenumber from egswtax_migration where erpid::bigint=connections.id  limit 1 into mobilenumber ;
				end if;
			      
				if(connections.description like 'Water Application Number%') then
				    service:='WS.ONE_TIME_FEE' ;
				end if;
				if(connections.description like 'Sewerage Application Number%') then
				    service:='SW.ONE_TIME_FEE' ;
				end if;

				SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring) into billId ;

			  	INSERT INTO public.egbs_bill_v1 (
			            id, tenantid, payername, payeraddress, payeremail, isactive, 
			            iscancelled, createdby, createddate, lastmodifiedby, lastmodifieddate, 
			            mobilenumber, status, additionaldetails)
			    VALUES (
				billId, digittenantId, connections.citizen_name, connections.citizen_address, connections.emailid, True, False,'6ccc8719-5b0a-4d24-924e-ec6d2a674b28',
				Extract(epoch FROM connections.create_date) * 1000, '6ccc8719-5b0a-4d24-924e-ec6d2a674b28', Extract(epoch FROM connections.modified_date) * 1000,
				mobilenumber, digitStatus, addetails) ;

				FOR demands IN (select * from public.egbs_demand_v1 d  where d.tenantid=digittenantId and d.consumercode=connections.consumer_id and d.businessservice=service and d.status='ACTIVE' and d.ispaymentcompleted=false)

				LOOP

					raise notice '% demandid for consumercode is %',demands.id, connections.consumer_id ;
					if(select exists(select 1 from ws_latest_bill_data where consumercode=connections.consumer_id and billno=connections.bill_no and businessservice=connections.service_code) =false) THEN
						INSERT INTO ws_latest_bill_data (erpbillid, consumercode, billno, businessservice) values (connections.id, connections.consumer_id, connections.bill_no, connections.service_code);
				    END IF;
				    if ( connections.id_bill_type = (select id from eg_bill_type where code='AUTO') and ( connections.total_amount = 0 or connections.total_amount is null) ) then
						total_amount=connections.total_collected_amount ;
					else
						total_amount=connections.total_amount;
					end if ;
						if( total_amount is null) then
							total_amount=0;
					end if;

					SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring) into billdetail_id ;

					INSERT INTO public.egbs_billdetail_v1(
			            id, tenantid, billid, businessservice, billno, billdate, consumercode, 
			            consumertype, billdescription, displaymessage, minimumamount, 
			            totalamount, callbackforapportioning, partpaymentallowed, collectionmodesnotallowed, 
			            createdby, createddate, lastmodifiedby, lastmodifieddate, receiptdate, 
			            receiptnumber, fromperiod, toperiod, demandid, isadvanceallowed, 
		            	expirydate, additionaldetails)
		    		VALUES ( billdetail_id, digittenantId, billId, service, connections.bill_no, Extract(epoch FROM connections.issue_date) * 1000, consumer_id_v, null, null, null, connections.min_amt_payable, connections.total_amount, null, null, null, '6ccc8719-5b0a-4d24-924e-ec6d2a674b28', Extract(epoch FROM connections.create_date) * 1000,  '6ccc8719-5b0a-4d24-924e-ec6d2a674b28', Extract(epoch FROM connections.create_date) * 100, null, null, demands.taxperiodfrom,demands.taxperiodto, demands.id, null, Extract(epoch FROM connections.last_date) * 1000, addetails);

		    		FOR demanddetails IN (select * from public.egbs_demanddetail_v1 dd where dd.demandid=demands.id and dd.tenantid=digittenantId)

		    		LOOP
		    			SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring) into billaccount_id ;
		    			billdetail_total_amount=ABS(demanddetails.taxamount)-ABS(demanddetails.collectionamount);

		    			INSERT INTO public.egbs_billaccountdetail_v1(id, tenantid, billdetail, glcode, orderno, accountdescription, 
				            creditamount, debitamount, isactualdemand, purpose, createdby, 
				            createddate, lastmodifiedby, lastmodifieddate, 
				            taxheadcode, amount, adjustedamount, demanddetailid, additionaldetails)
		    			VALUES ( billaccount_id, digittenantId, billdetail_id, null, null, demanddetails.taxheadcode, demanddetails.taxamount, demanddetails.collectionamount, False, demanddetails.taxheadcode, '6ccc8719-5b0a-4d24-924e-ec6d2a674b28', Extract(epoch FROM connections.create_date) * 1000, '6ccc8719-5b0a-4d24-924e-ec6d2a674b28', Extract(epoch FROM connections.create_date) * 1000, demanddetails.taxheadcode, demanddetails.collectionamount, 0, demanddetails.id, addetails);
					END LOOP;
					update public.egbs_billdetail_v1 set totalamount=billdetail_total_amount where id=billdetail_id;
			     END LOOP;
		    END IF;
		END LOOP;
	END LOOP;
	return 'Latest bills got migrated successfully';
	EXCEPTION WHEN others THEN
  raise notice 'exception while migrating bill acount details %: %',SQLERRM,SQLSTATE;
END$$  language plpgsql;

--TO start the latest bill migration
select migrate_latest_manual_bills('pb.dhilwan');
