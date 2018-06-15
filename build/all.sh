#!/bin/sh
cd `dirname $0`/..
build/javadocs.sh
if [ $? != 0 ] ; then
    exit 1
fi
build/libs.sh
