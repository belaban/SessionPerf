#
# Runs the client perf test. -h prints out command line options
#


CP=./classes

for i in lib/*.jar
do
    CP=$CP:./${i}
done

OPTS="-server -Xmx800M -Xss8K -XX:ThreadStackSize=8k -XX:CompileThreshold=10000 -XX:SurvivorRatio=8 -XX:TargetSurvivorRatio=90 -XX:MaxTenuringThreshold=31"

java -classpath $CP $OPTS perf.Test -host localhost:8000 $*
