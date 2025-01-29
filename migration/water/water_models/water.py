import json
from typing import Optional, List
import requests
from urllib.parse import urljoin
from config import config
from uploader.parsers.WaterDemand import Demand
from uploader.parsers.utils import WaterConnectionRequestEncoder, convert_json, underscore_to_camel, TIME_PERIOD_MAP
from common import to_json
from uploader.parsers.WaterDemand import Payer


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
    water: Optional[bool]
    sewerage: Optional[bool]
    property: Optional

    def __init__(self, tenant_id: None = None, property_id: None = None, documents: None = None,
                 plumber_info: None = None, road_type: None = None, road_cutting_area: None = None,
                 connection_category: None = None, connection_type: None = None,
                 additional_details: None = None, rain_water_harvesting: None = None,
                 water_source: None = None, meter_id: None = None, meter_installation_date: None = None,
                 proposed_pipe_size: None = None, actual_pipe_size: None = None,
                 proposed_taps: None = None, actual_taps: None = None, service: None = None, water: None = None, sewerage: None = None, property: None = None):
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
        self.water = water
        self.sewerage = sewerage

    def prepare_water_connection(self, json_data, tenant, property_id):
        print(json_data)
        self.tenantId = tenant
        self.propertyId = property_id
        # self.documents = json_data["documents"]
        # self.plumberInfo = json_data["plumber_info"]
        self.roadType = "BERMCUTTINGKATCHA"
        self.roadCuttingArea = 100
        # self.roadCuttingArea = json_data["roadCuttingArea"]
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
        self.water = True
        self.sewerage = False
        self.property = self.prepare_property()

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
            "WaterConnection":  to_json(self)

        }
        print("Water connection request payload", request_data)
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

    def get_road_type(road_type):
        switcher = {
            "Premix Carpet": "PREMIXCORPET",
            "BM and Premix Road": "BMPREMIXROAD",
            "Berm Cutting (Katcha)": "BERMCUTTINGKATCHA",
            "Brick Paving": "BRICKPAVING",
            "CC Road": "CCROAD",
            "Interlocking Paver Block": "INTERLOCKINGPAVERBLOCK",
            "Open Pipe": "OPENPIPE"
        }
        return switcher.get(road_type, "BERMCUTTINGKATCHA")

    def prepare_property(self):
        property_pb_qa =  {
              "id": "0af72ed5-0df3-41de-8749-7275fbfacfe2",
              "propertyId": "PT-107-997630",
              "surveyId": None,
              "linkedProperties": None,
              "tenantId": "pb.amritsar",
              "accountId": "0ca31b69-7ae7-4266-8e3a-f4724f75d270",
              "oldPropertyId": None,
              "status": "INWORKFLOW",
              "address": {
                "tenantId": "pb.amritsar",
                "doorNo": "231",
                "plotNo": None,
                "id": "85b7744d-0b62-4905-b5e7-6bba82f9409a",
                "landmark": None,
                "city": "amritsar",
                "district": None,
                "region": None,
                "state": None,
                "country": None,
                "pincode": None,
                "buildingName": "add",
                "street": None,
                "locality": {
                  "code": "ALOC1",
                  "name": "AASHIYANA ENCLAVE - NA1 - A3",
                  "label": "Locality",
                  "latitude": None,
                  "longitude": None,
                  "area": "AREA3",
                  "children": [],
                  "materializedPath": None
                },
                "geoLocation": {
                  "latitude": None,
                  "longitude": None
                },
                "additionalDetails": None
              },
              "acknowldgementNumber": "AC-2021-01-08-997641",
              "propertyType": "BUILTUP.SHAREDPROPERTY",
              "ownershipCategory": "INDIVIDUAL.SINGLEOWNER",
              "owners": [
                {
                  "id": 856017,
                  "uuid": "79e2fc6d-588d-4b08-b70b-3b97f9070cd8",
                  "userName": "6364021789",
                  "password": None,
                  "salutation": None,
                  "name": "Nizam",
                  "gender": "MALE",
                  "mobileNumber": "6364021789",
                  "emailId": None,
                  "altContactNumber": None,
                  "pan": None,
                  "aadhaarNumber": None,
                  "permanentAddress": None,
                  "permanentCity": None,
                  "permanentPinCode": None,
                  "correspondenceCity": None,
                  "correspondencePinCode": None,
                  "correspondenceAddress": "231, add, AASHIYANA ENCLAVE - B1 - A3, amritsar",
                  "active": True,
                  "dob": None,
                  "pwdExpiryDate": None,
                  "locale": None,
                  "type": "CITIZEN",
                  "signature": None,
                  "accountLocked": None,
                  "roles": [
                    {
                      "id": None,
                      "name": "Citizen",
                      "code": "CITIZEN",
                      "tenantId": None
                    }
                  ],
                  "fatherOrHusbandName": "ansasms",
                  "bloodGroup": None,
                  "identificationMark": None,
                  "photo": None,
                  "createdBy": "0ca31b69-7ae7-4266-8e3a-f4724f75d270",
                  "createdDate": 1610104120141,
                  "lastModifiedBy": "0ca31b69-7ae7-4266-8e3a-f4724f75d270",
                  "lastModifiedDate": 1610104120141,
                  "tenantId": "pb.amritsar",
                  "ownerInfoUuid": "fad2c52d-0f68-46e3-8d79-47674585cc27",
                  "isPrimaryOwner": None,
                  "ownerShipPercentage": None,
                  "ownerType": "NONE",
                  "institutionId": None,
                  "status": "ACTIVE",
                  "documents": None,
                  "relationship": "FATHER"
                }
              ],
              "institution": None,
              "creationReason": "CREATE",
              "usageCategory": "RESIDENTIAL",
              "noOfFloors": 1,
              "landArea": 23432,
              "superBuiltUpArea": None,
              "source": "MUNICIPAL_RECORDS",
              "channel": "SYSTEM",
              "documents": None,
              "units": [
                {
                  "id": "d782490d-390f-49c3-b16b-e952e2927c99",
                  "tenantId": None,
                  "floorNo": None,
                  "unitType": None,
                  "usageCategory": "RESIDENTIAL.COMMERCIAL.OTHERCOMMERCIALSUBMINOR.OTHERCOMMERCIAL",
                  "occupancyType": "SELFOCCUPIED",
                  "active": True,
                  "occupancyDate": None,
                  "constructionDetail": {
                    "carpetArea": None,
                    "builtUpArea": None,
                    "plinthArea": None,
                    "superBuiltUpArea": None,
                    "constructionType": None,
                    "constructionDate": None,
                    "dimensions": None
                  },
                  "additionalDetails": None,
                  "auditDetails": None,
                  "arv": None
                }
              ],
              "additionalDetails": {
                "isRainwaterHarvesting": False
              },
              "auditDetails": {
                "createdBy": "0ca31b69-7ae7-4266-8e3a-f4724f75d270",
                "lastModifiedBy": "0ca31b69-7ae7-4266-8e3a-f4724f75d270",
                "createdTime": 1610104119620,
                "lastModifiedTime": 1610104119620
              },
              "workflow": None
            }
        property_egov_qa = {
            "id": "601a9410-a873-4bc5-bd7c-37d3864f3beb",
            "propertyId": "PB-PT-2020-05-30-005172",
            "surveyId": None,
            "linkedProperties": None,
            "tenantId": "pb.amritsar",
            "accountId": "e2f7be4e-a412-4b80-89bc-5963eefe6417",
            "oldPropertyId": None,
            "status": "ACTIVE",
            "address": {
                "tenantId": "pb.amritsar",
                "doorNo": None,
                "plotNo": None,
                "id": "1b2caf8b-0bc2-4d88-b200-8935e73e7af8",
                "landmark": None,
                "city": "Amritsar",
                "district": None,
                "region": None,
                "state": None,
                "country": None,
                "pincode": None,
                "buildingName": None,
                "street": None,
                "locality": {
                    "code": "SUN55",
                    "name": "Deep Rice Mill Road - Area1",
                    "label": "Locality",
                    "latitude": None,
                    "longitude": None,
                    "area": "Area1",
                    "children": [],
                    "materializedPath": None
                },
                "geoLocation": {
                    "latitude": 0,
                    "longitude": 0
                },
                "additionalDetails": None
            },
            "acknowldgementNumber": "PB-MT-107-000465",
            "propertyType": "BUILTUP.SHAREDPROPERTY",
            "ownershipCategory": "INDIVIDUAL.SINGLEOWNER",
            "owners": [
                {
                    "id": None,
                    "uuid": "c28a4ee7-2531-4144-b83f-23d8c52b401b",
                    "userName": "ceddc253-5bcf-4a8d-bf7a-35585e597d5f",
                    "password": None,
                    "salutation": None,
                    "name": "srish",
                    "gender": "MALE",
                    "mobileNumber": "7050350829",
                    "emailId": None,
                    "altContactNumber": None,
                    "pan": None,
                    "aadhaarNumber": None,
                    "permanentAddress": None,
                    "permanentCity": None,
                    "permanentPinCode": None,
                    "correspondenceCity": None,
                    "correspondencePinCode": None,
                    "correspondenceAddress": "NA, NA, Deep Rice Mill Road - Area1, Amritsar",
                    "active": True,
                    "dob": None,
                    "pwdExpiryDate": 1598605666000,
                    "locale": None,
                    "type": "CITIZEN",
                    "signature": None,
                    "accountLocked": False,
                    "roles": [
                        {
                            "id": None,
                            "name": "Citizen",
                            "code": "CITIZEN",
                            "tenantId": "pb"
                        }
                    ],
                    "fatherOrHusbandName": "ssssss",
                    "bloodGroup": None,
                    "identificationMark": None,
                    "photo": None,
                    "createdBy": "2502",
                    "createdDate": 1590829666000,
                    "lastModifiedBy": "1",
                    "lastModifiedDate": 1601286876000,
                    "tenantId": "pb",
                    "ownerInfoUuid": "cf219937-2111-4716-be8c-196b321f04a4",
                    "isPrimaryOwner": None,
                    "ownerShipPercentage": None,
                    "ownerType": "NONE",
                    "institutionId": None,
                    "status": "INACTIVE",
                    "documents": None,
                    "relationship": "FATHER"
                },
                {
                    "id": None,
                    "uuid": "0bb68823-81a3-4495-8ead-6461ed26aea6",
                    "userName": "c3aaba50-df59-4898-8741-6362e20d6553",
                    "password": None,
                    "salutation": None,
                    "name": "srishti",
                    "gender": "FEMALE",
                    "mobileNumber": "7903553472",
                    "emailId": None,
                    "altContactNumber": None,
                    "pan": None,
                    "aadhaarNumber": None,
                    "permanentAddress": None,
                    "permanentCity": None,
                    "permanentPinCode": None,
                    "correspondenceCity": None,
                    "correspondencePinCode": None,
                    "correspondenceAddress": "NA, NA, Deep Rice Mill Road - Area1, Amritsar",
                    "active": True,
                    "dob": None,
                    "pwdExpiryDate": 1586897669000,
                    "locale": None,
                    "type": "CITIZEN",
                    "signature": None,
                    "accountLocked": False,
                    "roles": [
                        {
                            "id": None,
                            "name": "Citizen",
                            "code": "CITIZEN",
                            "tenantId": "pb"
                        }
                    ],
                    "fatherOrHusbandName": "sdf",
                    "bloodGroup": None,
                    "identificationMark": None,
                    "photo": None,
                    "createdBy": "1560",
                    "createdDate": 1579002870000,
                    "lastModifiedBy": "1",
                    "lastModifiedDate": 1601564003000,
                    "tenantId": "pb",
                    "ownerInfoUuid": "17640516-6f3e-42d8-a69e-10ca4eb833a2",
                    "isPrimaryOwner": None,
                    "ownerShipPercentage": None,
                    "ownerType": "NONE",
                    "institutionId": None,
                    "status": "ACTIVE",
                    "documents": None,
                    "relationship": "FATHER"
                }
            ],
            "institution": None,
            "creationReason": "MUTATION",
            "usageCategory": "RESIDENTIAL",
            "noOfFloors": 2,
            "landArea": None,
            "superBuiltUpArea": 2469.11,
            "source": "MUNICIPAL_RECORDS",
            "channel": "CFC_COUNTER",

            "units": [
                {
                    "id": "5a73af8c-dae3-41fe-a867-ae0fdd532bc9",
                    "tenantId": None,
                    "floorNo": 6,
                    "unitType": "False",
                    "usageCategory": "RESIDENTIAL",
                    "occupancyType": "RENTED",
                    "active": True,
                    "occupancyDate": 0,
                    "constructionDetail": {
                        "carpetArea": None,
                        "builtUpArea": 2469.11,
                        "plinthArea": None,
                        "superBuiltUpArea": None,
                        "constructionType": None,
                        "constructionDate": 0,
                        "dimensions": None
                    },
                    "additionalDetails": None,
                    "auditDetails": None,
                    "arv": 22222
                }
            ],
            "additionalDetails": {
                "caseDetails": "",
                "inflammable": False,
                "marketValue": "8878",
                "documentDate": 1601317799000,
                "documentValue": "8778",
                "documentNumber": "8989",
                "heightAbove36Feet": False,
                "isMutationInCourt": "NO",
                "reasonForTransfer": "SALEDEED",
                "previousPropertyUuid": "efed71cb-235e-46b4-b317-30fb7f5a4b5d",
                "govtAcquisitionDetails": "",
                "isPropertyUnderGovtPossession": "NO"
            },
            "auditDetails": {
                "createdBy": "e32344dd-ca37-4ea6-80f1-259f91bdfe8f",
                "lastModifiedBy": "e32344dd-ca37-4ea6-80f1-259f91bdfe8f",
                "createdTime": 1601286821408,
                "lastModifiedTime": 1601286876842
            },
            "workflow": None
        }

        return property_pb_qa