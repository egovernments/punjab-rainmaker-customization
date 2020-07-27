#!/bin/bash

export DB_NAME=legacy_data
export DB_USER=legacy
export DB_PASSWORD="<update password here>"
export TENANT="<update tenantid here>"
export DB_HOST=localhost
export BATCH_NAME=1
export TABLE_NAME="<Update tenant>_pt_legacy_data"
export BATCH_SIZE=100

export BATCH_PARALLEL=1
# If True this will only create one record and exist
# This should be used when testing the whole setup
export DRY_RUN=True

export CITY="<Update City Name>"

export PYTHONPATH=$PYTHONPATH:.
rm -rf *.log
for BATCH in {1..$BATCH_PARALLEL}
do
    export BATCH_NAME=$BATCH
    echo "Launching Batch $BATCH_NAME"
    python3 uploader/PropertyTaxDBProcess.py > $BATCH_NAME.log &
done
sleep 10
less +F *.log