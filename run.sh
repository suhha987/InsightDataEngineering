#!/usr/bin/env bash

javac -g  -cp external_jars/java-json.jar:external_jars src/Insight.java
java -cp external_jars/java-json.jar:./src/ Insight