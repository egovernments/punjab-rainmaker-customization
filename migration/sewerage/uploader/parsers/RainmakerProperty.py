import re
import json
from datetime import datetime
from uploader.PropertyTax import *
from uploader.parsers.utils import *


class RainmakerProperty(Property):

    def __init__(self, *args, **kwargs):
        super(RainmakerProperty, self).__init__()

    def prepare_property(self, context, tenant_id, city):
        property_details = []
        self.old_property_id = context['uniqueidentifier']
        self.property_type = context['propertytype']
        self.additional_details = context['additonalinfo']
        self.address = self.prepare_address(context, city)
        self.tenant_id = tenant_id

        for props in context['dcb']:
            property_details.append(self.prepare_property_detail(context, props['fromdate']))

        self.property_details = property_details

        pass

    def prepare_address(self, context, city):
        locality = Locality(code=context["boundary2"])

        address = Address(city=city, door_no=context["doorno"], locality=locality, address_line1=context["address"])

        return address

    def prepare_owners(self, context=None):
        owners = []
        for owner in context['owners']:
            owner = Owner(name=owner['name'], father_or_husband_name=owner['guardianname'],
                          mobile_number=owner['mobilenumber'], owner_type='NONE')
            owners.append(owner)

        return owners

    def prepare_floors(self, context, construction_date):
        units = []
        for floor in context['floors']:
            unit = Unit(floor_no=floor['floornumber'], occupancy_type=floor['occupancytype'],
                        unit_area=floor['builtuparea'], arv=floor['arv'],
                        usage_category_major=floor['usagetype'], construction_type=floor['constructiontype'], usage_category_minor=floor['usagetype'])
            if floor['usagetype'] == 'NONRESIDENTIAL':
                unit.usage_category_sub_minor = 'SUBMNR25'
            #if nr - usage_category_minor - CATDET25
            # if resd - usage_category_minor - blank
            '''unit.additional_details = {"innerDimensionsKnown": "true",
                                       "roomsArea": 100,
                                       "commonArea": 100,
                                       "garageArea": 100,
                                       "bathroomArea": 100};'''
            units.append(unit)

        return units

    def prepare_property_detail(self, context, assessment_date):
        property_detail = PropertyDetail(land_area=context["landarea"], source='LEGACY_RECORD')
        property_detail.assessment_date = assessment_date
        property_detail.usage_category_major = context['usagetype']
        property_detail.usage_category_minor = context['usagetype']
        property_detail.property_type = context['propertytype']
        property_detail.ownership_category = context["ownercategory"]
        property_detail.sub_ownership_category = context["ownersubcategory"]
        property_detail.financial_year = assessment_date[0:4] + '-' + str(int(assessment_date[2:4]) + 1)
        property_detail.assessment_date = TIME_PERIOD_MAP[assessment_date]
        property_detail.additional_details = {
            "thana": "",
            "roadType": context['roadtype'],
            "constructionYear": context['constructiondate']
          }
        if context['propertytype'] == 'BUILTUP':
            property_detail.build_up_area = context["builtuparea"]
            property_detail.property_sub_type = 'SHAREDPROPERTY'
            # Add units
            units = self.prepare_floors(context, assessment_date)
            property_detail.units = units
            property_detail.no_of_floors = len(units)
        if context['propertytype'] == 'VACANT':
            property_detail.units = []
            property_detail.no_of_floors = 1

        # Add Owners
        owners = self.prepare_owners(context)
        property_detail.owners = owners
        property_detail.citizen_info = CitizenInfo(name=owners[0].name, mobile_number=owners[0].mobile_number)

        return property_detail
