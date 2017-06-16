#!/bin/bash
export MYDIR=`dirname $0`

# Universe top level directory
export UNIVERSE=$MYDIR/..

# CheckerFramework Inference
export CFI="$JSR308"/checker-framework-inference

# Generic Type Inference Solver
export SOLVER="$JSR308"/generic-type-inference-solver

# Dependencies
export CLASSPATH=$UNIVERSE/bin/src:$UNIVERSE/bin/tst:$CHECKERFRAMEWORK/dataflow/build:$CHECKERFRAMEWORK/javacutil/build:$CHECKERFRAMEWORK/stubparser/build:$CHECKERFRAMEWORK/framework/build:$CHECKERFRAMEWORK/checker/build:$SOLVER/bin:$CHECKERFRAMEWORK/framework/tests/junit-4.12.jar:$CHECKERFRAMEWORK/framework/tests/hamcrest-core-1.3.jar:$CFI/bin:$CFI/dist/org.ow2.sat4j.core-2.3.4.jar:$JSR308/jsr308-langtools/build/classes:$CLASSPATH
