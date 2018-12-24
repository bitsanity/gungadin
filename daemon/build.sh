#!/bin/bash

commd=$1

jarname=gungadaemon.jar

source env.sh

#
# ./build.sh
#
if [ -z $commd ]
then
  echo compiling Java ...
  # Note: simplejson uses unchecked or unsafe operations
  javac -g -classpath $JARS:./java:. ./java/daemon/*.java

  echo making dist ...
  pushd ./java
  jar -cf ../dist/$jarname ./daemon/*.class
  popd
fi

#
# ./build.sh clean
#
if [ "$commd" = "clean" ]
then
  echo cleaning...
  rm ./java/daemon/*.class
  rm dist/$jarname
  rm -rf kgserver.log kgserver.properties kgserver.script kgserver.tmp
  rm -rf hwm.log hwm.properties hwm.script hwm.tmp
fi
