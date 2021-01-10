from typing import Optional, List
from uuid import UUID
from urllib.parse import urljoin
import requests
import json
from config import config
import datetime
from common import TIME_PERIOD_MAP, to_json


class Payer:
    uuid: Optional[str]
    tenantId: Optional[str]

    def __init__(self, uuid: Optional[str] = None, tenant_id: Optional[str] = None) -> None:
        self.uuid = uuid
        self.tenantId = tenant_id


class DemandDetail:
    demandId: Optional[str]
    taxHeadMasterCode: Optional[str]
    taxAmount: Optional[float]
    collectionAmount: Optional[float]

    def __init__(self, demand_id: Optional[str] = None,
                 tax_head_master_code: Optional[str] = None,
                 tax_amount: Optional[float] = None,
                 collection_amount: Optional[float] = None) -> None:
        self.demandId = demand_id
        self.taxHeadMasterCode = tax_head_master_code
        self.taxAmount = tax_amount
        self.collectionAmount = collection_amount


class Demand:
    tenantId: Optional[str]
    consumerCode: Optional[str]
    consumerType: Optional[str]
    businessService: Optional[str]
    taxPeriodFrom: Optional[int]
    taxPeriodTo: Optional[int]
    demandDetails: Optional[List[DemandDetail]]
    payer: Optional[Payer]

    def __init__(self, tenant_id: Optional[str] = None,
                 consumer_code: Optional[str] = None,
                 consumer_type: Optional[str] = None,
                 business_service: Optional[str] = None,
                 tax_period_from: Optional[int] = None,
                 tax_period_to: Optional[int] = None,
                 payer: Optional[Payer] = None,
                 demand_details: Optional[List[DemandDetail]] = None) -> None:
        self.tenantId = tenant_id
        self.consumerCode = consumer_code
        self.consumerType = consumer_type
        self.businessService = business_service
        self.taxPeriodFrom = tax_period_from
        self.taxPeriodTo = tax_period_to
        self.payer = payer
        self.demandDetails = demand_details


class WaterDemand:

    # def get_demand_json(self, demands: Optional[List[Demand]]):
    #     demands_encoder = PropertyEncoder().encode(demands)
    #     return convert_jsons(json.loads(demands_encoder), underscore_to_camel)

    def create_demand(self, access_token, demands: Optional[List[Demand]]):
        request_data = {
            "RequestInfo": {
                "authToken": access_token
            },
            "Demands": to_json(demands)
        }
        print("Demand Request-->", request_data)
        response = requests.post(urljoin(config.HOST, "/billing-service/demand/_create?tenantId="), json=request_data)
        res = response.json()
        return request_data, res

    def prepare_demand(self, context, tenant_id, property_id, owner_uuid):
        demand_list = []
        negative_int = -1
        for dcb_record in context['dcb']:
            demand_record = Demand(consumer_code=property_id, consumer_type='BUILTUP', business_service='WS',
                                   tax_period_from=1554076800000, tax_period_to=1617175799000,
                                   demand_details=[self.prepare_demand_details('WS_CHARGE', dcb_record['taxamount'], dcb_record['taxcollection'])],
                                   payer=Payer(uuid=owner_uuid), tenant_id=tenant_id)

            # taxPeriod_from = TIME_PERIOD_MAP[dcb_record['fromdate']],
            # taxPeriod_to = TIME_PERIOD_MAP[dcb_record['todate']],
            # dummy demand details entry for penalty, interest and rebate
            demand_record.demandDetails.append(self.prepare_demand_details('WS_TIME_PENALTY', 0, 0))

            if dcb_record['penaltyamount'] != 0 or dcb_record['penaltycollection'] != 0:
                demand_record.demandDetails.append(self.prepare_demand_details('WS_TIME_INTEREST', dcb_record['penaltyamount'], dcb_record['penaltycollection']))
            else:
                demand_record.demandDetails.append(self.prepare_demand_details('WS_TIME_INTEREST', 0, 0))

            if dcb_record['rebate'] != 0:
                demand_record.demandDetails.append(self.prepare_demand_details('WS_TIME_REBATE', dcb_record['rebate']*negative_int, 0))
            else:
                demand_record.demandDetails.append(self.prepare_demand_details('WS_TIME_REBATE', 0, 0))

            demand_list.append(demand_record)

        #Current demand with dummy taxes
        '''demand_record = Demand(consumer_code=property_id, consumer_type='BUILTUP', business_service='PT',
                               taxPeriod_from=1554076800000,
                               taxPeriod_to=1585699199000,
                               demand_details=[self.prepare_demand_details('PT_TAX', 500, 0)],
                               payer=Payer(uuid=owner_uuid), tenant_id=tenant_id)
        # dummy demand details entry for penalty, interest and rebate
        demand_record.demand_details.append(self.prepare_demand_details('PT_LATE_ASSESSMENT_PENALTY', 0, 0))
        demand_record.demand_details.append(self.prepare_demand_details('PT_TIME_INTEREST', 0, 0))
        demand_record.demand_details.append(self.prepare_demand_details('PT_TIME_REBATE', 0, 0))
        demand_list.append(demand_record)'''

        return demand_list

    def prepare_demand_details(self, tax_head, tax_amount, amt_collected):
        return DemandDetail(tax_head_master_code=tax_head, tax_amount=tax_amount,
                            collection_amount=amt_collected)

    def generate_penalty(self, access_token, tenant_id=None, consumer_codes=None):
        request_data = {
            "RequestInfo": {
                "authToken": access_token
            }
        }
        url_string = "/ws-calculator/waterCalculator/_updateDemand?tenantId=" + tenant_id + "&consumerCodes=" + consumer_codes
        # print("Penalty url_string-->", url_string)
        # print("Penalty Request-->", request_data)
        response = requests.post(urljoin(config.HOST, url_string),
                                 json=request_data)
        res = response.json()
        return request_data, res

    def generate_bill(self, access_token, tenant_id, consumer_code):
        request_data = {
            "RequestInfo": {
                "authToken": access_token
            }
        }
        url_string = "/billing-service/bill/v2/_fetchbill?tenantId="+tenant_id+"&consumerCode="+consumer_code+"&businessService=WS"
        print("Bill url_string-->", url_string)
        print("Bill Request-->", request_data)
        response = requests.post(urljoin(config.HOST, url_string),
                                 json=request_data)
        res = response.json()
        return request_data, res



class RequestInfo:
    api_id: Optional[str]
    ver: Optional[str]
    ts: Optional[str]
    action: Optional[str]
    did: Optional[int]
    key: Optional[str]
    msg_id: Optional[str]
    auth_token: Optional[UUID]

    def __init__(self, api_id: Optional[str] = None, ver: Optional[str] = None, ts: Optional[str] = None,
                 action: Optional[str] = None, did: Optional[int] = None, key: Optional[str] = None,
                 msg_id: Optional[str] = None, auth_token: Optional[UUID] = None) -> None:
        self.api_id = api_id
        self.ver = ver
        self.ts = ts
        self.action = action
        self.did = did
        self.key = key
        self.msg_id = msg_id
        self.auth_token = auth_token


class DemandCreateRequest:
    request_info: Optional[RequestInfo]
    demands: Optional[List[Demand]]

    def __init__(self, request_info: Optional[RequestInfo] = None,
                 demands: Optional[List[Demand]] = None) -> None:
        self.request_info = request_info
        self.demands = demands