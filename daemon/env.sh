export LD_LIBRARY_PATH=$HOME/secp256k1/.libs

JLIB=-Djava.library.path=../jni

WEB3J=\
$HOME/web3j/lib/abi-3.1.1.jar:\
$HOME/web3j/lib/codegen-3.1.1.jar:\
$HOME/web3j/lib/console-3.1.1.jar:\
$HOME/web3j/lib/core-3.1.1.jar:\
$HOME/web3j/lib/crypto-3.1.1.jar:\
$HOME/web3j/lib/infura-3.1.1.jar:\
$HOME/web3j/lib/rlp-3.1.1.jar:\
$HOME/web3j/lib/tuples-3.1.1.jar:\
$HOME/web3j/lib/utils-3.1.1.jar:\
$HOME/web3j/lib/okhttp-3.8.1.jar:\
$HOME/web3j/lib/okio-1.13.0.jar:\
$HOME/web3j/lib/jackson-annotations-2.8.0.jar:\
$HOME/web3j/lib/jackson-databind-2.8.5.jar:\
$HOME/web3j/lib/jackson-core-2.8.5.jar:\
$HOME/web3j/lib/slf4j-api-1.7.25.jar:\
$HOME/web3j/lib/rxjava-1.2.4.jar

JARS=\
$WEB3J:\
../lib/hsqldb.jar:\
../lib/core-3.2.1.jar:\
../lib/javase-3.2.1.jar:\
../lib/json-simple-1.1.1.jar:\
../lib/tbox.jar:\
../ethereum/bindings/.:\
./dist/gungadaemon.jar

