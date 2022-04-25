# Makefile for the online bookstore

default: Node.class Connection.class Transaction.class Driver.class

Connection.class: Connection.java
	javac Connection.java
Node.class: Node.java
	javac Node.java

Transaction.class: Transaction.java
	javac Transaction.java
Driver.class: Driver.java
	javac Driver.java

clean:
	-rm -f *.class

all: clean Node.class Connection.class Transaction.class Driver.class