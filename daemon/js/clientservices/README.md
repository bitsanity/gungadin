# Client User Interface Services

Simple daemon/service to provide minimal read-only geth access to clients.
Speaks JSON-RPC with any clientui.

Usual scenario is client wants to scan the events log for all hashes
associated with a specified public key. Does not use Ethereum account.

## Dependencies

[node.js (any recent version)](https://nodejs.org/en/)
[web3 npm module 1.0.0+](https://www.npmjs.com/package/web3)
[indutny/elliptic 6.4.1](https://github.com/indutny/elliptic)
