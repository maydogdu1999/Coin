#!/bin/sh

for VARIABLE in 4 5 6 7 8 9 10
do
openssl genrsa -out private_key$VARIABLE.pem 2048
openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key$VARIABLE.pem -out private_key$VARIABLE.der -nocrypt
openssl rsa -in private_key$VARIABLE.pem -pubout -outform DER -out public_key$VARIABLE.der
done