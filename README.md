# MyBlog Spring Boot App with PostgreSQL
<br>

- [x] Version 0.1.0 (ongoing)
<br>

## Features

- Java 17
- Spring Boot 3.3
- Maven
- Spring Data JPA
- Relational Database
- REST Services
- Java Bean Validation
- Global Exception Handler
- JUnit 5 (Mockito) and Integration Tests (MockMvc and H2 database)
- Test coverage of 87% (JaCoCo)

<br>

## Setup

Make sure `application.properties` has these to establish connection with PostgreSQL:
```properties
spring.application.name=myblog
server.servlet.context-path=/myblog
spring.datasource.url=jdbc:postgresql://localhost:5432/MyBlogDB
spring.datasource.username=yourusername
spring.datasource.password=yourpassword
```
and `application-test.properties` for test profile for H2 database:
```properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

<br>

`CREATE TABLE` scripts:

```sql
CREATE TABLE post (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255)
    text TEXT
);

CREATE TABLE tag (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE
);

CREATE TABLE post_tag (
    post_id INT REFERENCES post(id) ON DELETE CASCADE,
    tag_id INT REFERENCES tag(id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, tag_id)
);
```

