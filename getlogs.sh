#!/bin/sh


SERVERS=${SERVERS-"cluster08 cluster07 cluster06 cluster05 cluster04 cluster03 cluster02"}

mkdir logs
for server in $SERVERS
do
    echo "getting log from @$server"
    scp $SERVER.mw.lab.eng.bos.redhat.com:/tmp/servers/one/log/server.log logs/$SERVER.log

done
scp cluster01.mw.lab.eng.bos.redhat.com:/tmp/master/one/log/server.log logs/cluster01.log




