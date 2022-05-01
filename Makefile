# Makefile for the online bookstore
LIBS=/home/maydogdu/bowdoin-coin
NEW_CLASSPATH=lib/*:${CLASSPATH}

SRC = $(wildcard *.java) 

all: build

build: ${SRC}
	${JAVA_HOME}/bin/javac -Xlint -cp ${NEW_CLASSPATH} ${SRC}
	${JAVA_HOME}/bin/jar cvf bcprov-jdk18on-171.jar *.class lib