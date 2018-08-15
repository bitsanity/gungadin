# ethgateway

Subsystem of gungadin that interfaces with an Ethereum client such as geth
using the javascript web3 api

Exchanges digitally-signed JSON-RPC messages with the daemon (Java) process
for instructions. Also establishes a ws connection to geth running on
localhost and monitors the Ethereum blockchain for relevent events, which it
passes along to the daemon.

## Requirements

1. Ethereum wallet has been set up and the node operator has created an account
2. Requires the key's passphrase on startup to unlock the node's private key

## Dev Dependencies

[node.js (any recent version)](https://nodejs.org/en/)
[web3 npm module 1.0.0+](https://www.npmjs.com/package/web3)
