#!/bin/sh


SERVERS=${SERVERS-"1 2 3 4 5 6 7 8"}

SECOND_SERVERS=${SECOND_SERVERS-"1 2 3 4"}


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


for server in $SECOND_SERVERS
do
    num=$(($server+8))
    echo "Starting AS $num on cluster0$server"
    
    ssh  cluster0$server "cd /home/bela/jboss/bin ; nohup ./run-standalone2.sh $num > /dev/null 2>&1  &"

    sleep 1

done






