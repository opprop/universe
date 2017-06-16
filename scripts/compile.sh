#!/bin/bash

# Keep the environment settings from setup.sh script
export MYDIR=`dirname $0`
. ./$MYDIR/setup.sh

SRC_OUTPUT_DIR=$UNIVERSE/bin/src
TST_OUTPUT_DIR=$UNIVERSE/bin/tst

if [ ! -d "$SRC_OUTPUT_DIR" ]
then
mkdir -p "$SRC_OUTPUT_DIR"
fi

if [ ! -d "$TST_OUTPUT_DIR" ]
then
mkdir -p "$TST_OUTPUT_DIR"
fi

javac -cp $CLASSPATH -d "$SRC_OUTPUT_DIR" `find $UNIVERSE/src -name "*.java"` $*
javac -cp $CLASSPATH -d "$TST_OUTPUT_DIR" `find $UNIVERSE/tst -name "*.java"` $*
