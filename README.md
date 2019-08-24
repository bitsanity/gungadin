# gungadin (or "gd")

Encrypted Files on a Blockchain

https://en.wikipedia.org/wiki/Gunga_Din

## Requirements/Dependencies

1. Linux 4.4+ (Ubuntu or Debian) x86_64
2. Highest-capacity storage (drive, NAS or cloud) available
3. Full-HD Camera capable of 1080p still capture
4. Full-HD or better screen resolution non-interlaced display
5. GNU Compiler Collection (gcc)
6. Java (OpenJDK version)

## Dependencies/Setup

0. Prerequisites
  * git ("sudo apt install git")
  * libtool ("sudo apt install libtool")
  * autoconf ("sudo apt install autoconf")

1. Node.js (Javascript engine) and modules
  * "sudo apt install nodejs"
  * "sudo apt install npm"
  * "ln -s /usr/bin/nodejs /usr/bin/node"
  * "sudo npm install -g web3 solc elliptic keythereum lokijs fs net http"

2. Geth (go-ethereum)
  * https://github.com/ethereum/go-ethereum/wiki/Installation-Instructions-for-Ubuntu

3. Bitcoin-core's secp256k1 library
  * clone https://github.com/bitcoin-core/secp256k1 in $HOME/secp256k1
  * "./autogen.sh"
  * "./configure --enable-experimental --enable-module-recovery --enable-module-schnorrsig"
  * "make"
  * "make check"

4. Open Java Development Kit (JDK)
  * Oracle's Java is unsuitable due to licensing concerns
  * "sudo apt install default-jdk"
  * update .bashrc with JAVA_HOME definition
  * Install JCE UnlimitedJCEPolicy files

5. Apache Ant(TM)
  * http://ant.apache.org/

6. Java Crypto Library (uses bitcoin-core/secp256k1)
  * mkdir $HOME/projects && cd !$
  * "git clone https://github.com/bitsanity/cryptils"

7. v4l4j-0.9.1-r507
  * included in gungadin, or
  * git clone https://github.com/sarxos/v4l4j.git
  * GPL License

8. webcam-capture-driver-v4l4j-0.3.11
  * included in gungadin, or
  * git clone https://github.com/sarxos/webcam-capture
  * MIT License

9. IPFS Java API
  * included in gungadin, or
  * https://github.com/ipfs/java-ipfs-http-client
  * MIT License
  * needs multibase, multiaddr, multihash, cid libraries from /lib dir

