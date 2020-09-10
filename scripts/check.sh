#!/bin/bash

# Keep the environment settings from setup.sh script
export MYDIR=`dirname $0`
. ./$MYDIR/setup.sh

javac -processor universe.UniverseChecker $*
