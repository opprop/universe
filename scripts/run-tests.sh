#!/bin/bash
set -e

export MYDIR=`dirname $0`
. $MYDIR/setup.sh

java -cp $CLASSPATH universe.UniverseTests
