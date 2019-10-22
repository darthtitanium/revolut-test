# revolut-test

Building app:
mvn package

Running app from project home:
java -cp "target/*" com.test.money.transfer.BankWebServer

App Usage:

- http://localhost:8888/createAccount?name=a&balance=100

	Creates account with name a and balance 100

- http://localhost:8888/transfer?from=a&to=b&sum=10
	
	Transfers from account a to account b sum of 10

- http://localhost:8888/getBalance?name=a
	
	Gets balance of account a