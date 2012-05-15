#!/bin/sh

source config.sh

for server in $SERVERS
do
    echo "Executing $1 on ${PREFIX}$server:"

    ssh ${PREFIX}$server "$1"

done






