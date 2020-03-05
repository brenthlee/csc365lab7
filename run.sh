#!/bin/sh

export APP_JDBC_URL="jdbc:mysql://db.labthreesixfive.com/blee96?autoReconnect=true&useSSL=false"
export APP_JDBC_USER="hluengas"
export APP_JDBC_PW="WinterTwenty20_365_010845974"
javac *.java
java -cp ./mysql-connector-java-8.0.16.jar:. InnReservations
rm *.class
