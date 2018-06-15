#!/bin/sh
echo "This should be run from the project base directory."
javadoc -d docs/javadocs -sourcepath src edu.calpoly.spritely
