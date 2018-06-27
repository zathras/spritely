#!/bin/bash

#
# Compile the program:
#
rm -rf out
mkdir out
javac -Xlint:unchecked -sourcepath src:../src -d out src/Main.java
if [ $? != 0 ] ; then
    exit 1
fi

#
# If ^C is entered, make sure we get out of "stty cbreak" mode,
# as set below:
#
function reset_tty {
    stty sane
}
trap reset_tty INT
HEADLESS=""


# To test headless, set the java system property, and
# put the terminal in character mode:
stty -echo cbreak; HEADLESS=-Djava.awt.headless=true

java $HEADLESS -cp out -ea Main
reset_tty
