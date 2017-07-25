#!/bin/bash

export MYDIR=`dirname $0`
. $MYDIR/setup.sh

#java -cp $CLASSPATH org.junit.runner.JUnitCore GUT.GUTTestsLostNo
#java -cp $CLASSPATH org.junit.runner.JUnitCore GUT.GUTTestsLostYes
#java -cp $CLASSPATH org.junit.runner.JUnitCore GUT.GUTTestsStrictPurity
#java -cp $CLASSPATH org.junit.runner.JUnitCore GUT.GUTTestsTopology
java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5050 -cp $CLASSPATH org.junit.runner.JUnitCore GUTI.GUTITest
