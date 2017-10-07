#!/bin/bash

commd=$1

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
  jar -cf ../dist/gungadaemon.jar ./gungadaemon/*.class
  popd
fi

#
# ./build.sh clean
#
if [ "$commd" = "clean" ]
then
  echo cleaning...
  rm ./java/gungadaemon/*.class
  rm dist/gungadaemon.jar
  rm -rf kgserver.log kgserver.properties kgserver.script kgserver.tmp
fi
