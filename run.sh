#
# Runs the client perf test. -h prints out command line options
#


CP=./target/classes

for i in lib/*.jar
do
    CP=$CP:./${i}
done

## These props govern HttpURLConnection (used by perf.Test), see http://docs.oracle.com/javase/7/docs/api/ for details
HTTP_PROPS="-Dhttp.keepalive=true -Dhttp.maxConnections=410"


OPTS="-server -Xmx800M -Xss8K -XX:ThreadStackSize=8k -XX:CompileThreshold=10000 -XX:SurvivorRatio=8 -XX:TargetSurvivorRatio=90 -XX:MaxTenuringThreshold=31"

java -classpath $CP $HTTP_PROPS $OPTS perf.Test -host localhost:8000 $*
