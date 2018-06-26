#!/bin/sh

if [ "$JAVA8_HOME" = "" ] ; then
    # Insist on JDK 1.8, so that the release that gets published
    # works for 1.8 and above.
    echo "Please set the JAVA8_HOME environment variable."
    exit 1
fi
cd `dirname $0`/..
rm -rf out/classes
mkdir -p out/classes
$JAVA8_HOME/bin/javac -Xlint:unchecked -Xlint:deprecation \
        -sourcepath src -d out/classes \
	-target 8 -source 8 -bootclasspath $JAVA8_HOME/jre/lib/rt.jar \
	src/edu/calpoly/spritely/*.java
if [ $? != 0 ] ; then
    exit 1
fi
cd out/classes
jar cf ../spritely.jar *
echo "Created out/spritely.jar"
