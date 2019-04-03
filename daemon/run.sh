#!/bin/bash

# Functions ------------------------------------------------------------------

runminer() {

  echo -n "Run ipfs daemon? "
  read -p '[N/y]: ' -n 1 -r
  echo
  if [[ $REPLY =~ ^[Yy]$ ]]; then
    nohup nice ipfs daemon --enable-pubsub-experiment &> $HOME/temp/ipfs.out &
    sleep 5
  fi

  echo -n "Run ethgateway? "
  read -p '[N/y]: ' -n 1 -r
  echo
  if [[ $REPLY =~ ^[Yy]$ ]]; then
    pushd js/ethgateway
    nohup node ethgw.js \
      $egwport \
      $daemonegwinport \
      $daemonpubkey \
      $publishersca \
      $votesca \
      $acctindex \
      "$acctpass" &> $HOME/temp/ethgw.out &
    popd
    sleep 5
  fi

  echo -n "Run clientservices? "
  read -p '[N/y]: ' -n 1 -r
  echo
  if [[ $REPLY =~ ^[Yy]$ ]]; then
    pushd js/clientservices
    #nohup node clisvcs.js $publishersca &> $HOME/temp/clisvcs.out &
    popd
    sleep 5
  fi

  echo -n "Run lottery? "
  read -p '[N/y]: ' -n 1 -r
  echo
  if [[ $REPLY =~ ^[Yy]$ ]]; then
    pushd ./js/lottery
    nohup node lotto.js $votessca &> $HOME/temp/lotto.out &
    popd
    sleep 5
  fi

  echo -n "Run gungadaemon? "
  read -p '[N/y]: ' -n 1 -r
  echo
  if [[ $REPLY =~ ^[Yy]$ ]]; then
    nohup java $JLIB -cp $JARS:./java:. daemon.Daemon \
      intpassphrase="$intkeypassphrase" \
      uiport=$daemonuiport \
      egwinport=$daemonegwinport \
      egwoutport=$egwport \
      egwpeerpubkey=$ethgwpubkey \
      extaddress=$extaddress \
      intkeyfilepath=$intkeyfilepath \
      pubsdbfilepath=$pubsdbfilepath \
      hwmdbfilepath=$hwmdbfilepath \
      aclfilepath=$aclfilepath \
      ipfscachedir=$ipfscachedir &> $HOME/temp/daemon.out &
  fi
}

# main -----------------------------------------------------------------------
source env.sh

commd=$1

extaddress="0x258b75ec103c1b4bfdf5f2f40a9e2733703b4a5a"
ipfscachedir="$HOME/temp"
daemonuiport="8804"
daemonegwinport="8805"
egwport="8806"
ethgwpubkey="0x04734b27fd334b517855343e28ddda311c2200f19a3e7083ddf11cc40722927f1276e25c63c2dcba779cc82bc211378f33e452ba984a2f4ffaed263b9639aa6ba5"
pubsdbfilepath="publications.db"
aclfilepath="acl.db"
hwmdbfilepath="hwm.db"
intkeypassphrase="change-on-install"
intkeyfilepath="nodeid.blk"
daemonpubkey=`java $JLIB -cp $JARS:./java:. daemon.NodeIdentity "$intkeypassphrase" $intkeyfilepath`

if [ -z $commd ]
then
  membershipsca="0x5D1710Ec045AEcb93B7968d016bD15699560237a"
  publishersca="0xD3Cf41315DAf77A06bAde6A931f47C1AB98d2952"
  votesca="0xC6F5fD8356f54E1CE115b68A44042209b6252DFf"
  acctindex=8
  acctpassfile="./ethacctpass.txt"
  acctpass=`cat $acctpassfile`

  echo -n "Run geth? "
  read -p '[N/y]: ' -n 1 -r
  echo
  if [[ $REPLY =~ ^[Yy]$ ]]; then
    gethapis="web3,eth,net,db,personal"
    nohup geth --syncmode light \
      --cache 4096  \
      --rpc --rpcapi $gethapis \
      --ws --wsorigins "*" --wsapi $gethapis \
      --unlock "$extaddress" --password "$acctpassfile" \
      &> $HOME/temp/geth.out &
    sleep 5
  fi

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
  acctindex=0
  acctpass="testpassword"

  #ganache-cli --account="0x0bce878dba9cce506e81da71bb00558d1684979711cf2833bab06388f715c01a,100000000000000000000" --account="0xff7da9b82a2bd5d76352b9c385295a430d2ea8f9f6f405a7ced42a5b0e73aad7,100000000000000000000" &

  echo waiting for ganache to initialize ...
  sleep 20

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

