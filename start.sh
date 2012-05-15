#!/bin/sh



source ./config.sh

if [ $# -ne 1 ]
then
  echo "Usage: `basename $0` <number of servers to start>"
  exit 1
fi

num_servers=$1
echo Starting $num_servers servers
echo ""

cnt=1


for server in $SERVERS
do
    echo "Starting server $cnt on ${PREFIX}$server"

    ssh  ${PREFIX}$server "cd /home/bela/jboss/bin ; nohup ./run-standalone.sh $cnt > /dev/null 2>&1  &"

    if [ $cnt -eq $num_servers ]; then
        exit 1
    fi

    if [ $server -lt 2 ]; then
         sleep 3
    else
         sleep 1
    fi

    let cnt=cnt+1


done


for server in $SECOND_SERVERS
do
    echo "Starting server $cnt on ${PREFIX}$server"

    ssh  ${PREFIX}$server "cd /home/bela/jboss/bin ; nohup ./run-standalone2.sh $cnt > /dev/null 2>&1  &"

    if [ $cnt -eq $num_servers ]; then
        exit 1
    fi

    sleep 1

    let cnt=cnt+1

done






