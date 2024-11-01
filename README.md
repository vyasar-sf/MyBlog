# MyBlog Spring Boot App
<br>

- [x] Version 0.3.0 (ongoing)
<br>

## Features

- Java 17
- Spring Boot 3.3
- Maven
- MySQL
- Spring Data JPA
- Relational Database
- REST Services
- Java Bean Validation
- Docker
- JUnit 5 (Mockito)
- Integration Tests with Test Containers
- Authentication with JWT
- Swagger
- Logging with Slf4j
- Hibernate Search

<br>

## Setup

Make sure `application.properties` has these to establish connection with PostgreSQL:
```properties
spring.application.name=myblog
server.servlet.context-path=/myblog

# MySQL configuration
spring.datasource.url=jdbc:mysql://localhost:3306/myblogdb?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect

# Secret Key for JWT
application.security.jwt.secret-key=35e8250dc2ad986f0c5ffefa4dde5903a785d74182e598a8d23853b7c7f6982b
# JWT expiration time in milliseconds (24 hours)
application.security.jwt.expiration=86400000

# Swagger customizations
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true

# Hibernate Search properties
hibernate.search.backend.directory.type=local-heap
hibernate.search.backend.directory.root = /path/to/indexes
```
and `application-test.properties` for test profile for H2 database:
```properties
# Deprecated
# Currently using test containers instead of H2
#spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
#spring.datasource.driverClassName=org.h2.Driver
#spring.datasource.username=sa
#spring.datasource.password=password
#spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
#spring.jpa.hibernate.ddl-auto=update
#spring.jpa.show-sql=true
```
