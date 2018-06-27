#!/bin/sh
if [ "$JAVA8_HOME" = "" ] ; then
    echo "Please set the JAVA8_HOME environment variable."
    exit 1
fi
cd `dirname $0`/..
echo "Running in `pwd`"
rm -rf docs/javadocs
$JAVA8_HOME/bin/javadoc -d docs/javadocs -sourcepath src edu.calpoly.spritely
cd docs
cp -r javadocs spritely-javadocs
mkdir -p ../out
rm -f ../out/spritely-javadocs.zip
zip -q -r ../out/spritely-javadocs.zip spritely-javadocs
rm -rf spritely-javadocs
echo "Created out/spritely-javadocs.zip"
