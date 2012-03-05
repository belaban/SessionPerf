#!/bin/sh

COMMAND=${1-unknown}
NUM_SERVERS=${2}
JBOSS_CONFIG=${3-base}
BIND_ADDRESS=${4-\$MYTESTIP_1}
JGROUPS_BIND_ADDR=${5-$BIND_ADDRESS}
MCAST_ADDR=${6-232.9.8.7}

if [ "x${JBOSS_TMP}" == "x" ]
then
   JBOSS_TMP=/tmp/SessionStress
fi
JBOSS_HOME=${JBOSS_TMP}/ActiveJBoss

AP_SERVERS=( cluster01 cluster02 cluster03 cluster04 cluster05 cluster06 cluster07 cluster08 cluster09 cluster10 )

JAVA_OPTS=${JAVA_OPTS-"-server -Xms756m -Xmx756m -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000 -Djgroups.udp.ip_ttl=1 -Djboss.jvmRoute=\$HOSTNAME"}

STARTUP_COMMAND="nohup $JBOSS_HOME/bin/run.sh -b $BIND_ADDRESS -Djgroups.bind_addr=$JGROUPS_BIND_ADDR -c $JBOSS_CONFIG -u $MCAST_ADDR -g StressPartition -m 23456 -Djboss.server.log.threshold=INFO >/dev/null 2>&1 &"
STARTUP_WAIT_COMMAND="while ! cat $JBOSS_HOME/server/$JBOSS_CONFIG/log/server.log 2>&1 | grep 'JBoss (Microcontainer)' > /dev/null; do sleep 1; done"
SHUTDOWN_COMMAND="nohup $JBOSS_HOME/bin/shutdown.sh -s $BIND_ADDRESS:1099 -S >/dev/null 2>&1 &"
SHUTDOWN_WAIT_COMMAND="while ps -efl | grep -v grep | grep java | grep org.jboss.Main > /dev/null; do sleep 1; done"

server=unknown

case $COMMAND in
    start)        
         for ((idx=0; idx < NUM_SERVERS ; idx++))
         do
            server="${AP_SERVERS[idx]}"
            echo "jboss: Startup JBoss@$server"
            ssh $server.qa.atl.jboss.com JAVA_OPTS=\"$JAVA_OPTS\" $STARTUP_COMMAND &
            sleep 10 
         done
         for ((idx=0; idx < NUM_SERVERS ; idx++))
         do
            server="${AP_SERVERS[idx]}"
            echo "jboss: Waiting for JBoss@$server"
            ssh $server.qa.atl.jboss.com "$STARTUP_WAIT_COMMAND"
         done
        ;;
    stop)        
         for ((idx=0; idx < NUM_SERVERS ; idx++))
         do
            server="${AP_SERVERS[idx]}"
            echo "jboss: Shutdown JBoss@$server"
            ssh $server.qa.atl.jboss.com "$SHUTDOWN_COMMAND"
         done        
         for ((idx=0; idx < NUM_SERVERS ; idx++))
         do
            server="${AP_SERVERS[idx]}"
            echo "jboss: Waiting for JBoss@$server"
            ssh $server.qa.atl.jboss.com "$SHUTDOWN_WAIT_COMMAND"
         done
        ;;
    *)
        echo "Usage: $0 [start|stop] NUM_SERVERS CONFIG BIND_VAR JGROUPS_BIND_VAR MCAST_ADDR"
        ;;
esac
