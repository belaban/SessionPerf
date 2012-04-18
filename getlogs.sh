#!/bin/sh


SERVERS=${SERVERS-"cluster08 cluster07 cluster06 cluster05 cluster04 cluster03 cluster02"}

mkdir logs
for server in $SERVERS
do
    echo "getting log from @$server"
    scp $server.mw.lab.eng.bos.redhat.com:/tmp/servers/one/log/server.log logs/$server-one.log
    scp $server.mw.lab.eng.bos.redhat.com:/tmp/servers/two/log/server.log logs/$server-two.log

done
scp cluster01.mw.lab.eng.bos.redhat.com:/tmp/master/one/log/server.log logs/cluster01-one.log
scp cluster01.mw.lab.eng.bos.redhat.com:/tmp/master/two/log/server.log logs/cluster01-two.log




