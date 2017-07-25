#!/bin/bash

# Keep the environment settings from setup.sh script
export MYDIR=`dirname $0`
. ./$MYDIR/setup.sh

# dist directory of CheckerFramework Inference
distDir=$CFI/dist

# Start the inference: jar files are used when making inference
java -cp "$distDir"/checker.jar:"$distDir"/plume.jar:"$distDir"/checker-framework-inference.jar:$CLASSPATH checkers.inference.InferenceLauncher --checker GUTI.GUTIChecker --solver checkers.inference.solver.DebugSolver --solverArgs=useGraph=false --hacks=true --afuOutputDir=./annotated -m ROUNDTRIP $*
