# Lambda Handlers Sample

With WSO2 MSS you can handle http requests easily with lambdas.
Following example shows how to handle requests with lambdas.

```java
new MicroservicesRunner()
                .deploy(
                        new HttpMethods()
                                .get("/hi", (request, response) -> "Hi GET")
                                .get("/hello", (request, response) -> "Hello GET")
                                .post("/hi", (request, response) -> "Hi POST")
                                .post("/hello", (request, response) -> "Hello POST")
                )
                .start();
```


How to build the sample
------------------------------------------
From this directory, run

```
mvn clean package
```

How to run the sample
------------------------------------------
Use following command to run the application
```
java -jar target/lambdahandlers-*.jar
```

How to tests the sample
------------------------------------------

Test the HTTP GET requests to /hello route
```
curl -v -X GET http://localhost:8080/hello
```

Test the HTTP GET requests to /hi route
```
curl -v -X GET http://localhost:8080/hi
```

Test the HTTP POST requests to /hello route
```
curl -v -X POST http://localhost:8080/hello
```

Test the HTTP POST requests to /hi route
```
curl -v -X POST http://localhost:8080/hi
```