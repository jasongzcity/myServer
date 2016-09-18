#!/bin/sh

# shared script for startup & shutdown
# author lwz

#locate JAVA_HOME and decide OPTS & ARGUMENTS
if [ ! -x ./setClasspath.sh ]; then
  echo "must have setClasspath.sh in ${SERVER_HOME}/bin"
  exit 1
fi

. ./setClasspath.sh

CLASSPATH=$CLASSPATH${SERVER_HOME}/classes:${SERVER_HOME}/lib/el-api.jar:${SERVER_HOME}/lib/log4j-api.jar:${SERVER_HOME}/lib/log4j-core.jar:${SERVER_HOME}/lib/servlet-api.jar:

#we got JAVA_HOME & CMD LINE ARGS now, execute
JAVA_OPTS="$SERVER_OPTS $USR_OPTS"
CMD_ARGS="$@ $SERVER_ARGS $USR_ARGS"
CLASSPATH="$CLASSPATH$USR_CLASSPATH"

#========JAVA EXECUTION========
echo
echo "Using JAVA_OPTS=$JAVA_OPTS"
echo "Using CMD_ARGS=$CMD_ARGS"
echo "Using CLASSPATH=$CLASSPATH"
echo

MAINCLASS=com.jason.server.connector.Bootstrap

exec $_RUNJAVA $JAVA_OPTS -classpath $CLASSPATH -Dserver.base=${SERVER_HOME} $MAINCLASS $CMD_ARGS
exit 0
