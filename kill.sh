#!/bin/sh

source ./config.sh


for server in $SERVERS
do
    echo "Killing ${PREFIX}$server"

    ssh ${PREFIX}$server killall -9 java
    ssh ${PREFIX}$server "find /tmp/standalone -name log -exec rm -fr {} \;"
done






