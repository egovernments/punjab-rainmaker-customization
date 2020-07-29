from pathlib import Path
import os
from .global_config import config

config.TENANT = "pb"
config.CITY_NAME = os.getenv("CITY", None) or "Testing"

config.CONFIG_ENV = "uat"

import importlib

importlib.import_module("." + config.CONFIG_ENV, "config")
