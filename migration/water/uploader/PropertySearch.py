import json
import os
from common import superuser_login
from config import *
import requests
tenant_id = os.getenv("TENANT_ID", "pg.citya")


def property_search(waterJsonString):
    try:
        water = json.loads(waterJsonString)
        mobilenumber = water[0]["mobilenumber"]
        if mobilenumber!=None :

             access_token = superuser_login()["access_token"]
             request_data = {
                "RequestInfo": {
                    "authToken": access_token
                }
                 }

             url = config.URL_PROPERTY_SEARCH + "?tenantId=" + tenant_id + "&mobileNumber=" + mobilenumber
             response = requests.post(url,
                                     json=request_data).json()

             data = response;
             if (len(data['Properties']) == 0):
                return config.NO_PROPERTY_MESSAGE
             else:
              for property in data['Properties']:
                if (property["owners"][0]["name"] == water[0]["applicantname"]) and (property["owners"][0]["fatherOrHusbandName"]== water[0]["guardianname"]):
                    return property['propertyId']

                    return config.NO_PROPERTY_MESSAGE
        else :
            return config.NO_MOBILE_NUMBER


    except Exception as ex:
        print("Exception occurred while searching the property", ex)
        return config.NO_PROPERTY_MESSAGE
