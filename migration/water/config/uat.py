from pathlib import Path

from .global_config import config
from attrdict import AttrDict

SUPERUSER = AttrDict()
# WSUSER = AttrDict()


config.HOST = "https://mseva-qa.lgpunjab.gov.in"
SUPERUSER.ws_username = "EMP-0-000121"
SUPERUSER.ws_password = "eGov@123"
SUPERUSER.ws_tenant_id = "pb.amritsar"

SUPERUSER.username = "EMP-0-000121"
SUPERUSER.password = "eGov@123"
SUPERUSER.tenant_id = "pb.amritsar"

# config.HOST = "https://egov-micro-uat.egovernments.org"
# SUPERUSER.username = "WS-CEMP"
# SUPERUSER.password = "12345678"
# SUPERUSER.tenant_id = "pg.citya"

# config.HOST = "https://egov-micro-qa.egovernments.org"
# SUPERUSER.ws_username = "EMP-107-000177"
# SUPERUSER.ws_password = "79m7be9"
# SUPERUSER.ws_tenant_id = "pb.amritsar"

# SUPERUSER.username = "EMP1"
# SUPERUSER.password = "eGov@123"
# SUPERUSER.tenant_id = "pb.amritsar"


# TO get the active connection search using mobile number 7050350829
# Property Information Property ID: PB-PT-2020-05-30-005172

# SUPERUSER.username = "CounterEmployee"
# SUPERUSER.password = "eGov@123"




config.SUPERUSER = SUPERUSER


# Designation # User Name # Password # ULB
#
# Counter Employee # EMP-107-000177 # 79m7be9 # Amritsar
#
# Doc Verifier # EMP-107-000178 # x8bv7av1 # Amritsar
#
# Field Inspector # EMP-107-000179 # x0mn1mc7 # Amritsar
#
# Approver # EMP-107-000180 # 01xA7aA9 # Amritsar