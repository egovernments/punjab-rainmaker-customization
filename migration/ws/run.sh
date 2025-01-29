#!/bin/bash

export PYTHONPATH=.:$PYTHONPATH
export DB_NAME=sunam
export DB_USER=postgres
export DB_PASSWORD=postgres
export TENANT=pb.sunam
export DB_HOST=localhost
export TABLE_NAME=public.jalandhar_pt_legacy_data
export BATCH_SIZE=100
export DRY_RUN=False

for BATCH in {1..1}
do
    export BATCH_NAME=$BATCH
    echo "Launching Batch $BATCH_NAME"
    python3 uploader/WaterDBProcess.py > $BATCH_NAME.log &
done
tail -f *.log