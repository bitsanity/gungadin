export LD_LIBRARY_PATH=$HOME/secp256k1/.libs

JLIB=-Djava.library.path=../jni

JARS=\
../lib/hsqldb.jar:\
../lib/core-3.2.1.jar:\
../lib/javase-3.2.1.jar:\
../lib/json-simple-1.1.1.jar:\
../lib/tbox.jar:\
./dist/gungadaemon.jar

