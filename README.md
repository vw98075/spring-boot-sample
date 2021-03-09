# spring-boot-sample

# A Sample Spring Boot Application

## Basic Functionalities

It comes with the following basic features:

- Relational database for data persist
- Spring Date JPA for ORM
- RESTful APIs with Spring MVC
- Integration Test

Testing with cURL

    curl -i  http://localhost:8080/customers/1;echo

    curl -X PUT localhost:8080/customers/3 -H 'Content-type:application/json' -d '{"firstName": "Samwise", "lastName":"Gamgee"}'

    curl -X PUT localhost:8080/customers/13 -H 'Content-type:application/json' -d '{"firstName": "Samwise", "lastName":"Gamgee"}'                 ;echo

    curl -X POST localhost:8080/customers -H 'Content-type:application/json' -d '{"firstName": "Joe", "lastName":"Smith"}'                 ;echo

    curl -v  http://localhost:8080/customers/search?last-name=Bauer;echo

    curl -v  http://localhost:8080/customers/?last-name=Bauer;echo

## Containerization 

### Docker

To build a docker image

    ./mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=my-sample

Check the image

    docker images

Run the images

    docker run -p 8080:8080 my-sample
