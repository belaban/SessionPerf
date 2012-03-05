
CP=./target/classes

for i in lib/*.jar
do
    CP=$CP:./${i}
done

mkdir -p target/classes

javac -classpath $CP -d target/classes src/perf/*.java