#!/bin/bash

javah -cp ../java:. tbox.Secp256k1
javah -cp ../java:. tbox.RIPEMD160

gcc -D __int64="long long" -c -fPIC -shared rmd160.c

gcc -D __int64="long long" -c -fPIC -I"$HOME/secp256k1/include" -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/linux" -shared tbox_Secp256k1.c

gcc -D __int64="long long" -c -fPIC -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/linux" -shared tbox_RIPEMD160.c

gcc -shared -o ../../jni/libtbox.so tbox_Secp256k1.o tbox_RIPEMD160.o rmd160.o $HOME/secp256k1/.libs/libsecp256k1.so

