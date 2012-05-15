#!/bin/sh

source ./config.sh

for server in $SERVERS
do
    echo "Removing all webapps from ${PREFIX}$server:/tmp/standalone/XX/deployments"
    ssh ${PREFIX}$server "find /tmp/standalone -name web.war -exec rm -f {} \;"

done






