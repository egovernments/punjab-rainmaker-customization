select json_build_object( 
	'paymentMode',(upper(it.type)), 
	'paymentStatus', 'New', 
	'transactionNumber','transactionNumber',
	'transactionDate', 'transactionDate',
	'paidBy', ch.payeename,
	'mobileNumber', owner.mobilenumber,
	'payerName', owner.name,
	'consumercode', ch.consumercode,
	'payerAddress', (COALESCE(address.houseNoBldgApt||', ', '')||COALESCE(address.areaLocalitySector||', ','')||COALESCE(address.streetRoadLine||', ','')||COALESCE(address.landmark||', ','')||COALESCE(address.cityTownVillage||', ','')||COALESCE(address.postOffice||', ','')||COALESCE(address.subdistrict||', ','')||COALESCE(address.district||', ','')||COALESCE(address.state||', ','')||COALESCE(address.country||', ','')||COALESCE(address.pinCode,'')),
	'payerEmail', owner.emailid,
	'payerId', owner.id,
	'totalAmountPaid', ch.totalamount,
	'totalDue', (select sum(dd.amount - dd.amt_collected) from egwtr_connectiondetails conndetails, egwtr_connection wtrcon, egwtr_demand_connection dc, eg_demand d, eg_demand_details dd 
where wtrcon.consumercode=ch.consumercode and wtrcon.id=conndetails.connection and dc.connectiondetails=conndetails.connection and d.id = dc.demand and d.is_history='N' and dd.id_demand = d.id),
	'instrumentDate',(select ih.instrumentdate from egcl_collectioninstrument ci, egf_instrumentheader ih, egf_instrumenttype it where ci.collectionheader=ch.id and ci.instrumentheader=ih.id and ih.instrumenttype=it.id),
	'instrumentNumber', (select ih.instrumentNumber from egcl_collectioninstrument ci, egf_instrumentheader ih, egf_instrumenttype it where ci.collectionheader=ch.id and ci.instrumentheader=ih.id and ih.instrumenttype=it.id),
	'instrumentStatus',(select status.code from egcl_collectioninstrument ci, egf_instrumentheader ih, egf_instrumenttype it,egw_status status where ci.collectionheader=ch.id and ci.instrumentheader=ih.id and ih.instrumenttype=it.id and ih.id_status=status.id),
'paymentDetails', 
(SELECT json_agg(paymentDetails)
FROM (
SELECT  
'' "id", 
'' "tenantId", 
(select sum(dd.amount - dd.amt_collected) from  egwtr_connectiondetails conndetails, egwtr_connection wtrcon, egwtr_demand_connection dc, eg_demand d, eg_demand_details dd 
where wtrcon.consumercode=ch.consumercode and wtrcon.id=conndetails.connection and dc.connectiondetails=conndetails.connection and d.id = dc.demand and d.is_history='N' and dd.id_demand = d.id) "totalDue", 
(select sum(dd.amt_collected) from  egwtr_connectiondetails conndetails, egwtr_connection wtrcon, egwtr_demand_connection dc, eg_demand d, eg_demand_details dd 
where wtrcon.consumercode=ch.consumercode and wtrcon.id=conndetails.connection and dc.connectiondetails=conndetails.connection and d.id = dc.demand and d.is_history='N' and dd.id_demand = d.id) "totalAmountPaid", 
ch.manualreceiptnumber "manualReceiptNumber", 
ch.receiptnumber "receiptNumber", 
ch.receipttype "receiptType", 
(extract(epoch from date_trunc('milliseconds', ch.receiptdate)) * 1000) "receiptDate",
'WC' "businessService", 
'' "additionalDetails",
json_build_object('createdby', ch.createdby, 'createddate' , (extract(epoch from date_trunc('milliseconds', ch.createddate)) * 1000),'lastmodifiedby', ch.lastmodifiedby, 'lastmodifieddate' , (extract(epoch from date_trunc('milliseconds', ch.lastmodifieddate)) * 1000)) auditDetails

from  eg_bill bill  where bill.consumer_id=ch.consumercode and bill.id=billid.id group by bill.id
) paymentDetails ),
'bill', (SELECT json_agg(bill)
FROM (
SELECT 
ch.payeename "paidBy",
owner.mobilenumber "mobileNumber", 
ch.payeename "payerName",
bill.citizen_address "payerAddress",
owner.emailid "payerEmail",
owner.id "payerId",
'ACTIVE' "status",
ch.reasonforcancellation "reasonForCancellation", 
bill.is_cancelled "isCancelled",
ch.collmodesnotallwd "collectionModesNotAllowed",
'' "additionalDetails",
ch.partpaymentallowed "partPaymentAllowed", 
ch.minimumamount "minimumAmountToBePaid",
'WC' "businessService", 
ch.totalamount "totalAmount",
ch.consumercode "consumerCode",
bill.bill_no "billNumber", 
bill.id "billId",
(extract(epoch from date_trunc('milliseconds', bill.issue_date)) * 1000) "billDate"
from  eg_bill bill  where bill.consumer_id=ch.consumercode and bill.id=billid.id group by bill.id
) bill),

'billDetails', (SELECT json_agg(billDetails)
FROM (
SELECT 
bill.id_demand "demandId",
bill.id "billId",
bill.total_amount "amount",
bill.total_collected_amount "amountPaid",
(select install.start_date from eg_demand demand, eg_installment_master install 
	where bill.id_demand=demand.id and demand.id_installment=install.id) "fromPeriod", 
(select install.start_date from eg_demand demand, eg_installment_master install 
	where bill.id_demand=demand.id and demand.id_installment=install.id)  "toPeriod",
(extract(epoch from date_trunc('milliseconds', ch.receiptdate)) * 1000) "receiptDate",
ch.receipttype "receiptType",
ch.source "channel",
locality.name "boundary",
ch.manualreceiptnumber "manualReceiptNumber",
(extract(epoch from date_trunc('milliseconds', ch.manualreceiptdate)) * 1000) "manualReceiptDate",
ch.collectiontype "collectionType",
bill.description "billDescription",
(extract(epoch from date_trunc('milliseconds', bill.last_date)) * 1000) "expiryDate",
bill.dspl_message "displayMessage",
ch.reasonforcancellation "cancellationRemarks"
from  eg_bill bill, eg_demand d  where bill.consumer_id=ch.consumercode and bill.id=billid.id group by bill.id
) billDetails) 
)	
from egcl_collectionheader ch, egcl_servicedetails billingservice, egcl_collectioninstrument ci, egf_instrumentheader ih, egf_instrumenttype it, egw_status status, eg_user eu, eg_boundary locality, eg_boundary block, egwtr_connection wtrcon, egwtr_connection_owner_info connowner, eg_user owner,eg_address address, eg_bill billid
 where billingservice.id=ch.servicedetails and ci.collectionheader=ch.id and ci.instrumentheader=ih.id and ih.instrumenttype=it.id and ch.status=status.id and ch.collectiontype in ('C','F') and billingservice.code='WT' and wtrcon.consumercode=ch.consumercode and ch.consumercode=billid.consumer_id and ch.createdby=eu.id and ch.status in (select id from egw_status where moduletype='ReceiptHeader' and code not in ('PENDING','FAILED')) and locality.id=wtrcon.locality and block.id=wtrcon.block and wtrcon.id=connowner.connection and connowner.owner=owner.id and address.id=wtrcon.address limit 2;

 ==================================================================










 and ch.consumercode='2113000715';


and (ch.createddate::date>='2020-08-28'::date or ch.lastmodifieddate::date>='2020-08-28'::date)

union all 

select json_build_object(  '_index', 'receipts-consumers', '_type', 'receipts_bifurcation', '_id', (select code from eg_city)||'_'||ch.receiptNumber, '_score', 1, '_source', (select json_build_object( 'paymentmode',it.type , 'status', status.description, 'channel', ch.source, 'cityname', (SELECT CASE WHEN name like '%UAT%' THEN (SELECT split_part(name,'-',1) from eg_city) ELSE name END from eg_city), 'consumercode', ch.consumercode, 'consumertype', (select name from egwtr_usage_type where id in (select cd.usagetype from  egwtr_connectiondetails cd where wtrcon.id=cd.connection and wtrcon.consumercode = ch.consumercode)), 'currentamount', (select sum(cramount) from egcl_collectiondetails colld where ch.id=colld.collectionheader and colld.purpose ='CURRENT_AMOUNT'), 'id', (select code from eg_city)||'_'||ch.receiptNumber, 'receiptnumber', ch.receiptNumber, 'arrearamount',(select sum(cramount) from egcl_collectiondetails colld where ch.id=colld.collectionheader and colld.purpose ='ARREAR_AMOUNT'), 'receiptcreator', eu.name, 'consumername', ch.payeename, 'advanceamount', (select sum(cramount) from egcl_collectiondetails colld where ch.id=colld.collectionheader and colld.purpose ='ADVANCE_AMOUNT'), 'locality', locality.name, 'billnumber', ch.referencenumber, 'regionname', (select regionname from eg_city), 'latepaymentcharges', (select sum(cramount) from egcl_collectiondetails colld where ch.id=colld.collectionheader and colld.purpose in ('ARREAR_LATEPAYMENT_CHARGES','CURRENT_LATEPAYMENT_CHARGES')), 'interestamount',(select sum(cramount) from egcl_collectiondetails colld where ch.id=colld.collectionheader and colld.purpose in ('ARREAR_PENALTY_CHARGES','CURRENT_PENALTY_CHARGES')), 'totalamount', ch.totalamount, 'paymentgateway', paymentservice.name, 'block', block.name, 'districtname', (select districtname from eg_city), 'installmentfrom', (select right(left(colld.description, strpos(colld.description, 'Q') + 1), 12) from egcl_collectiondetails colld where ch.id=colld.collectionheader and colld.ordernumber = (select min(ordernumber) from egcl_collectiondetails where collectionheader=ch.id)), 'installmentto', (select right(left(colld.description, strpos(colld.description, 'Q') + 1), 12) from egcl_collectiondetails colld where ch.id=colld.collectionheader and colld.ordernumber=(select max(ordernumber) from egcl_collectiondetails where cramount> 0 and collectionheader=ch.id)), 'receiptdate', DATE(ch.receiptdate) , 'citygrade', (select grade from eg_city), 'citycode', (select code from eg_city), 'createddate', ch.createddate, 'billingservice', billingservice.name, '@version', '1', '@timestamp', DATE(ch.receiptdate), 'pwssb',CASE (select value from eg_appconfig_values where key_id in (select id from eg_appconfig where key_name ='IS_PWSSB_ULB')) WHEN 'YES' THEN true ELSE false END ) _source)) 


from egcl_collectionheader ch,egcl_servicedetails billingservice, egcl_collectioninstrument ci, egf_instrumentheader ih, egf_instrumenttype it, egcl_onlinepayments op, egcl_servicedetails paymentservice, egw_status status, eg_user eu, eg_boundary locality, eg_boundary block, egwtr_connection wtrcon where billingservice.id=ch.servicedetails and ci.collectionheader=ch.id and ci.instrumentheader=ih.id and ih.instrumenttype=it.id and ch.id=op.collectionheader and op.servicedetails=paymentservice.id and ch.status=status.id and ch.collectiontype='O' and billingservice.code='WT' and wtrcon.consumercode=ch.consumercode and ch.createdby=eu.id and ch.status in (select id from egw_status where moduletype='ReceiptHeader' and code not in ('PENDING','FAILED')) and locality.id=wtrcon.locality and block.id=wtrcon.block and (ch.createddate::date>='2020-08-28'::date or ch.lastmodifieddate::date>='2020-08-28'::date) 

