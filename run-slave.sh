
# Runs a JBoss slave host controller
./domain.sh --host-config=host-slave.xml \
            -Djboss.node.name=$1 \
            -Dinstance-id=$1 \
            -Djboss.server.data.dir=/tmp/$1 \
            -Djboss.server.temp.dir=/tmp/$1 \
            -Djboss.host.name=$1