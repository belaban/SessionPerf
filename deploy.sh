#!/bin/sh


SERVERS=${DEPLOY_SERVERS-"cluster01 cluster02 cluster03 cluster04 cluster05 cluster06 cluster07 cluster08"}


for server in $SERVERS
do
    echo "Deploying $1 into @$server:/tmp/standalone/deployments"

    scp $1 $server:/tmp/standalone/
    ssh $server "find /tmp/standalone -name deployments -exec cp /tmp/standalone/`basename $1` {}/ \;"
    ssh $server "rm -f /tmp/standalone/`basename $1`"
done






