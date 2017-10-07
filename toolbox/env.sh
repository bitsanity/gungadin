export LD_LIBRARY_PATH=$HOME/secp256k1/.libs

JLIB=-Djava.library.path=../jni

JARS=\
../lib/scrypt-1.4.0.jar:\
../lib/zxing-core-3.2.1.jar:\
../lib/zxing-javase-3.2.1.jar:\
../lib/tbox.jar
