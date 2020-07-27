# import uuid
# from pathlib import Path
#
# from common import *
# from config import config, load_revenue_boundary_config
#
# load_revenue_boundary_config()
#
# dfs = open_excel_file(config.SHEET)
#
# wards = get_sheet(dfs, config.SHEET_WARDS)
# zones = get_sheet(dfs, config.SHEET_ZONES)
# locality = get_sheet(dfs, config.SHEET_LOCALITY)
#
# offset = 1
#
# index_code = get_column_index(wards, config.COLUMN_WARD_CODE)
# index_name = get_column_index(wards, config.COLUMN_WARD_NAME)
# index_zone_name = get_column_index(wards, config.COLUMN_WARD_ADMIN_ZONE_NAME)
#
# ward_to_code_map = {}
# for _, row in wards.iterrows():
#     ward_to_code_map[row[index_code].strip()] = row[index_name].strip()
#     ward_to_code_map[row[index_name].strip()] = row[index_code].strip()
#
# wards_data = wards.apply(lambda row: {"id": str(uuid.uuid4()),
#                                       "boundaryNum": 1,
#                                       "name": row[index_name].strip(),
#                                       "localname": row[index_name].strip(),
#                                       "longitude": None,
#                                       "latitude": None,
#                                       "label": "Block",
#                                       "code": row[index_code].strip(),
#                                       "zone": row[index_zone_name].strip(),
#                                       "children": []}
#                          , axis=1)
#
# index_code = get_column_index(zones, config.COLUMN_ZONE_CODE)
# index_name = get_column_index(zones, config.COLUMN_ZONE_NAME)
#
# zone_to_code_map = {}
# for _, row in zones.iterrows():
#     zone_to_code_map[row[index_code].strip()] = row[index_name].strip()
#     zone_to_code_map[row[index_name].strip()] = row[index_code].strip()
#
# zones_data = zones.apply(lambda row: {"id": str(uuid.uuid4()),
#                                       "boundaryNum": 1,
#                                       "name": row[index_name].strip(),
#                                       "localname": row[index_name].strip(),
#                                       "longitude": None,
#                                       "latitude": None,
#                                       "label": "Zone",
#                                       "code": row[index_code].strip(),
#                                       "children": []}
#                          , axis=1)
#
# index_code = get_column_index(locality, config.COLUMN_LOCALITY_CODE)
# index_name = get_column_index(locality, config.COLUMN_LOCALITY_NAME)
# index_admin_block = get_column_index(locality, config.COLUMN_LOCALITY_ADMIN_BLOCK)
# index_area = get_column_index(locality, config.COLUMN_LOCALITY_AREA)
#
# locality_data = locality.apply(lambda row: {
#     "id": str(uuid.uuid4()),
#     "boundaryNum": 1,
#     "name": row[index_name].strip() + " - " + ward_to_code_map[row[index_admin_block].strip()],
#     "localname": row[index_name].strip() + " - " + ward_to_code_map[row[index_admin_block].strip()],
#     "longitude": None,
#     "latitude": None,
#     "label": "Locality",
#     "code": row[index_code].strip(),
#     "ward": row[index_admin_block].strip(),
#     "area": row[index_area].strip().upper().replace(" ", ""),
#     "children": []
# }, axis=1)
#
# wards_list = wards_data.tolist()
# locality_list = locality_data.tolist()
# zones_list = zones_data.tolist()
#
# wards_map = {}
#
# for ward in wards_list:
#     zone = ward.pop('zone')
#
#     wards_map[zone] = wards_map.get(zone, [])
#     wards_map[zone].append(ward)
#
#     wards_map[zone_to_code_map[zone]] = wards_map[zone]
#
# locality_map = {}
#
# for loca in locality_list:
#     ward = loca.pop('ward')
#     locality_map[ward] = locality_map.get(ward, [])
#     locality_map[ward].append(loca)
#
#     locality_map[ward_to_code_map[ward]] = locality_map[ward]
#
# for ward in wards_list:
#     code = ward['code']
#     ward['children'] = locality_map.get(code, [])
#
# for zone in zones_list:
#     name = zone['name']
#     zone['children'] = wards_map[name]
#
# import json
#
# current_boundary_type = 'REVENUE'
#
# new_boundary_data = {
#     "hierarchyType": {
#         "code": current_boundary_type,
#         "name": current_boundary_type
#     },
#     "boundary": {
#         "id": 1,
#         "boundaryNum": 1,
#         "name": config.CITY_NAME,
#         "localname": config.CITY_NAME,
#         "longitude": None,
#         "latitude": None,
#         "label": "City",
#         "code": config.TENANT_ID,
#         "children": zones_list
#     }
# }
#
# final_data = {
#     "tenantId": config.TENANT_ID,
#     "moduleName": "egov-location",
#     "TenantBoundary": [
#         new_boundary_data
#     ]
# }
#
# data = json.dumps(final_data, indent=2)
#
# print(data)
# import os
#
# response = input("Do you want to append the data in repo (y/[n])?")
#
# if response.lower() == "y":
#
#     boundary_path = config.MDMS_LOCATION / config.CITY_NAME.lower() / "egov-location"
#     os.makedirs(boundary_path, exist_ok=True)
#
#     if os.path.isfile(boundary_path / "boundary-data.json"):
#         with open(boundary_path / "boundary-data.json") as f:
#             existing_boundary_data = json.load(f)
#
#         if len(existing_boundary_data["TenantBoundary"]) == 0:
#             # should never happen but just in case
#             existing_boundary_data["TenantBoundary"].append(new_boundary_data)
#             print("Boundary aydded")
#         elif len(existing_boundary_data["TenantBoundary"]) == 1:
#             if existing_boundary_data["TenantBoundary"][0]["hierarchyType"]["code"] == current_boundary_type:
#                 existing_boundary_data["TenantBoundary"][0] = new_boundary_data
#                 print("Boundary already exists. Overwriting")
#             else:
#                 existing_boundary_data["TenantBoundary"].append(new_boundary_data)
#                 print("Boundary file exists. Adding new data")
#         elif len(existing_boundary_data["TenantBoundary"]) == 2:
#             if existing_boundary_data["TenantBoundary"][0]["hierarchyType"]["code"] == current_boundary_type:
#                 existing_boundary_data["TenantBoundary"][0] = new_boundary_data
#             else:
#                 existing_boundary_data["TenantBoundary"][1] = new_boundary_data
#             print("Boundary already exists. Overwriting")
#     else:
#         # the file doesn't exists already, so we can safely generate current boundary
#         print("Boundary didn't exist. Creating one")
#
#     with open(boundary_path / "boundary-data.json", "w") as f:
#         f.write(data)


from common import create_boundary
from config import load_revenue_boundary_config

def main():
    create_boundary(load_revenue_boundary_config, "REVENUE")


if __name__ == "__main__":
    main()
