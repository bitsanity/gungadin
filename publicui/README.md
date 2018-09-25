# Public UI

User Interface available to anyone/everyone to identify, retrieve and decrypt
own files.

## Dev Dependencies

1. nw.js
  * run-time environment composed of a stripped-down chrome browser and the
    node.js javascript runtime engine.
  * https://nwjs.io

2. indutny/elliptic
  * javascript library (npm module) to do operations with curve secp256k1
  * enables the UI to sign/verify messages
  * https://github.com/indutny/elliptic

3. ricmoo/aes-js
  * for symmetric 256-bit cryptography
  * decrypts private keys and files
  * https://github.com/ricmoo/aes-js

3. talmobi/tor-request
  * enables this UI to talk to clientservices (Ethereum proxy) over TOR
  * https://github.com/talmobi/tor-request
