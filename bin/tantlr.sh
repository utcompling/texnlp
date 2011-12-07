#!/bin/sh
TEXNLP_LIB=$TEXNLP_DIR/lib
DIRLIBS=$TEXNLP_LIB/trove.jar:$TEXNLP_LIB/cli.jar:$TEXNLP_LIB/antlr-runtime-3.0.1.jar:$TEXNLP_LIB/antlr-3.0.1.jar:$TEXNLP_LIB/antlr-2.7.7.jar:$TEXNLP_LIB/stringtemplate-3.1b1.jar
CP=.:${TEXNLP_DIR}/output/classes:${DIRLIBS}
JAVA=$JAVA_HOME/bin/java
#$JAVA -classpath $CP org.antlr.Tool $@
$JAVA -classpath $CP $@
