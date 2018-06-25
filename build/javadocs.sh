#!/bin/sh
cd `dirname $0`/..
echo "Running in `pwd`"
rm -rf docs/javadocs
javadoc -d docs/javadocs -sourcepath src edu.calpoly.spritely
cd docs
cp -r javadocs spritely-javadocs
mkdir -p ../out
rm -f ../out/spritely-javadocs.zip
zip -q -r ../out/spritely-javadocs.zip spritely-javadocs
rm -rf spritely-javadocs
echo "Created out/spritely-javadocs.zip"
