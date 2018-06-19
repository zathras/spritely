#!/bin/sh
cd `dirname $0`/..
rm -rf out/classes
mkdir -p out/classes
javac -Xlint:unchecked -Xlint:deprecation -sourcepath src -d out/classes \
	src/edu/calpoly/spritely/*.java
if [ $? != 0 ] ; then
    exit 1
fi
cd out/classes
jar cf ../spritely.jar *
echo "Created out/spritely.jar"
