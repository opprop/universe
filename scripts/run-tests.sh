#!/bin/bash

export MYDIR=`dirname $0`
. $MYDIR/setup.sh

java -cp $CLASSPATH org.junit.runner.JUnitCore GUT.GUTTestsLostNo
java -cp $CLASSPATH org.junit.runner.JUnitCore GUT.GUTTestsLostYes
java -cp $CLASSPATH org.junit.runner.JUnitCore GUT.GUTTestsStrictPurity
java -cp $CLASSPATH org.junit.runner.JUnitCore GUT.GUTTestsTopology
