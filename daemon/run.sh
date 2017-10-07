#!/bin/bash

source env.sh

java $JLIB -cp $JARS:./java:. gungadaemon.Daemon ethwalletpath=$HOME/.gungadin/UTC--2016-10-23T18-50-15.853386528Z--8e9342eb769c4039aaf33da739fb2fc8af9afdc1
