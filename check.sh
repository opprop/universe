#!/bin/bash
set -e
# Keep the environment settings from setup.sh script
export MYDIR=`dirname $0`
. ./$MYDIR/setup.sh

export CLASSPATH=./bin/src:"$CFI"/dist/checker-framework-inference.jar:$CLASSPATH

DEBUG=""
CHECKER="universe.GUTChecker"

declare -a ARGS
for i in "$@" ; do
    if [[ $i == "-d" ]] ; then
        echo "Typecheck using debug mode. Listening at port 5050. Waiting for connection...."
        DEBUG="-J-Xdebug -J-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5050"
        continue
    fi

    ARGS[${#ARGS[@]}]="$i"
done

cmd=""

if [ "$DEBUG" == "" ]; then
	cmd="javac -cp "${CLASSPATH}" -processor "${CHECKER}" "${ARGS[@]}""
else
	cmd="javac "$DEBUG" -cp "${CLASSPATH}" -processor "${CHECKER}" -AatfDoNotCache "${ARGS[@]}""
fi

eval "$cmd"
