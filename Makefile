#Brent Lee, Henry Luengas, Ryan Nevils
#java -cp ./mysql-connector-java-8.0.16.jar:. InnReservations

run:
	@javac *.java
	@java -cp ./mysql-connector-java-8.0.16.jar:. InnReservations

clean:
	@rm *.class
