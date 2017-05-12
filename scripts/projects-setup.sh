#!/bin/bash

# Fail the whole script if any command fails
set -e

WORKING_DIR=$(cd $(dirname "$0") && pwd)
. $WORKING_DIR/env-setup.sh

# export SHELLOPTS

#default value is pascaliUWat. REPO_SITE may be set to other value for travis test purpose.
export REPO_SITE="${REPO_SITE:-opprop}"

echo "------ Downloading everthing from REPO_SITE: $REPO_SITE ------"

##### Clone checker-framework
if [ -d $JSR308/checker-framework ] ; then
    (cd $JSR308/checker-framework && git pull)
else
    (cd $JSR308 && git clone -b hackathon --depth 1 https://github.com/"$REPO_SITE"/checker-framework.git)
fi

## Clone annotation-tools (Annotation File Utilities)
if [ -d $JSR308/annotation-tools ] ; then
    # Older versions of git don't support the -C command-line option
    (cd $JSR308/annotation-tools && git pull)
else
    (cd $JSR308 && git clone --depth 1 https://github.com/"$REPO_SITE"/annotation-tools.git)
fi

##### Clone checker-framework-inference
if [ -d $JSR308/checker-framework-inference ] ; then
    (cd $JSR308/checker-framework-inference && git pull)
else
    (cd $JSR308 && git clone -b hackathon --depth 1 https://github.com/"$REPO_SITE"/checker-framework-inference.git)
fi

##### Clone generic-type-inference-solver
if [ -d $JSR308/generic-type-inference-solver ] ; then
    (cd $JSR308/generic-type-inference-solver && git pull)
else
    (cd $JSR308 && git clone --depth 1 https://github.com/"$REPO_SITE"/generic-type-inference-solver.git)
fi

# Build jsr308-langtools
(cd $JSR308/annotation-tools/ && ./.travis-build-without-test.sh)

## Build checker-framework, with jdk
ant -f $JSR308/checker-framework/checker/build.xml jar

### Build checker-framework-inference
(cd $JSR308/checker-framework-inference && gradle dist)

##### Build generic-type-inference-solver
(cd $JSR308/generic-type-inference-solver/ && gradle build)

###### Build GUT
(cd $JSR308/universe && gradle build)
