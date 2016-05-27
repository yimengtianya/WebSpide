#!/bin/sh
export LANG=zh_CN.GBK

APP=WebSpideServer.jar 
APP_PARAS=-c ./webspide.ini

#Jconsole_OPTS="-Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"
Code_OPTS="-Duser.country=ES -Duser.language=es -Duser.variant=Traditional_WIN -Dfile.encoding=UTF-8"
JAVA_OPTS="-Xverify:none -Xms1024M -Xmx1024M -Xmn600M -Xss1M -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+UseParNewGC  -XX:CMSInitiatingOccupancyFraction=85 -XX:MaxTenuringThreshold=0"

while [ -f $APP ]
do
java $JAVA_OPTS $Code_OPTS $Jconsole_OPTS -jar $APP $APP_PARAS
done
