import psycopg2
import json
import os
import time
from common import superuser_login
from water_models.water import *

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
tenant_id = os.getenv("TENANT_ID", "pg.citya")

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

            access_token = superuser_login()["access_token"]
            property_id = "65af3a8d-ab1b-4cb8-9a81-ac56583abff6"
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
                    water.prepare_water_connection(json_data, tenant_id, property_id)
                    request, response = water.upload_water_connection(access_token)
                    print("Water connection created", json.dumps(response))
                    time_taken = time.time() - start

    except Exception as ex:
        print("Exception occurred while pushing data to API.+ ")
        print(ex)
        # update_db_record(uuid, migrationstatus="EXCEPTION", responsejson=str(ex))


if __name__ == "__main__":
    main()