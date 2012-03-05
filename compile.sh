
CP=./classes

for i in lib/*.jar
do
    CP=$CP:./${i}
done

mkdir classes

javac -classpath $CP -d classes src/perf/*.java