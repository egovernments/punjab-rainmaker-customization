import json
import uuid
from urllib.parse import urljoin

import numpy
import pandas as pd
import re
import requests
import jsonpickle

from config import config


def to_json(self):
    json_obj_encode = jsonpickle.encode(self, unpicklable=False)
    json_data = json.dumps(json_obj_encode, indent=4)
    json_obj_decode = json.loads(jsonpickle.decode(json_data))
    # print(json_obj_decode)
    return json_obj_decode


def superuser_login():
    return login_egov(config.SUPERUSER.username, config.SUPERUSER.password, config.SUPERUSER.tenant_id, "EMPLOYEE")


def ws_user_login():
    return login_egov(config.SUPERUSER.ws_username, config.SUPERUSER.ws_password, config.SUPERUSER.ws_tenant_id, "EMPLOYEE")


def login_egov(username, password, tenant_id, user_type="EMPLOYEE"):
    resp = requests.post(config.URL_LOGIN, data={
        "username": username,
        "password": password,
        "grant_type": "password",
        "scope": "read",
        "tenantId": tenant_id,
        "userType": user_type
    }, headers={
        "Authorization": "Basic ZWdvdi11c2VyLWNsaWVudDplZ292LXVzZXItc2VjcmV0"
    })

    assert resp.status_code == 200, "Login should respond with 200: " + json.dumps(resp.json(), indent=2)
    return resp.json()


TIME_PERIOD_MAP = {
    '2021-03-31': '1617175799000',
    '2019-04-01': '1554076800000',
    '2020-03-31': '1585699199000',
    '2018-04-01': '1522540800000',
    '2019-03-31': '1554076799000',
    '2017-04-01': '1491004800000',
    '2018-03-31': '1522540798000',
    '2016-04-01': '1459468800000',
    '2017-03-31': '1491004799000',
    '2015-04-01': '1427846400000',
    '2016-03-31': '1459468799000',
    '2014-04-01': '1396310400000',
    '2015-03-31': '1427846399000'
}

