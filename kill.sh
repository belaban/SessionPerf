#!/bin/sh


KILL_SERVERS=${KILL_SERVERS-"cluster01 cluster02 cluster03 cluster04 cluster05 cluster06 cluster07 cluster08"}


for server in $KILL_SERVERS
do
    echo "Killing @$server"

    ssh $server killall -9 java
    ssh $server "find /tmp/standalone -name log -exec rm -fr {} \;"
done






