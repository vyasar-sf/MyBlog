## MyBlog App with Spring Boot and PostgreSQL
<br>

- [x] Version 0.1.0 (ongoing)
<br>

- Java 17
- Spring Boot 3.3
- Maven
- Lombok
- Spring Data JPA
- Spring Security
- Global Exception Handler
- Relational Database
- REST Services
<br>


For setup, make sure `application.properties` has these to establish connection with PostgreSQL:
```
spring.application.name=myblog
server.servlet.context-path=/myblog
spring.datasource.url=jdbc:postgresql://localhost:5432/MyBlogDB
spring.datasource.username=yourusername
spring.datasource.password=yourpassword
```
