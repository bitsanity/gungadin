#!/bin/bash

source env.sh

java $JLIB -cp $JARS:./java:./res:. pubui.PublicUI

