from pathlib import Path

from .global_config import config
from attrdict import AttrDict

config.HOST = "https://egov-micro-uat.egovernments.org"

SUPERUSER = AttrDict()

# SUPERUSER.username = "CounterEmployee"
# SUPERUSER.password = "eGov@123"
SUPERUSER.username = "WS-CEMP"
SUPERUSER.password = "12345678"
SUPERUSER.tenant_id = "pg.citya"

config.SUPERUSER = SUPERUSER
