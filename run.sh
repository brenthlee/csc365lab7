#!/bin/sh

export APP_JDBC_URL="jdbc:mysql://db.labthreesixfive.com/blee96?autoReconnect=true&useSSL=false"
export APP_JDBC_USER="blee96"
export APP_JDBC_PW="WinterTwenty20_365_011115373"
javac *.java
java -cp ./mysql-connector-java-8.0.16.jar:. InnReservations
rm *.class
