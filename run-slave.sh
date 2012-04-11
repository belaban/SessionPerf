
# Runs a JBoss slave host controller
./domain.sh --host-config=host-slave.xml \
            -Djboss.domain.servers.dir=/tmp/servers \
            -Djboss.host.name=$1