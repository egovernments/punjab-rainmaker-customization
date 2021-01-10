import json
import os
import time
import psycopg2
import sys
from typing import Optional, List
from json import JSONEncoder
import requests
from urllib.parse import urljoin
from config import config
from uploader.parsers.utils import WaterConnectionRequestEncoder, convert_json, underscore_to_camel
import jsonpickle


class AuditDetails:
    createdBy: Optional[str]
    lastModifiedBy: Optional[str]
    createdTime: Optional[int]
    lastModifiedTime: Optional[int]

    def __init__(self, created_by: None = None,last_modified_by: None = None,created_time: None = None,last_modified_time: None = None):
        self.createdBy = created_by
        self.lastModifiedBy = last_modified_by
        self.createdTime = created_time
        self.lastModifiedTime = last_modified_time


class Document:
    id: Optional[str]
    documentType: Optional[str]
    documentUid: Optional[str]
    additionalDetails: Optional[str]
    fileStoreId: Optional[str]
    status: Optional[str]

    def __init__(self, id: None = None, document_type: None = None, document_uid: None = None,
                 additional_details: None = None) -> None:
        self.id = id
        self.documentType = document_type
        self.documentUid = document_uid
        self.additionalDetails = additional_details


class ProcessInstance:
    action: Optional[str]
    def __init__(self, action: None = None):
        self.action = action


class SewerageConnectionRequest:
    tenantId: Optional[str]
    propertyId: Optional[str]
    applicationNo: Optional[str]
    connectionNo: Optional[str]
    oldConnectionNo: Optional[str]
    applicationStatus: Optional[str]
    status: Optional[str]
    documents: Optional[List]
    plumberInfo: Optional[List]
    roadType: Optional[str]
    roadCuttingArea: Optional[int]
    connectionExecutionDate: Optional[int]
    connectionCategory: Optional[str]
    connectionType: Optional[str]
    additionalDetails: Optional[List]
    processInstance: Optional[ProcessInstance]
    auditDetails: Optional[AuditDetails]
    proposedWaterClosets: Optional[int]
    proposedToilets: Optional[int]
    noOfWaterClosets: Optional[int]
    noOfToilets: Optional[int]
    water: Optional[bool]
    sewerage: Optional[bool]
    service: Optional[str]

    def __init__(self, tenant_id: None = None, property_id: None = None, application_no: None = None, connection_no: None = None,
                 old_connection_no: None = None, application_status: None = None, status: None = None,
                 documents: None = None, plumber_info: None = None,
                 road_type: None = None, road_cutting_area: None = None,
                 connection_execution_date: None = None, connection_category: None = None,
                 connection_type: None = None,
                 additional_details: None = None, process_instance: None = None,
                 audit_details: None = None, proposed_water_closets: None = None, proposed_toilets: None = None,
                 no_of_water_closets: None = None,no_of_toilets: None = None, water: None = None, sewerage: None = None, service: None = None):
        print(self, tenant_id, property_id, documents)
        self.tenantId = tenant_id
        self.propertyId = property_id
        self.applicationNo = application_no
        self.connectionNo = connection_no
        self.oldConnectionNo = old_connection_no
        self.applicationStatus = application_status
        self.status = status
        self.documents = documents
        self.plumberInfo = plumber_info
        self.roadType = road_type
        self.roadCuttingArea = road_cutting_area
        self.connectionExecutionDate = connection_execution_date
        self.connectionCategory = connection_category
        self.connectionType = connection_type
        self.additionalDetails = additional_details
        self.processInstance = process_instance
        self.auditDetails = audit_details
        self.proposedWaterClosets = proposed_water_closets
        self.proposedToilets = proposed_toilets
        self.noOfWaterClosets: no_of_water_closets
        self.noOfToilets: no_of_toilets
        self.water = water
        self.sewerage = sewerage
        self.service = service

    def prepare_sewerage_connection(self, json_data, tenant, property_id):
        print(json_data)
        self.tenantId = tenant
        self.propertyId = property_id
        # json_data["applicationnumber"]
        self.applicationNo = None
        self.connectionNo = None
        self.oldConnectionNo = None
        self.applicationStatus = json_data["connectionstatus"]
        self.status = 'Active'
        self.documents = None
        self.plumberInfo = None
        self.roadType = 'BERMCUTTINGKATCHA'
        # self.get_road_type(json_data["roadType"])
        self.roadCuttingArea = 3213
        # json_data["roadCuttingArea"]
        self.connectionExecutionDate = None
        self.connectionCategory = None
        self.connectionType = "Non Metered"
        self.additionalDetails = Optional[List]
        self.processInstance = self.prepare_process_instance()
        self.auditDetails = self.prepare_audit_details()
        self.proposedWaterClosets = self.prepare_closets(json_data)
        self.proposedToilets = self.prepare_closets(json_data)
        self.noOfWaterClosets = self.prepare_closets(json_data)
        self.noOfToilets = self.prepare_closets(json_data)
        self.water = False
        self.sewerage = True
        self.service = 'Sewerage'
        print(self.processInstance.action)

    def prepare_closets(self, json_data):
        noofseatsnonresidential = json_data["noofseatsnonresidential"]
        noofseatsresidential = json_data["noofseatsresidential"]

        if noofseatsnonresidential is not None:
            return noofseatsnonresidential
        elif noofseatsresidential is not None:
            return noofseatsresidential
        else:
            return 1

    def prepare_process_instance(self):
        action_initiate = 'INITIATE'
        process_obj = ProcessInstance(action=action_initiate)
        return process_obj

    def prepare_audit_details(self):
        created_by_init = 'f9065e25-ece0-4bb6-828e-bf1ebb6fbda9'
        last_modified_by_init = 'f9065e25-ece0-4bb6-828e-bf1ebb6fbda9'
        created_time_init = int(round(time.time() * 1000))
        last_modified_time_init = int(round(time.time() * 1000))
        audit_details = AuditDetails(created_by=created_by_init,last_modified_by=last_modified_by_init,created_time=created_time_init,last_modified_time=last_modified_time_init)
        return audit_details

    def get_road_type(road_type):
        switcher = {
            "Premix Carpet": "PREMIXCORPET",
            "BM and Premix Road": "BMPREMIXROAD",
            "Berm Cutting (Katcha)": "BERMCUTTINGKATCHA",
            "Brick Paving": "BRICKPAVING",
            "CC Road": "CCROAD",
            "Interlocking Paver Block": "INTERLOCKINGPAVERBLOCK",
            "Open Pipe": "OPENPIPE",
        }
        return switcher.get(road_type, "")

    def upload_sewerage_connection(self, access_token):
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
            "SewerageConnection":  self.to_json()

        }
        print(request_data)
        print(urljoin(config.HOST, "/sw-services/swc/_create?"))
        response = requests.post(urljoin(config.HOST, "/sw-services/swc/_create?"), json=request_data)
        res = response.json()
        return request_data, res

    # def get_self_json(self):
    # return json.dumps(self.__dict__)

    def get_water_json(self):
        print("Printing JSON Object")
        print(WaterConnectionRequestEncoder().encode(self))
        water_encoder = WaterConnectionRequestEncoder().encode(self)
        return convert_json(json.loads(water_encoder), underscore_to_camel)

    def to_json(self):
        json_obj_encode = jsonpickle.encode(self, unpicklable=False)
        json_data = json.dumps(json_obj_encode, indent=4)
        json_obj_decode = json.loads(jsonpickle.decode(json_data))
        print(json_obj_decode)
        return json_obj_decode

