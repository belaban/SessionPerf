#!/bin/sh


KILL_SERVERS=${KILL_SERVERS-"cluster10 cluster09 cluster08 cluster07 cluster06 cluster05 cluster04 cluster03 cluster02 cluster01"}


for server in $KILL_SERVERS
do
    echo "Killing @$server"

    ssh $server killall -9 java
done



