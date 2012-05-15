#!/bin/sh


SERVERS=${SERVERS-"cluster01 cluster02 cluster03 cluster04 cluster05 cluster06 cluster07 cluster08"}


for server in $SERVERS
do
    echo "Executing $1 on @$server:"

    ssh $server "$1"

done






