#!/bin/sh


SERVERS=${DEPLOY_SERVERS-"cluster01 cluster02 cluster03 cluster04 cluster05 cluster06 cluster07 cluster08"}


for server in $SERVERS
do
    echo "Removing all webapps from @$server:/tmp/standalone/XX/deployments"
    ssh $server "find /tmp/standalone -name web.war -exec rm -f {} \;"

done






