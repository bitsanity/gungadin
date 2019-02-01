export LD_LIBRARY_PATH=$HOME/secp256k1/.libs

JLIB=-Djava.library.path=../jni

WEBCAM=\
$HOME/webcam-capture/webcam-capture-0.3.10.jar:\
$HOME/webcam-capture/libs/slf4j-api-1.7.2.jar:\
$HOME/webcam-capture/libs/bridj-0.6.2.jar:\
../lib/v4l4j-0.9.1-r507.jar:\
../lib/webcam-capture-driver-v4l4j-0.3.11.jar

JARS=\
../lib/hsqldb.jar:\
../lib/zxing-core-3.2.1.jar:\
../lib/zxing-javase-3.2.1.jar:\
../lib/json-simple-1.1.1.jar:\
../ethereum/bindings/.:\
../lib/tbox.jar:\
$WEBCAM
