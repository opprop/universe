#!/bin/bash

WORKING_DIR=$(pwd)

if [ ! -z ${JSR308+x} ] ; then
    JSR308=$(cd $(dirname "$0")/../.. && pwd)
fi

DLJC="$JSR308"/do-like-javac
export AFU="$JSR308"/annotation-tools/annotation-file-utilities
export PATH="$PATH":"$AFU"/scripts

export CLASSPATH="$JSR308"/universe/bin/src:"$JSR308"/generic-type-inference-solver/bin


#parsing build command of the target program
build_cmd="$1"
shift
while [ "$#" -gt 0 ]
do
    build_cmd="$build_cmd $1"
    shift
done

cd "$WORKING_DIR"
running_cmd="python $DLJC/dljc -t inference --checker GUTI.GUTIChecker --solver GUTI.GUTIConstraintSolver --solverArgs=\"collectStatistic=true\" -o logs -m ROUNDTRIP -afud $WORKING_DIR/annotated -- $build_cmd "
echo "============ Important variables ============="
echo "JSR308: $JSR308"
echo "CLASSPATH: $CLASSPATH"
echo "build cmd: $build_cmd"
echo "running cmd: $running_cmd"
echo "============================================="

eval "$running_cmd"
