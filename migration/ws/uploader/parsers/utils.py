import json
import re

from json import JSONEncoder


class WaterConnectionRequestEncoder(JSONEncoder):
    def default(self, o):
        return o.__dict__


def convert_json(d, convert):
    new_d = {}
    for k, v in d.items():
        if isinstance(v, list):
            new_d[convert(k)] = []
            for i, vv in enumerate(v):
                new_d[convert(k)].append(convert_json(v[i], convert))
        else:
            new_d[convert(k)] = convert_json(v, convert) if isinstance(v, dict) else v
    return new_d


def convert_jsons(d, convert):
    new_d = []
    for item in d:
        new_d.append(convert_json(item, convert))

    return new_d


camel_pat = re.compile(r'([A-Z])')
under_pat = re.compile(r'_([a-z])')


def camel_to_underscore(name):
    return camel_pat.sub(lambda x: '_' + x.group(1).lower(), name)


def underscore_to_camel(name):
    return under_pat.sub(lambda x: x.group(1).upper(), name)


def convert_load(*args, **kwargs):
    json_obj = json.load(*args, **kwargs)
    return convert_json(json_obj, camel_to_underscore)


def convert_dump(*args, **kwargs):
    args = (convert_json(args[0], underscore_to_camel),) + args[1:]
    json.dump(*args, **kwargs)


def get_floor_number(floor: str):
    if floor in FLOOR_MAP:
        return FLOOR_MAP[floor]

    if "GROUND" in floor.upper():
        return "0"
    elif " 1ST" in floor:
        return "1"
    elif " 2ND" in floor:
        return "2"


TIME_PERIOD_MAP = {
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