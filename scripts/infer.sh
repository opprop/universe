#!/bin/bash

# Keep the environment settings from setup.sh script
export MYDIR=`dirname $0`
. ./$MYDIR/setup.sh

# dist directory of CheckerFramework Inference
distDir=$CFI/dist

# Start the inference: jar files are used when making inference
java -cp "$distDir"/checker.jar:"$distDir"/plume.jar:"$distDir"/checker-framework-inference.jar:$CLASSPATH checkers.inference.InferenceLauncher --checker universe.UniverseInferenceChecker --solver universe.UniverseInferenceConstraintSolver $*

# Below is wrong because we can't set the classpath using command line arguments because they are passed as arguments to InferenceLauncher.
#$CFI/scripts/inference --checker GUTI.GUTIChecker --solver GUTI.GUTIConstraintSolver $
