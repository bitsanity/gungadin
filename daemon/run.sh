#!/bin/bash

commd=$1

# Publisher on rinkeby network
SCA="0x55A3A250fd420C5da0fa3343621216167A6D62aD"

source env.sh

if [ -z $commd ]
then
  echo running daemon ...
  java $JLIB -cp $JARS:./java:. gungadaemon.Daemon ethwalletpath=$HOME/.gungadin/UTC--2016-10-23T18-50-15.853386528Z--8e9342eb769c4039aaf33da739fb2fc8af9afdc1
fi

if [ "$commd" = "reactor" ]
then
  echo running reactor ...
  java $JLIB -cp $JARS:./java:. gungadaemon.Reactor $SCA
fi

if [ "$commd" = "test" ]
then
  echo running tests ...
fi
