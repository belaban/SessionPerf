#!/bin/sh


source ./config.sh


for server in $SERVERS
do
    echo "Deploying $1 into ${PREFIX}$server:/tmp/standalone/deployments"

    scp $1 ${PREFIX}$server:/tmp/standalone/
    ssh ${PREFIX}$server "find /tmp/standalone -name deployments -exec cp /tmp/standalone/`basename $1` {}/ \;"
    ssh ${PREFIX}$server "rm -f /tmp/standalone/`basename $1`"
done






