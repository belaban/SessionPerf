#!/bin/sh


KILL_SERVERS=${KILL_SERVERS-"cluster10 cluster09 cluster08 cluster07 cluster06 cluster05 cluster04 cluster03 cluster02 cluster01"}


for server in $KILL_SERVERS
do
    echo "Truncating logs on  @$server"

    ssh $server truncate -c -s 0 /tmp/servers/one/log/server.log
    ssh $server truncate -c -s 0 /tmp/servers/two/log/server.log
done

    echo "Truncating logs on  cluster01"
ssh cluster01 truncate -c -s 0 /tmp/master/one/log/server.log
ssh cluster01 truncate -c -s 0 /tmp/master/two/log/server.log





