# Hello Service Sample

This is the WSO2 Microservices Server Hello World sample that uses sessions to keep a count of the number of requests 
that each user makes.

## How to build the sample

From this directory, run

```
mvn clean install
```

## How to run the sample

From the target directory, run
```
java -jar helloworld-session-*.jar
```

## How to test the sample

Go to the following url using the browser. Make sure to allow cookies.

```
http://localhost:8080/hello/wso2
```

You should get a response similar to the following and the count should get incremented when you repeatedly go to the
url with the browser.

```
Hello wso2 count 1
```
