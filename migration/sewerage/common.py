import json
import uuid
from urllib.parse import urljoin

import numpy
import re
import requests

from config import config


def superuser_login():
    return login_egov(config.SUPERUSER.username, config.SUPERUSER.password, config.SUPERUSER.tenant_id, "EMPLOYEE")


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