#!/bin/sh

# the startup shell script for MyServer
# author lwz

#notice: Didn't check platform here. Default Linux
#Default no softlink, the startup.sh is in the same dir with startInternal.sh

CD=`pwd`
if [ ! -x ./startInternal.sh  ]; then
  echo "Can't find startInternal.sh in the same directory with $0"
  echo "Please build up server as BUILDING_GUIDE.TXT specified"
  exit 1
fi

#get $SERVER_HOME & call startInternal.sh
export SERVER_HOME=`dirname $CD`
exec $CD/startInternal.sh start
