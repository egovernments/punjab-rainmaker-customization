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


class WaterConnectionRequest:
    tenantId: Optional[str]
    propertyId: Optional[str]
    documents: Optional[List]
    plumberInfo: Optional[List]
    roadType: Optional[str]
    roadCuttingArea: Optional[int]
    connectionCategory: Optional[str]
    connectionType: Optional[str]
    additionalDetails: Optional[List]
    rainWaterHarvesting: Optional[bool]
    waterSource: Optional[str]
    meterId: Optional[str]
    meterInstallationDate: Optional[int]
    proposedPipeSize: Optional[int]
    actualPipeSize: Optional[int]
    proposedTaps: Optional[int]
    actualTaps: Optional[int]
    service: Optional[str]
    processInstance: Optional[ProcessInstance]

    def __init__(self, tenant_id: None = None, property_id: None = None, documents: None = None,
                 plumber_info: None = None, road_type: None = None, road_cutting_area: None = None,
                 connection_category: None = None, connection_type: None = None,
                 additional_details: None = None, rain_water_harvesting: None = None,
                 water_source: None = None, meter_id: None = None, meter_installation_date: None = None,
                 proposed_pipe_size: None = None, actual_pipe_size: None = None,
                 proposed_taps: None = None, actual_taps: None = None, service: None = None):
        print(self, tenant_id, property_id, documents)
        self.tenantId = tenant_id
        self.propertyId = property_id
        self.documents = None
        self.plumberInfo = None
        self.roadType = None
        self.roadCuttingArea = None
        self.connectionCategory = connection_category
        self.connectionType = connection_type
        self.additionalDetails = additional_details
        self.rainWaterHarvesting = rain_water_harvesting
        self.waterSource = water_source
        self.meterId = meter_id
        self.meterInstallationDate = meter_installation_date
        self.proposedPipeSize = proposed_pipe_size
        self.actualPipeSize = actual_pipe_size
        self.proposedTaps = proposed_taps
        self.actualTaps = actual_taps
        self.service = service

    def prepare_water_connection(self, json_data, tenant, property_id):
        print(json_data)
        self.tenantId = tenant
        self.propertyId = property_id
        # self.documents = json_data["documents"]
        # self.plumberInfo = json_data["plumber_info"]
        # self.roadType = json_data["road_type"]
        # self.roadCuttingArea = road_cutting_area
        # self.connectionCategory = connection_category
        self.connectionType = json_data["connectiontype"]
        self.additionalDetails = Optional[List]
        self.rainWaterHarvesting = True
        self.waterSource = "GROUND.WELL"
        self.meterId = None
        self.meterInstallationDate = 0
        self.proposedPipeSize = json_data["actualPipeSize"]
        self.actualPipeSize = json_data["actualPipeSize"]
        self.proposedTaps = json_data["actualTaps"]
        self.actualTaps = json_data["actualTaps"]
        self.service = 'Water'
        self.processInstance = self.prepare_process_instance()
        print(self.processInstance.action)

    def prepare_process_instance(self):
        action_initiate = 'INITIATE'
        process_obj = ProcessInstance(action=action_initiate)
        return process_obj

    def upload_water_connection(self, access_token):
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
            "WaterConnection":  self.to_json()

        }
        print(request_data)
        print(urljoin(config.HOST, "/ws-services/wc/_create?"))
        response = requests.post(urljoin(config.HOST, "/ws-services/wc/_create?"), json=request_data)
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
