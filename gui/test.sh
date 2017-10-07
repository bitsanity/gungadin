#!/bin/bash

source env.sh

CLPTH=./dist/gungagui.jar:$JARS:./java:.

java $JLIB -cp $CLPTH gungagui.CameraView

