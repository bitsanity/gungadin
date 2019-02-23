# lottery

Subsystem of gungadin interfacing with an Ethereum client such as geth using
the javascript web3 api

Functions:

* watches the Votes contract on the Ethereum blockchain
* executes a Lottery for all nodes that correctly voted on block N-x, where
  N is the latest Ethereum block number and x is the number of blocks we wait
  to allow all the nodes to vote
* determines a winner for the lottery
* ensures the winner voted "correctly" according to the majority vote for
  that block
* issues the lottery winnings

Notes:

* a block may contain events for one or more shards
* no winner if fewer than three nodes vote on a block within the x-block window

## Requirements

1. Ethereum wallet has been set up and the node operator has created an account
2. Requires the key's passphrase on startup to unlock the node's private key

## Dependencies

[node.js (any recent version)](https://nodejs.org/en/)
[web3 npm module 1.0.0+](https://www.npmjs.com/package/web3)
[LokiJS](http://lokijs.org/)

