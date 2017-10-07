#!/bin/bash

source env.sh

java $JLIB -cp $JARS:.:./java tbox.Tests
