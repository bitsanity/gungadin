#!/bin/bash

# Functions ------------------------------------------------------------------
runminer() {

  echo running ipfs ...
  echo ipfs daemon --enable-pubsub-experiment &

  echo running ethgateway ...
  pushd js/ethgateway
  echo "node ethgw.js $egwport $daemonegwinport $daemonpubkey $publishersca $votessca" &
  popd

  echo running clientservices ...
  pushd js/clientservices
  echo node clisvcs.js $publishersca &
  popd

  echo running daemon ...
  echo java $JLIB -cp $JARS:./java:. daemon.Daemon \
      intpassphrase=$intkeypassphrase \
      uiport=$daemonuiport \
      egwinport=$daemonegwinport \
      egwoutport=$egwport \
      egwpeerpubkey=$ethgwpubkey \
      extkeyfilepath=$extkeyfilepath \
      intkeyfilepath=$intkeyfilepath \
      pubsdbfilepath=$pubsdbfilepath \
      hwmdbfilepath=$hwmdbfilepath \
      aclfilepath=$aclfilepath \
      ipfscachedir=$ipfscachedir

  echo running lottery process ...
  pushd ../ethereum/lottery
  echo node lotto.js "$votessca"
  popd
}

# main -----------------------------------------------------------------------
source env.sh

commd=$1

daemonuiport=8804
daemonegwinport=8805
egwport=8806
ethgwpubkey=04blahblahblah

extkeyfilepath=$HOME/.ethereum/keystore/UTC--2016-10-23T18-50-15.853386528Z--8e9342eb769c4039aaf33da739fb2fc8af9afdc1
intkeypassphrase="change-on-install"
intkeyfilepath=nodeid.blk
pubsdbfilepath=publications.db
aclfilepath=acl.db
hwmdbfilepath=hwm.db

ipfscachedir=$HOME/temp

daemonpubkey=`java $JLIB -cp $JARS:./java:. daemon.NodeIdentity $intkeypassphrase $intkeyfilepath`

echo daemon internal pubkey: $daemonpubkey

if [ -z $commd ]
then
  publishersca=
  votesca=

  echo running geth ...
  #geth --syncmode light --cache 4096 --ws --wsorigins "*" > geth.out 2>&1 &

  runminer
fi

if [ "$commd" = "key" ]
then
  # <dbfile> add <pubkey>
  java $JLIB -cp $JARS:./java:. daemon.ACL $2 add $3
fi

if [ "$commd" = "test" ]
then
  account0="0x8c34f41f1cf2dfe2c28b1ce7808031c40ce26d38"
  account1="0x147b61187f3f16583ac77060cbc4f711ae6c9349"
  ethgwpubkey="somepubkey"
  treasurysca="0xF68580C3263FB98C6EAeE7164afD45Ecf6189EbB"
  membershipsca="0x4Ebf4321A360533AC2D48A713B8f18D341210078"
  publishersca="0xbEE4730F42fEe0756A3bC6d34C04D8dB17fe1758"
  votessca="0x14eA1a75a615f3392Ad71F309699e84866fc3C1C"

  #ganache-cli --account="0x0bce878dba9cce506e81da71bb00558d1684979711cf2833bab06388f715c01a,100000000000000000000" --account="0xff7da9b82a2bd5d76352b9c385295a430d2ea8f9f6f405a7ced42a5b0e73aad7,100000000000000000000" &

  echo waiting for ganache to initialize ...
  #sleep 5

  echo deploying Treasury ...
  pushd ../../treasury/scripts
  #node cli.js 0 0 deploy
  popd

  echo deploying/stocking test Membership contract ...
  pushd ../ethereum/membership
  #node cli.js 0 0 deploy
  #node cli.js 0 "$membershipsca" setTreasury "$treasurysca"
  #node cli.js 0 "$membershipsca" setApproval "$account0" "true"
  #node cli.js 0 "$membershipsca" paydues 100
  popd

  echo deploying test Publisher contract ...
  pushd ../ethereum/publisher
  #node cli.js 0 0 deploy
  #node cli.js 0 "$publishersca" setTreasury "$treasurysca"
  #node cli.js 0 "$publishersca" setMembership "$membershipsca"
  popd

  echo deploying test Votes contract ...
  pushd ../ethereum/votes
  #node cli.js 0 0 deploy
  #node cli.js 0 "$votessca" setTreasury "$treasurysca"
  #node cli.js 0 "$votessca" setMembership "$membershipsca"
  popd

  runminer
fi

