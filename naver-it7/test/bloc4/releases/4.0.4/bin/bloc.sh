#!/bin/sh
# -----------------------------------------------------------------------------
# Start/Stop Script for the BLOC Server
# -----------------------------------------------------------------------------

BLOC_HOME="$( cd "$( dirname "$0" )" && cd .. && pwd )"

if [ -z "$BLOC_BASE" ] ; then
  BLOC_BASE="$BLOC_HOME"
fi

"$JAVA_HOME/bin/java" \
	-Dbloc.home="$BLOC_HOME" -Dbloc.base="$BLOC_BASE" -Djava.io.tmpdir="$BLOC_BASE"/temp -jar "$BLOC_HOME"/libs/bloc-server-4.0.4.jar \
	"$BLOC_BASE"/conf/bloc.ini "$@"