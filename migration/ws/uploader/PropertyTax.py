import json
from typing import Optional, List
from urllib.parse import urljoin
from uuid import UUID

import requests

from config import config
from uploader.parsers.utils import PropertyEncoder, convert_json, underscore_to_camel


class PropertyAdditionalDetails:
    pass

    def __init__(self, ) -> None:
        pass


class Locality:
    code: Optional[str]
    area: Optional[str]

    def __init__(self, code: Optional[str] = None, area: Optional[str] = None) -> None:
        self.code = code
        self.area = area


class Address:
    city: Optional[str]
    door_no: Optional[str]
    building_name: Optional[str]
    street: Optional[str]
    locality: Optional[Locality]
    pincode: Optional[int]

    def __init__(self, city: Optional[str] = None, door_no: Optional[str] = None, building_name: Optional[str] = None,
                 street: Optional[str] = None, locality: Optional[Locality] = None,
                 pincode: Optional[int] = None, address_line1: Optional[int] = None) -> None:
        self.city = city
        self.door_no = door_no
        self.building_name = building_name
        self.street = street
        self.locality = locality
        self.address_line1 = address_line1
        self.pincode = pincode


class PropertyDetailAdditionalDetails:
    inflammable: Optional[bool]
    height_above36_feet: Optional[bool]

    def __init__(self, inflammable: Optional[bool] = False, height_above36_feet: Optional[bool] = False) -> None:
        self.inflammable = inflammable
        self.height_above36_feet = height_above36_feet


class CitizenInfo:
    mobile_number: Optional[str]
    name: Optional[str]

    def __init__(self, mobile_number: Optional[str] = None, name: Optional[str] = None) -> None:
        self.mobile_number = mobile_number
        self.name = name


class Document:
    document_uid: None
    document_type: None

    def __init__(self, document_uid: None = None, document_type: None = None) -> None:
        self.document_uid = document_uid
        self.document_type = document_type


class Owner:
    documents: Optional[List[Document]]
    name: Optional[str]
    mobile_number: Optional[str]
    father_or_husband_name: Optional[str]
    email_id: Optional[str]
    permanent_address: Optional[str]
    relationship: Optional[str]
    owner_type: Optional[str]
    gender: Optional[str]
    designation: Optional[str]
    alt_contact_number: Optional[str]

    def __init__(self, documents: Optional[List[Document]] = None, name: Optional[str] = None,
                 mobile_number: Optional[str] = None, father_or_husband_name: Optional[str] = None,
                 email_id: Optional[str] = None, permanent_address: Optional[str] = None,
                 relationship: Optional[str] = None, owner_type: Optional[str] = None,
                 gender: Optional[str] = None,
                 designation: Optional[str] = None,
                 alt_contact_number: Optional[str] = None
                 ) -> None:
        self.documents = documents
        self.name = name
        self.mobile_number = mobile_number
        self.father_or_husband_name = father_or_husband_name
        self.email_id = email_id
        self.permanent_address = permanent_address
        self.relationship = relationship
        self.owner_type = owner_type
        self.gender = gender
        self.designation = designation
        self.alt_contact_number = alt_contact_number


class UnitAdditionalDetails:
    inner_dimensions_known: Optional[bool]
    rooms_area: Optional[int]
    common_area: Optional[int]
    garage_area: Optional[int]
    bathroom_area: Optional[int]

    def __init__(self, inner_dimensions_known: Optional[bool] = False, rooms_area: Optional[int] = False,
                 common_area: Optional[int] = False, garage_area: Optional[int] = False,
                 bathroom_area: Optional[int] = False) -> None:
        self.inner_dimensions_known = inner_dimensions_known
        self.rooms_area = rooms_area
        self.common_area = common_area
        self.garage_area = garage_area
        self.bathroom_area = bathroom_area


class Unit:
    usage_category_major: Optional[str]
    usage_category_minor: Optional[str]
    usage_category_detail: Optional[str]
    usage_category_sub_minor: Optional[str]
    occupancy_type: Optional[str]
    unit_area: Optional[int]
    floor_no: Optional[int]
    arv: Optional[float]
    construction_type: Optional[str]
    construction_type: Optional[UnitAdditionalDetails]

    def __init__(self, usage_category_major: Optional[str] = None,
                 usage_category_minor: Optional[str] = None,
                 usage_category_detail: Optional[str] = None,
                 usage_category_sub_minor: Optional[str] = None,
                 occupancy_type: Optional[str] = None,
                 unit_area: Optional[int] = None,
                 floor_no: Optional[str] = None,
                 arv: Optional[float] = None,
                 construction_type: Optional[str] = None,
                 additional_details: Optional[UnitAdditionalDetails] = None) -> None:
        self.usage_category_major = usage_category_major
        self.occupancy_type = occupancy_type
        self.unit_area = unit_area
        self.floor_no = floor_no
        self.arv = arv
        self.usage_category_minor = usage_category_minor
        self.usage_category_detail = usage_category_detail
        self.usage_category_sub_minor = usage_category_sub_minor
        self.construction_type = construction_type
        self.additional_details = additional_details


class Institution:
    name: Optional[str]
    type: Optional[str]
    designation: Optional[str]

    def __init__(self, name: Optional[str] = None, type: Optional[str] = None,
                 designation: Optional[str] = None) -> None:
        self.name = name
        self.type = type
        self.designation = designation


class PropertyDetail:
    usage_category_minor: None
    units: Optional[List[Unit]]
    usage_category_major: Optional[str]
    property_sub_type: Optional[str]
    land_area: None
    build_up_area: Optional[int]
    property_type: Optional[str]
    no_of_floors: Optional[int]
    sub_ownership_category: Optional[str]
    ownership_category: Optional[str]
    owners: Optional[List[Owner]]
    financial_year: Optional[str]
    citizen_info: Optional[CitizenInfo]
    institution: Optional[Institution]
    additional_details: Optional[PropertyDetailAdditionalDetails]
    assessment_date: Optional[int]
    source: Optional[str]

    def __init__(self, usage_category_minor: Optional[str] = None, units: Optional[List[Unit]] = None,
                 usage_category_major: Optional[str] = None, property_sub_type: Optional[str] = None,
                 land_area: Optional[float] = None, build_up_area: Optional[float] = None,
                 property_type: Optional[str] = None, no_of_floors: Optional[int] = None,
                 sub_ownership_category: Optional[str] = None, ownership_category: Optional[str] = None,
                 owners: Optional[List[Owner]] = None, financial_year: Optional[str] = None,
                 citizen_info: Optional[CitizenInfo] = None,
                 institution: Optional[Institution] = None,
                 source: Optional[str] = None, assessment_date: Optional[int] = None,
                 additional_details: Optional[PropertyDetailAdditionalDetails] = None) -> None:
        self.usage_category_minor = usage_category_minor
        self.units = units
        self.usage_category_major = usage_category_major
        self.property_sub_type = property_sub_type
        self.land_area = land_area
        self.build_up_area = build_up_area
        self.property_type = property_type
        self.no_of_floors = no_of_floors
        self.sub_ownership_category = sub_ownership_category
        self.ownership_category = ownership_category
        self.owners = owners
        self.financial_year = financial_year
        self.citizen_info = citizen_info
        self.additional_details = additional_details
        self.institution = institution
        self.source = source
        self.assessment_date = assessment_date


class Property:
    tenant_id: Optional[str]
    address: Optional[Address]
    old_property_id: Optional[str]
    property_details: Optional[List[PropertyDetail]]
    additional_details: Optional[PropertyAdditionalDetails]

    def __init__(self, tenant_id: Optional[str] = None, address: Optional[Address] = None,
                 old_property_id: Optional[str] = None, property_details: Optional[List[PropertyDetail]] = None,
                 additional_details: Optional[PropertyAdditionalDetails] = None) -> None:
        self.tenant_id = tenant_id
        self.address = address
        self.old_property_id = old_property_id
        self.property_details = property_details
        self.additional_details = additional_details

    def get_property_json(self):
        property_encoder = PropertyEncoder().encode(self)
        return convert_json(json.loads(property_encoder), underscore_to_camel)

    def upload_property(self, access_token):
        #print("Property Json : ", self.get_property_json())
        request_data = {
            "RequestInfo": {
                "authToken": access_token
            },
            "Properties": [
                self.get_property_json()
            ]
        }
        #print(request_data)
        response = requests.post(urljoin(config.HOST, "/pt-services-v2/property/_create?tenantId="), json=request_data)
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


class PropertyCreateRequest:
    request_info: Optional[RequestInfo]
    properties: Optional[List[Property]]

    def __init__(self, request_info: Optional[RequestInfo] = None, properties: Optional[List[Property]] = None) -> None:
        self.request_info = request_info
        self.properties = properties
