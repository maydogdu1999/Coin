# Makefile for the online bookstore

SRC = $(wildcard *.java) 

all: build

build: ${SRC}
	javac -Xlint ${SRC}
