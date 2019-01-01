#!/bin/bash

source env.sh

java $JLIB -cp $JARS:./java:. gungagui.AgentUI port=8804

