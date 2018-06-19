#!/bin/sh
cd `dirname $0`/..
echo "Running in `pwd`"
rm -rf docs/javadocs
javadoc -d docs/javadocs -sourcepath src edu.calpoly.spritely
cd docs/javadocs
zip -r ../../out/spritely-javadocs.zip *
