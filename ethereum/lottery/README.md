# lottery

Subsystem of gungadin that interfaces with an Ethereum client such as geth
using the javascript web3 api

Functions:

* watches the Votes contract on the Ethereum blockchain
* executes a Lottery for all nodes that correctly voted on block N-100, where
N is the latest Ethereum block number
* determines a winner for the lottery
* ensures the winner voted "correctly" according to the majority vote for
that block
* note that any block may contain transactions for zero or a number of shards

## Requirements

1. Ethereum wallet has been set up and the node operator has created an account
2. Requires the key's passphrase on startup to unlock the node's private key

## Dev Dependencies

[node.js (any recent version)](https://nodejs.org/en/)
[web3 npm module 1.0.0+](https://www.npmjs.com/package/web3)

