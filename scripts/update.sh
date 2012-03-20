#!/bin/bash

cd /home/media/apache-tomcat/bin
# ./shutdown sh

cd /home/media
rm -Rf src
mkdir src
cd src
git clone git@github.com:k-int/AggregatorCore.git
git clone git@github.com:k-int/Media.git


cd ~/src/aggregator
git checkout release
git pull
cd ~/src/aggregator/HandlerRegistry
grails prod war
cd ~/src/aggregator/repository
grails prod war

cp ~/src/AggregatorCore/repository/target/repository-0.1.war ~/apache-tomcat/webapps/repository.war
cp ~/src/AggregatorCore/HandlerRegistry/target/HandlerRegistry-0.1.war ~/apache-tomcat/webapps/HandlerRegistry.war
