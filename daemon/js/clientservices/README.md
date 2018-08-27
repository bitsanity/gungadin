# Client User Interface Services

Simple daemon/service to provide minimal read-only geth access to clients.
Speaks JSON-RPC with any clientui.

Usual scenario is client wants to scan the events log for all hashes
associated with a specified public key. Does not use Ethereum account.

## Dev Dependencies

1. node.js
  * https://nodejs.org/

2. ethereum web3 module
  * https://github.com/ethereum/web3.js/

