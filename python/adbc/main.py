import adbc_driver_flightsql.dbapi
from adbc_driver_flightsql import DatabaseOptions

with adbc_driver_flightsql.dbapi.connect(
    "grpc://localhost:10000",
    db_kwargs={
        DatabaseOptions.AUTHORIZATION_HEADER.value: "Anonymous",
    },
) as conn:
    with conn.cursor() as cursor:
        cursor.execute("SELECT 42 as Foo")
        pa_table = cursor.fetch_arrow_table()
        print(pa_table)
