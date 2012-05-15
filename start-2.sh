#!/bin/sh


SERVERS=${SERVERS-"1 2"}


for server in $SERVERS
do
    echo "Starting AS on cluster0$server"

    ssh  cluster0$server "cd /home/bela/jboss/bin ; nohup ./run-standalone.sh $server > /dev/null 2>&1  &"


    if [ $server -lt 2 ]; then
         sleep 3
    else
         sleep 1
    fi



done






