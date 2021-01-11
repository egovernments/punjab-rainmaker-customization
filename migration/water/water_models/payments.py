from typing import Optional, List
from config import config
from urllib.parse import urljoin
from common import superuser_login, to_json
import requests


class BillAccountDetails:
    id: Optional[str]
    tenantId: Optional[str]
    billDetailId: Optional[str]
    demandDetailId: Optional[str]
    order: Optional[int]
    amount: Optional[int]
    adjustedAmount: Optional[int]
    taxHeadCode: Optional[str]
    additionalDetails: Optional
    purpose: Optional[str]

    def __init__(self, id: None = None, tenant_id: None = None, bill_detail_id: None = None,
             demand_detail_id: None = None, order: None = None, amount: None = None,
             adjusted_amount: None = None, tax_head_code: None = None,
             additional_details: None = None, purpose: None = None):
        self.id: id
        self.tenantId: tenant_id
        self.billDetailId: bill_detail_id
        self.demandDetailId: demand_detail_id
        self.order: order
        self.amount: amount
        self.adjustedAmount: adjusted_amount
        self.taxHeadCode: tax_head_code
        self.additionalDetails: additional_details
        self.purpose: purpose


class BillDetails:
    id: Optional[str]
    tenantId: Optional[str]
    demandId: Optional[str]
    billId: Optional[str]
    amount: Optional[int]
    amountPaid: Optional[int]
    fromPeriod: Optional[int]
    toPeriod: Optional[int]
    receiptDate: Optional[str]
    receiptType: Optional[List[str]]
    channel: Optional[str]
    boundary: Optional[List[str]]
    manualReceiptNumber: Optional[str]
    manualReceiptDate: Optional[int]
    collectionType: Optional[str]
    billDescription: Optional[str]
    expiryDate: Optional[int]
    displayMessage: Optional[str]
    cancellationRemarks: Optional[str]
    billAccountDetails: Optional[BillAccountDetails]

    def __init__(self, id: None = None, tenant_id: None = None, demand_id: None = None,
             bill_id: None = None, amount: None = None, amount_paid: None = None,
             from_period: None = None, to_period: None = None,
             receipt_date: None = None, receipt_type: None = None,
             channel: None = None, boundary: None = None, manual_receipt_number: None = None,
             manual_receipt_date: None = None, collection_type: None = None, bill_description: None = None,
             expiry_date: None = None, display_message: None = None, cancellation_remarks: None = None,
             bill_account_details: None = None):
        self.id = id
        self.tenantId = tenant_id
        self.demandId = demand_id
        self.billId = bill_id
        self.amount = amount
        self.amountPaid = amount_paid
        self.fromPeriod = from_period
        self.toPeriod = to_period
        self.receiptDate = receipt_date
        self.receiptType = receipt_type
        self.channel = channel
        self.boundary = boundary
        self.manualReceiptNumber = manual_receipt_number
        self.manualReceiptDate = manual_receipt_date
        self.collectionType = collection_type
        self.billDescription = bill_description
        self.expiryDate = expiry_date
        self.displayMessage = display_message
        self.cancellationRemarks = cancellation_remarks
        self.billAccountDetails = bill_account_details


class Bill:
    paidBy: Optional[str]
    mobileNumber: Optional[str]
    payerName: Optional[str]
    payerAddress: Optional[str]
    payerEmail: Optional[str]
    payerId: Optional[str]
    status: Optional[str]
    reasonForCancellation: Optional[str]
    isCancelled: Optional[bool]
    additionalDetails: Optional
    collectionModesNotAllowed: Optional[List[str]]
    partPaymentAllowed: Optional[bool]
    isAdvanceAllowed: Optional[bool]
    minimumAmountToBePaid: Optional[int]
    businessService: Optional[str]
    totalAmount: Optional[int]
    consumerCode: Optional[str]
    billNumber: Optional[str]
    billDate: Optional[int]
    billDetails: Optional[List[BillDetails]]

    def __init__(self, paid_by: None = None, mobile_number: None = None, payer_name: None = None,
             payer_address: None = None, payer_email: None = None, payer_id: None = None,
             status: None = None, reason_for_cancellation: None = None,
             is_cancelled: None = None, additional_details: None = None,
             collection_modes_not_allowed: None = None, part_payment_allowed: None = None,
             is_advance_allowed: None = None, minimum_amount_to_be_paid: None = None, business_service: None = None,
             total_amount: None = None, consumer_code: None = None, bill_number: None = None, bill_bate: None = None,
             bill_details: None = None):
        self.paidBy = paid_by
        self.mobileNumber = mobile_number
        self.payerName = payer_name
        self.payerAddress = payer_address
        self.payerEmail = payer_email
        self.payerId = payer_id
        self.status = status
        self.reasonForCancellation = reason_for_cancellation
        self.isCancelled = is_cancelled
        self.additionalDetails = additional_details
        self.collectionModesNotAllowed = collection_modes_not_allowed
        self.partPaymentAllowed = part_payment_allowed
        self.isAdvanceAllowed = is_advance_allowed
        self.minimumAmountToBePaid = minimum_amount_to_be_paid
        self.businessService = business_service
        self.totalAmount = total_amount
        self.consumerCode = consumer_code
        self.billNumber = bill_number
        self.billDate = bill_bate
        self.billDetails = bill_details


class PaymentDetails:
    id: Optional[str]
    tenantId: Optional[str]
    totalDue: Optional[int]
    totalAmountPaid: Optional[int]
    manualReceiptNumber: Optional[str]
    receiptNumber: Optional[str]
    receiptType: Optional[str]
    receiptDate: Optional[int]
    businessService: Optional[str]
    billId: Optional[str]
    bill: Optional[List[Bill]]
    additionalDetails: Optional[int]

    def __init__(self,  id: None = None, tenant_id: None = None, total_due: None = None,
                 total_amount_paid: None = None, manual_receipt_number: None = None,
                 receipt_number: None = None, receipt_type: None = None, receipt_date: None = None,
                 business_service: None = None, bill_id: None = None,
                 bill: None = None, additional_details: None = None):
        self.id = id
        self.tenantId = tenant_id
        self.totalDue = total_due
        self.totalAmountPaid = total_amount_paid
        self.manualReceiptNumber = manual_receipt_number
        self.receiptNumber = receipt_number
        self.receiptType = receipt_type
        self.receiptDate = receipt_date
        self.businessService = business_service
        self.billId = bill_id
        self.bill = bill
        self.additionalDetails = additional_details


class Payments:
    tenantId: Optional[str]
    id: Optional[str]
    totalDue: Optional[int]
    totalAmountPaid: Optional[int]
    transactionNumber: Optional[str]
    transactionDate: Optional[int]
    paymentMode: Optional[str]
    instrumentDate: Optional[int]
    instrumentNumber: Optional[str]
    instrumentStatus: Optional[str]
    ifscCode: Optional[str]
    additionalDetails: {}
    paymentDetails: Optional[List[PaymentDetails]]
    paidBy: Optional[str]
    mobileNumber: Optional[str]
    payerName: Optional[str]
    payerAddress: Optional[str]
    payerEmail: Optional[str]
    payerId: Optional[str]
    paymentStatus: Optional[str]

    def __init__(self, tenant_id: None = None, id: None = None, total_due: None = None,
                 total_amount_paid: None = None, transaction_number: None = None, transaction_date: None = None,
                 payment_mode: None = None, instrument_date: None = None,
                 instrument_number: None = None, instrument_status: None = None,
                 ifsc_code: None = None, additional_details: None = None, payment_details: None = None,
                 paid_by: None = None, mobile_number: None = None,
                 payer_name: None = None, payer_address: None = None, payer_email: None = None, payer_id: None = None,
                 payment_status: None = None):
        self.tenantId = tenant_id
        self.id = id
        self.totalDue = total_due
        self.totalAmountPaid = total_amount_paid
        self.transactionNumber = transaction_number
        self.transactionDate = transaction_date
        self.paymentMode = payment_mode
        self.instrumentDate = instrument_date
        self.instrumentNumber = instrument_number
        self.instrumentStatus = instrument_status
        self.ifscCode = ifsc_code
        self.additionalDetails = additional_details
        self.paymentDetails = payment_details
        self.paidBy = paid_by
        self.mobileNumber = mobile_number
        self.payerName = payer_name
        self.payerAddress = payer_address
        self.payerEmail = payer_email
        self.payerId = payer_id
        self.paymentStatus = payment_status

    def prepare_payments(self, json_data, tenant_id):
        self.tenantId = tenant_id
        self.id = None
        self.totalDue = json_data["totalDue"]
        self.totalAmountPaid = json_data["totalAmountPaid"]
        self.transactionNumber = json_data["transactionNumber"]
        self.transactionDate = json_data["transactionDate"]
        self.paymentMode = json_data["paymentMode"]
        self.instrumentDate = json_data["instrumentDate"]
        self.instrumentNumber = json_data["instrumentNumber"]
        self.instrumentStatus = json_data["instrumentStatus"]
        self.ifscCode = None
        self.additionalDetails = None
        self.paymentDetails = self.prepare_payment_details(json_data, tenant_id)
        self.paidBy = json_data["paidBy"]
        self.mobileNumber = json_data["mobileNumber"]
        self.payerName = json_data["payerName"]
        self.payerAddress = json_data["payerAddress"]
        self.payerEmail = json_data["payerEmail"]
        self.payerId = json_data["payerId"]
        self.paymentStatus = json_data["paymentStatus"]

    def prepare_payment_details(self, json_data, tenant_id):
        # payment_details_json = json_data["paymentDetails"]
        payment_details_list = []

        for payment_details_json in json_data["paymentDetails"]:
            paymentDetails = PaymentDetails(id=None, tenant_id=tenant_id, total_due=json_data["totalDue"],
                                            total_amount_paid=json_data["totalAmountPaid"], manual_receipt_number=payment_details_json["manualReceiptNumber"],
                                            receipt_number=payment_details_json["receiptNumber"], receipt_type=payment_details_json["receiptType"],
                                            receipt_date=payment_details_json["receiptDate"], business_service=payment_details_json["businessService"],
                                            bill_id=payment_details_json["billId"], bill=self.prepare_bill(json_data, tenant_id), additional_details=None)
            payment_details_list.append(paymentDetails)

        return payment_details_list

    def prepare_bill(self, json_data, tenant_id):
        #  bill_json = json_data["bill"]
        bill_details_list = []

        for bill_json in json_data["bill"]:

            bill = Bill(paid_by=bill_json["paidBy"], mobile_number=bill_json["mobileNumber"], payer_name=bill_json["payerName"],
             payer_address=bill_json["payerAddress"], payer_email=bill_json["payerEmail"], payer_id=bill_json["payerId"],
             status=bill_json["status"], reason_for_cancellation=bill_json["reasonForCancellation"], is_cancelled=bill_json["isCancelled"],
                        additional_details=None, collection_modes_not_allowed=bill_json["collectionModesNotAllowed"],
                        part_payment_allowed=bill_json["partPaymentAllowed"], is_advance_allowed=False, minimum_amount_to_be_paid=bill_json["minimumAmountToBePaid"],
                        business_service=bill_json["businessService"], total_amount=bill_json["totalAmount"], consumer_code=bill_json["consumerCode"],
                        bill_number=bill_json["billNumber"], bill_bate=bill_json["billDate"], bill_details=self.prepare_bill_details(json_data, tenant_id))
            bill_details_list.append(bill)

        return bill_details_list

    def prepare_bill_details(self, json_data, tenant_id):
        # bill_details_json = json_data["billDetails"]
        bill_details_list = []

        for bill_details_json in json_data["billDetails"]:

            bill_detail = BillDetails(id=None, tenant_id=tenant_id, demand_id=bill_details_json["demandId"], bill_id=bill_details_json["billId"], amount=bill_details_json["amount"], amount_paid=bill_details_json["amountPaid"], from_period=bill_details_json["fromPeriod"], to_period=bill_details_json["toPeriod"], receipt_date=bill_details_json["receiptDate"], receipt_type=bill_details_json["receiptType"], channel=bill_details_json["channel"], boundary=bill_details_json["boundary"], manual_receipt_number=bill_details_json["manualReceiptNumber"], manual_receipt_date=bill_details_json["manualReceiptDate"], collection_type=bill_details_json["collectionType"], bill_description=bill_details_json["billDescription"], expiry_date=bill_details_json["expiryDate"], display_message=bill_details_json["displayMessage"], cancellation_remarks=bill_details_json["cancellationRemarks"], bill_account_details=self.prepare_bill_account_details(json_data, tenant_id))
            bill_details_list.append(bill_detail)

        return bill_details_list

    def prepare_bill_account_details(self, json_data, tenant_id):
        # bill_account_details_json = json_data["billAccountDetails"]
        bill_account_details_list = []

        for bill_account_details_json in json_data["billAccountDetails"]:

            bill_account_details = BillAccountDetails(id=None, tenant_id=tenant_id, bill_detail_id=bill_account_details_json["billDetailId"], demand_detail_id=bill_account_details_json["demandDetailId"], order=bill_account_details_json["order"], amount=0, adjusted_amount=0, tax_head_code=bill_account_details_json["taxHeadCode"], additional_details=None, purpose=None)
            bill_account_details_list.append(bill_account_details)

        return bill_account_details_list

    def upload_water_collection(self, access_token):
        print("hello", access_token)
        # print("Water Json : ", self.get_water_json())
        request_data = {
            "RequestInfo": {
                "apiId": "Rainmaker",
                "ver": ".01",
                "action": "",
                "did": "1",
                "key": "",
                "msgId": "20170310130900|en_IN",
                "requesterId": "",
                "authToken": access_token
            },
            "Payment": to_json(self)

        }
        print(request_data)
        print(urljoin(config.HOST, "/payments/_create?"))
        time.sleep(0.01)
        response = requests.post(urljoin(config.HOST, "/payments/_create?"), json=request_data)
        res = response.json()
        return request_data, res












