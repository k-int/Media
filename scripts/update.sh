#!/bin/bash


cd /home/media/apache-tomcat-7.0.25/bin
# ./shutdown sh
cd /home/media
rm -Rf src
mkdir src
cd src
# git clone git@github.com:ianibo/XCRI-Aggregator.git
git clone git@developer.k-int.com:aggregator.git
git clone git@github.com:k-int/Media.git


cd ~/src/aggregator
git checkout release
git pull
cd ~/src/aggregator/HandlerRegistry
grails prod war
cd ~/src/aggregator/repository
grails prod war
cd ~/src/Media/utils/cgimp
grails prod war


# cd ~/src/XCRI-Aggregator
# git checkout release
# git pull
# cd ~/src/XCRI-Aggregator/XCRISearch
# grails prod war
# cd ~/src/XCRI-Aggregator/FeedManager
# grails prod war


cp ~/src/aggregator/repository/target/repository-0.1.war ~/apache-tomcat-7.0.23/webapps/repository.war
cp ~/src/aggregator/HandlerRegistry/target/HandlerRegistry-0.1.war ~/apache-tomcat-7.0.23/webapps/HandlerRegistry.war
# cp ~/src/aggregator/XCRISearch/target/HandlerRegistry-0.1.war ~/apache-tomcat-7.0.23/webapps
# cp ~/src/XCRI-Aggregator/FeedManager/target/FeedManager-0.1.war ~/apache-tomcat-7.0.23/webapps
