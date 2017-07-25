#!/bin/bash

# Keep the environment settings from setup.sh script
export MYDIR=`dirname $0`
. ./$MYDIR/setup.sh

# Start the inference-dev
$CFI/scripts/inference-dev --checker GUTI.GUTIChecker --solver GUTI.GUTIConstraintSolver --solverArgs=useGraph=false --hacks=true --afuOutputDir=./annotated -m ROUNDTRIP $*
