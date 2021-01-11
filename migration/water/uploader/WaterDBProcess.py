import psycopg2
import json
import os
import time
from common import superuser_login, ws_user_login
from water_models.water import *
from uploader.parsers.WaterDemand import WaterDemand

batch_size = os.getenv("BATCH_SIZE", "100")
dbname = os.getenv("DB_NAME", "sunam")
dbuser = os.getenv("DB_USER", "postgres")
dbpassword = os.getenv("DB_PASSWORD", "postgres")
host = os.getenv("DB_HOST", "localhost")
schema_name = os.getenv("schema_name", "generic")
batch = os.getenv("BATCH_NAME", "1")
default_phone = os.getenv("DEFAULT_PHONE", "6364021789")
batch_size = os.getenv("BATCH_SIZE", "100")
table_name = os.getenv("TABLE_NAME", "public.egpt_stg_property")
# tenant_id = os.getenv("TENANT_ID", "pg.citya")
tenant_id = os.getenv("TENANT_ID", "pb.amritsar")

connection = None
cursor = None
try:
    connection = psycopg2.connect(
        "dbname={} user={} password={} host={}".format(dbname, dbuser, dbpassword, host))
    cursor = connection.cursor()
    print("Successfully connected to the database ")

except Exception as exception:
    print("Exception occurred while connecting to the database")
    print(exception)


def update_db_record(uuid, **kwargs):
    columns = []
    for key in kwargs.keys():
        columns.append(key + "=%s")

    query = """UPDATE {} SET {} where uuid = %s""".format(table_name, ",".join(columns))
    print(query)
    cursor.execute(query, list(kwargs.values()) + [uuid])
    connection.commit()
    pass


def main():
    try:
        if connection is not None and cursor is not None:
            set_path = """set search_path to {}""".format(schema_name)
            cursor.execute(set_path)
            my_path = os.path.abspath(os.path.dirname(__file__))
            # print(my_path)
            path = os.path.join(my_path, "../sql/water_search.sql")
            print(path)
            sql = open(path, mode='r', encoding='utf-8-sig').read()
            # print(sql)

            ws_access_token = ws_user_login()["access_token"]
            print("access token:", ws_access_token)

            # property_id = "65af3a8d-ab1b-4cb8-9a81-ac56583abff6"
            # property_id = "c1578dac-7e8f-45a4-ab17-1d57a55f6a40"
            # egov micro qa property id
            # property_id = "601a9410-a873-4bc5-bd7c-37d3864f3beb"
            # punjab qa property id
            property_id = "0af72ed5-0df3-41de-8749-7275fbfacfe2"

            continue_processing = True
            while continue_processing:
                cursor.execute(sql)
                data = cursor.fetchmany(int(batch_size))
                if not data:
                    print("No more data to process. Script exiting")
                    continue_processing = False
                    cursor.close()
                    connection.close()

                for row in data:
                    # print(rows)
                    json_data = row[0]
                    consumer_code = json_data["consumercode"]
                    start = time.time()
                    water = WaterConnectionRequest()
                    print("ERP Water data", json.dumps(json_data))
                    water.prepare_water_connection(json_data, tenant_id, property_id)
                    request, response = water.upload_water_connection(ws_access_token)
                    print("Water connection created", json.dumps(response))
                    # time_taken = time.time() - start

                    if "WaterConnection" in response:
                        super_access_token = superuser_login()["access_token"]
                        print("super_access_token: ", super_access_token)
                        application_number = response["WaterConnection"][0]["applicationNo"]
                        ack_no = water.property.get("acknowldgementNumber")
                        print("ack_no: ", ack_no)
                        owner_uuid = water.property.get("owners")[0]["uuid"]
                        print("UUID: ", owner_uuid)
                        wd = WaterDemand()
                        demands = wd.prepare_demand(json_data, tenant_id, application_number, owner_uuid)
                        wd_request, wd_response = wd.create_demand(super_access_token, demands)
                        print(wd_request, wd_response)
                        print("Demand request", json.dumps(wd_request))
                        print("Demand created", json.dumps(wd_response))
                        # penalty_request, penalty_response = wd.generate_penalty(super_access_token, tenant_id, applicationNo)
                        # print("Penalty Generated", json.dumps(penalty_response))
                        bill_request, bill_response = wd.generate_bill(super_access_token, tenant_id, application_number)
                        print("Bill Generated", json.dumps(bill_response))
                        print("A Water with unique identifier:", consumer_code,
                              "is migrated successfully with applicationNo:", application_number)

    except Exception as ex:
        print("Exception occurred while pushing data to API.+ ")
        print(ex)
        # update_db_record(uuid, migrationstatus="EXCEPTION", responsejson=str(ex))


if __name__ == "__main__":
    main()