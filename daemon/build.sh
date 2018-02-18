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
  javac -g -classpath $JARS:./java:. ./java/gungadaemon/*.java

  echo making dist ...
  pushd ./java
  jar -cf ../dist/$jarname ./gungadaemon/*.class
  popd
  pushd ../ethereum/bindings
  jar uf ../../daemon/dist/$jarname ./gungadin/membership/*.class
  jar uf ../../daemon/dist/$jarname ./gungadin/publisher/*.class
  popd
fi

#
# ./build.sh clean
#
if [ "$commd" = "clean" ]
then
  echo cleaning...
  rm ./java/gungadaemon/*.class
  rm dist/$jarname
  rm -rf kgserver.log kgserver.properties kgserver.script kgserver.tmp
  rm -rf hwm.log hwm.properties hwm.script hwm.tmp
fi
