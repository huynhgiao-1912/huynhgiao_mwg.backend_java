#!/bin/sh

cd /root/app
ls -lh
echo "1. build mavenparent..."
cd mwg.mavenparent
mvn clean install