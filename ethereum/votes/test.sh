#!/bin/bash

TESTPVTA='0x0bce878dba9cce506e81da71bb00558d1684979711cf2833bab06388f715c01a'
TESTPVTB='0xff7da9b82a2bd5d76352b9c385295a430d2ea8f9f6f405a7ced42a5b0e73aad7'
TESTACCTA='0x8c34f41f1cf2dfe2c28b1ce7808031c40ce26d38'
TESTACCTB='0x147b61187f3f16583ac77060cbc4f711ae6c9349'

SCA="0xF68580C3263FB98C6EAeE7164afD45Ecf6189EbB"
MOCK="0x4Ebf4321A360533AC2D48A713B8f18D341210078"

echo CONFIRM: are you running:
echo ""
echo ganache-cli --account="<privatekey>,balance"
echo "         " --account="<privatekey>,balance"
echo ""
read -p '[N/y]: ' ans
if [[ $ans != "y" && $ans != "Y" ]]; then
  echo ""
  echo Please run the following before this:
  echo ""
  echo -n ganache-cli ""
  echo -n --account=\"$TESTPVTA,100000000000000000000\" ""
  echo  --account=\"$TESTPVTB,100000000000000000000\"
  echo ""
  exit
fi

echo ""
echo deploying Votes
node cli.js 0 1 0 deploy

echo ""
echo deploying MembershipMock
node deployMock.js

echo ""
echo linking MembershipMock to Votes contract
node cli.js 0 1 $SCA setMembership $MOCK

echo ""
echo setting fee to something non-zero
node cli.js 0 1 $SCA setFee 1

echo ""
echo setting mock to return true for ismember and approved
node setMock.js $MOCK "true" "true"

echo ""
echo vote
node cli.js 0 1 $SCA vote 1 "hash1"

echo ""
echo setting mock to return false for ismember and true for approved
node setMock.js $MOCK "false" "true"

echo ""
echo vote "(should fail)"
node cli.js 0 1 $SCA vote 2 "hash2"

echo ""
echo setting mock to return true for ismember and false for approved
node setMock.js $MOCK "true" "false"

echo ""
echo vote "(should fail)"
node cli.js 0 1 $SCA vote 3 "hash3"

echo ""
echo resulting events
node cli.js 0 1 $SCA events
