#!/bin/bash
set -e

export MYDIR=`dirname $0`
. ./$MYDIR/setup.sh

CHECKER=universe.UniverseInferenceChecker

SOLVER=universe.solver.UniverseGASolverEngine
IS_HACK=true

$CFI/scripts/inference-dev --checker "$CHECKER" --solver "$SOLVER" --solverArgs="collectStatistics=true,outputCNF=true" --hacks="$IS_HACK" -m ROUNDTRIP -afud ./annotated "$@"
