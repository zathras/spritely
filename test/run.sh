#!/bin/sh

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


# To test headless, put $H in java line, and put stdin in single
# character mode:
#stty -echo cbreak; HEADLESS=-Djava.awt.headless=true

java $HEADLESS -cp out -ea Main
reset_tty
