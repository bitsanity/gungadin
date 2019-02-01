# gungadin (or "gd")

Encrypted Files on a Blockchain

https://en.wikipedia.org/wiki/Gunga_Din

## System Requirements

1. Linux 4.4+ (Ubuntu or Debian) x86_64
2. Highest-capacity storage (drive, NAS or cloud) available
3. Full-HD Camera capable of 1080p still capture
4. Full-HD or better screen resolution non-interlaced display
4. GNU Compiler Collection (gcc)

## Dev Dependencies

1. libsecp256k1
  * https://github.com/bitcoin-core/secp256k1
  * build scripts assume installed at $HOME/secp256k1

2. Oracle JDK 1.8+
  * Install UnlimitedJCEPolicy files

3. Apache Ant(TM) (for development)
  * http://ant.apache.org/

4. https://github.com/bitsanity/cryptils

5. web3
  * Javascript library interface to Ethereum
  * npm install web3
  * Apache 2.0 License

6. v4l4j-0.9.1-r507
  * git clone https://github.com/sarxos/v4l4j.git
  * GPL License

7. webcam-capture-driver-v4l4j-0.3.11
  * git clone https://github.com/sarxos/webcam-capture
  * MIT License

8. IPFS Java API
  * https://github.com/ipfs/java-ipfs-http-client
  * MIT License
  * needs multibase, multiaddr, multihash, cid libraries from /lib dir

