#!/bin/bash

commd=$1

source env.sh

#
# ./build.sh
#
if [ -z $commd ]
then
  echo compiling Java ...
  javac -g -classpath $JARS:./java:. ./java/pubui/*.java

  echo making dist ...
  pushd ./java
  jar -cf ../dist/pubui.jar ./pubui/*.class
  popd
fi

#
# ./build.sh clean
#
if [ "$commd" = "clean" ]
then
  echo cleaning...
  rm ./java/pubui/*.class
fi
