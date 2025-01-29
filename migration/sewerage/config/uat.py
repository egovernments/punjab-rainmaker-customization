from pathlib import Path

from .global_config import config
from attrdict import AttrDict



SUPERUSER = AttrDict()

# config.HOST = "https://egov-micro-uat.egovernments.org"
# SUPERUSER.username = "CounterEmployee"
# SUPERUSER.password = "eGov@123"
# SUPERUSER.username = "WS-CEMP"
# SUPERUSER.password = "12345678"
# SUPERUSER.tenant_id = "pg.citya"
config.HOST = "https://egov-micro-qa.egovernments.org"
SUPERUSER.username = "EMP-107-000177"
SUPERUSER.password = "79m7be9"
SUPERUSER.tenant_id = "pb.amritsar"

#TO get the active connection search using mobile number 7050350829
# Property Information Property ID: PB-PT-2020-05-30-005172

config.SUPERUSER = SUPERUSER
