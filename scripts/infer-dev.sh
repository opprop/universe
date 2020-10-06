#!/bin/bash

# Keep the environment settings from setup.sh script
export MYDIR=`dirname $0`
. ./$MYDIR/setup.sh

# dist directory of CheckerFramework Inference
distDir=$CFI/dist

# Start the inference: jar files are used when making inference
$CFI/scripts/inference-dev --checker universe.UniverseInferenceChecker --solver universe.UniverseInferenceConstraintSolver $*
