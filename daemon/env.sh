export LD_LIBRARY_PATH=$HOME/secp256k1/.libs

JLIB=-Djava.library.path=../jni

JARS=\
../lib/hsqldb.jar:\
../lib/core-3.2.1.jar:\
../lib/javase-3.2.1.jar:\
../lib/json-simple-1.1.1.jar:\
$HOME/web3j/lib/core-2.3.1.jar:\
$HOME/web3j/lib/bcprov-jdk15on-1.54.jar:\
$HOME/web3j/lib/scrypt-1.4.0.jar:\
$HOME/web3j/lib/jackson-annotations-2.8.0.jar:\
$HOME/web3j/lib/jackson-databind-2.8.5.jar:\
$HOME/web3j/lib/jackson-core-2.8.5.jar:\
$HOME/web3j/lib/rxjava-1.2.4.jar:\
../lib/tbox.jar:\
../ethereum/bindings/.:

