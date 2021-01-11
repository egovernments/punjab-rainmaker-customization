select json_build_object( 
'paymentMode',(upper(it.type)), 
'paymentStatus', 'New', 
'transactionNumber','transactionNumber',
'transactionDate', 'transactionDate',
'paidBy', ch.payeename,
'mobileNumber', owner.mobilenumber,
'payerName', owner.name,
'consumercode', ch.consumercode,
'payerAddress', billid.citizen_address,
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
	json_build_object('createdby', ch.createdby, 'createddate' , (extract(epoch from date_trunc('milliseconds', ch.createddate)) * 1000),'lastmodifiedby', ch.lastmodifiedby, 'lastmodifieddate' , (extract(epoch from date_trunc('milliseconds', ch.lastmodifieddate)) * 1000)) auditDetails,
	bill.id "billId"

	from  eg_bill bill  where bill.consumer_id=ch.consumercode and bill.id=billid.id group by bill.id
	) paymentDetails ),
'bill', (SELECT json_agg(bill)
	FROM (
	SELECT 
	ch.payeename "paidBy",
	owner.mobilenumber "mobileNumber", 
	ch.payeename "payerName",
	billid.citizen_address "payerAddress",
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
	from  eg_bill bill, eg_bill_details bd where bill.consumer_id=ch.consumercode and bill.id=billid.id and bill.id=bd.id_bill group by bill.id
	) billDetails),
'billAccountDetails', (SELECT json_agg(billAccountDetails)
	FROM (
	SELECT 
	 bill.id "billId",
	 bd.id "billDetailId",
	 dd.id "demandDetailId",
	 drm.order "order",
     rc.code "taxHeadCode"
	from eg_bill bill, eg_bill_details bd, eg_demand_details dd, eg_reason_category rc, eg_demand_reason dr, eg_demand_reason_master drm  where bill.consumer_id=ch.consumercode and bill.id=billid.id and bill.id_demand=dd.id_demand and dd.id_demand_reason=dr.id and dr.id_demand_reason_master=drm.id and drm.category=rc.id limit 1

	) billAccountDetails)
	)	
from egcl_collectionheader ch, egcl_servicedetails billingservice, egcl_collectioninstrument ci, egf_instrumentheader ih, egf_instrumenttype it, egw_status status, eg_user eu, eg_boundary locality, eg_boundary block, egwtr_connection wtrcon, egwtr_connection_owner_info connowner, eg_user owner,eg_address address, eg_bill billid
 where billingservice.id=ch.servicedetails and ci.collectionheader=ch.id and ci.instrumentheader=ih.id and ih.instrumenttype=it.id and ch.status=status.id and billingservice.code='WT' and wtrcon.consumercode=ch.consumercode and ch.consumercode=billid.consumer_id and ch.createdby=eu.id and ch.status in (select id from egw_status where moduletype='ReceiptHeader' and code not in ('PENDING','FAILED')) and locality.id=wtrcon.locality and wtrcon.id=connowner.connection and connowner.owner=owner.id and address.id=wtrcon.address limit 2;





'amount' 
	'adjustedAmount' 


id | installment_num |  installment_year   |     start_date      |      end_date       | id_module |    lastupdatedtimestamp    | description | installment_type | financial_year 
----+-----------------+---------------------+---------------------+---------------------+-----------+----------------------------+-------------+------------------+----------------
  6 |          201004 | 2010-04-01 00:00:00 | 2010-04-01 00:00:00 | 2011-03-31 00:00:00 |       443 | 2018-08-06 15:15:25.305163 | TL_I/10-11  | Yearly           | 
  7 |          201104 | 2011-04-01 00:00:00 | 2011-04-01 00:00:00 | 2012-03-31 00:00:00 |       443 | 2018-08-06 15:15:25.305163 | TL_I/11-12  | Yearly           | 
(2 rows)

\d eg_demand
                           Table "nabha.eg_demand"
     Column      |            Type             |          Modifiers           
-----------------+-----------------------------+------------------------------
 id              | bigint                      | not null
 id_installment  | bigint                      | not null
 base_demand     | double precision            | 
 is_history      | character(1)                | not null default 'N'::bpchar
 create_date     | timestamp without time zone | not null
 modified_date   | timestamp without time zone | not null
 amt_collected   | double precision            | 
 status          | character(1)                | 
 min_amt_payable | double precision            | 
 amt_rebate      | double precision            | 
 version         | bigint                      | default 0
Indexes:


(SELECT json_agg(bills)
FROM (
json_build_object(
	'paidBy', ch.payeename,
	'mobileNumber',  owner.mobilenumber, 
	'payerName', ch.payeename,
	'payerAddress', bill.citizen_address,
	'payerEmail', owner.emailid,
	'payerId', owner.id,
	'status', 'ACTIVE',
	'reasonForCancellation', ch.reasonforcancellation, 
	'isCancelled', bill.is_cancelled,
	'collectionModesNotAllowed', ch.collmodesnotallwd,
	'additionalDetails', '',
	'partPaymentAllowed', ch.partpaymentallowed,
	'minimumAmountToBePaid', ch.minimumamount,
	'businessService', 'WC',
	'totalAmount', ch.totalamount,
	'consumerCode', ch.consumercode,
	'billNumber', bill.bill_no, 
	'billDate', (extract(epoch from date_trunc('milliseconds', bill.issue_date)) * 1000)
	)  ) as bills from  eg_bill bill  where bill.consumer_id=ch.consumercode 



































select ch.consumercode
from egcl_collectionheader ch, egcl_servicedetails billingservice, egcl_collectioninstrument ci, egf_instrumentheader ih, egf_instrumenttype it, egw_status status, eg_user eu, eg_boundary locality, eg_boundary block, egwtr_connection wtrcon, egwtr_connection_owner_info connowner, eg_user owner,eg_address address, eg_bill bill
 where billingservice.id=ch.servicedetails and ci.collectionheader=ch.id and ci.instrumentheader=ih.id and ih.instrumenttype=it.id and ch.status=status.id and ch.collectiontype in ('C','F') and billingservice.code='WT' and wtrcon.consumercode=ch.consumercode and ch.createdby=eu.id and ch.status in (select id from egw_status where moduletype='ReceiptHeader' and code not in ('PENDING','FAILED')) and locality.id=wtrcon.locality and block.id=wtrcon.block and wtrcon.id=connowner.connection and connowner.owner=owner.id and address.id=wtrcon.address and bill.consumer_id=ch.consumercode limit 5;


from egcl_collectionheader ch, egcl_servicedetails billingservice, egcl_collectioninstrument ci, egf_instrumentheader ih, egf_instrumenttype it, egw_status status, eg_user eu, eg_boundary locality, eg_boundary block, egwtr_connection wtrcon, egwtr_connection_owner_info connowner, eg_user owner,eg_address address
 where billingservice.id=ch.servicedetails and ci.collectionheader=ch.id and ci.instrumentheader=ih.id and ih.instrumenttype=it.id and ch.status=status.id and ch.collectiontype in ('C','F') and billingservice.code='WT' and wtrcon.consumercode=ch.consumercode and ch.createdby=eu.id and ch.status in (select id from egw_status where moduletype='ReceiptHeader' and code not in ('PENDING','FAILED')) and locality.id=wtrcon.locality and block.id=wtrcon.block and wtrcon.id=connowner.connection and connowner.owner=owner.id and address.id=wtrcon.address  limit 5;
