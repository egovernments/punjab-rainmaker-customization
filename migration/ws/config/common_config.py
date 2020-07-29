from .local import *


def load_config():
    config.URL_LOGIN = config.HOST + "/user/oauth/token"

    config.URL_BILLING_SLAB_CREATE = config.HOST + "/pt-calculator-v2/billingslab/_create"

    config.TENANT_ID = config.TENANT + "." + config.CITY_NAME.lower()


load_config()
