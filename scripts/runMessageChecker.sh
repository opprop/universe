#!/bin/bash

# Keep the environment settings from setup.sh script
export MYDIR=`dirname $0`
. ./$MYDIR/setup.sh

javac -processor org.checkerframework.checker.compilermsgs.CompilerMessagesChecker -Apropfiles=$MYDIR/../src/universe/messages.properties $MYDIR/../src/universe/*.java
