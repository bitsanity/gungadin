#!/bin/bash

# ----------------------------------------------------------------------------
# NOTE:
#
# Requires web3j-2.3.1 installed under $HOME
#
# ----------------------------------------------------------------------------

commd=$1
ethdir=`pwd`
pubbase=$ethdir/publisher/build/Publisher_sol_Publisher
mbrbase=$ethdir/membership/build/Membership_sol_Membership
web3j=$HOME/web3j
w3jlib=$web3j/lib

jlibs=\
$w3jlib/core-2.3.1.jar:\
$w3jlib/rxjava-1.2.4.jar:\
$w3jlib/jackson-annotations-2.8.0.jar

#
# ./build.sh
#
if [ -z $commd ]
then
  echo make new bindings ...
  pushd $HOME/web3j/bin
  ./web3j solidity generate $pubbase.bin $pubbase.abi\
    -o $ethdir/bindings -p gungadin.publisher
  ./web3j solidity generate $mbrbase.bin $mbrbase.abi\
    -o $ethdir/bindings -p gungadin.membership
  popd
  javac -g -classpath "$jlibs:./bindings:."\
    ./bindings/gungadin/publisher/*.java
  javac -g -classpath "$jlibs:./bindings:."\
    ./bindings/gungadin/membership/*.java

  exit
fi

#
# ./build.sh clean
#
if [ "$commd" = "clean" ]
then
  echo cleaning...
  rm -rf ./bindings
  rm publisher/*.abi publisher/*.bin
  rm membership/*.abi membership/*.bin
fi
