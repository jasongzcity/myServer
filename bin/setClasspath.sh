#!/bin/sh

#find jvm & let user set their own arguments
#author lwz

#check if JAVA_HOME set
if [ -z "$JAVA_HOME" ]; then
  echo 'please set $JAVA_HOME'
  exit 1
fi

if [ ! -x "$JAVA_HOME"/bin/java ]; then
  echo "please ensure you have executable java in your JAVA_HOME"
  exit 1
fi

_RUNJAVA=$JAVA_HOME/bin/java

#user should set their own java options here
USR_OPTS=

#user should set their own auguments here
USR_ARGS=

#user should set their own classpath here,please remember to add':'at the end
USR_CLASSPATH=
