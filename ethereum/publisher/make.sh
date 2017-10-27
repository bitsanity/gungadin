#!/bin/bash

export commd=$1

#
# ./make.sh
#
if [ -z $commd ]
then
  echo building...
  solcjs --bin --abi --optimize -o ./build Publisher.sol
  exit
fi

#
# ./make.sh clean
#
if [ commd=clean ]
then
  echo removing all bin and abi files...
  rm *.abi *.bin
fi
