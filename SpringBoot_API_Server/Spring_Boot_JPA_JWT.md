
Spring Boot - REST - JPA database - Spring Security with JWT
==========

- v1.1 - 2018.11.21 - changes:
    - refactoring and update md doc for transactions

- v1.0 - 2018.10.19 - first version

-----

__Requirements__:
- install Java JDK: [https://openjdk.java.net/]
- install Maven: [https://maven.apache.org/]
- access to a database (can be used H2 embedded)

**For more info:**
- [https://www.shortn0tes.com/2018/01/spring-boot-web-app-intellij-idea-community.html]
- [https://blog.jetbrains.com/idea/2018/04/spring-and-spring-boot-in-intellij-idea-2018-1/]
- [https://spring.io/guides/gs/accessing-data-rest/
- [https://www.leveluplunch.com/java/tutorials/014-post-json-to-spring-rest-webservice/]
- [https://spring.io/guides/gs/securing-web/]
- [https://spring.io/projects/spring-security-oauth]
- [http://websystique.com/spring-security/secure-spring-rest-api-using-oauth2/]
- [https://hellokoding.com/registration-and-login-example-with-spring-security-spring-boot-spring-data-jpa-hsql-jsp/]
- [https://medium.com/@nydiarra/secure-a-spring-boot-rest-api-with-json-web-token-reference-to-angular-integration-e57a25806c50]

-----

__Note__: 
- this project use JPA and H2 database but can be converted to any JPA database (MySQL, PostgreSQL, Oracle, etc) by changing the database connection in the application.properties file. 
- I recommend creating a curl alias: __curlj__ with the command below (can be added to ~ / .bash_profile or ~ / .bashrc):
`
    alias curlj='curl --header "Content-Type: application/json" -w "\n" -v'
`
  - add at the end of the curlj cmd: ` | json_pp ` for formatted display of the json response
  - the -v parameter in curl can be removed if there is no need to display headers for request and response
  
-----

# 1. Create Spring Boot JPA project

Create a new java maven project SpringBoot using pom.xml generated with:
__Spring Initializr:__
[https://start.spring.io/]( https://start.spring.io/ )

- select "Switch to the full version"
- add for Group: api-server (or any other relevant name)
- Artifact: spring-jpa
- Name: api_spring_jpa
- and check: Security, Web, Jersey (JAX-RS), JPA, MySQL, H2, JDBC, PostgreSQL
- download the spring project zip and unzip the archive in the working folder

- edit pom.xml and add in <dependencies> section:
    - io.jsonwebtoken for JWT lib
    - com.google.code.gson for JsonOnject class
```xml
    <!-- https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt -->
    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt</artifactId>
      <version>0.9.1</version>
    </dependency>
    <!-- https://mvnrepository.com/artifact/com.google.code.gson/gson -->
    <dependency>
      <groupId>com.google.code.gson</groupId>
      <artifactId>gson</artifactId>
    </dependency>
	
```
- edit pom.xml and add in <build> - <plugins> section:
    - fmt-maven-plugin for automatic reformats java source code files to comply with Google Java Style
    - useSystemClassLoader: false for maven-surefire-plugin in order to fix some build errors in OpenJDK on ubuntu/debian

```xml
    <plugin>
        <groupId>com.coveo</groupId>
        <artifactId>fmt-maven-plugin</artifactId>
        <version>2.6.0</version>
        <executions>
        <execution>
            <goals>
            <goal>format</goal>
            </goals>
        </execution>
        </executions>
    </plugin>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <configuration>
        <useSystemClassLoader>false</useSystemClassLoader>
        </configuration>
    </plugin>
```

- edit pom.xml and add in <properties> section maven.test.skip: true because we still do not have a communication with a database and unit tests ready:
```xml
    <maven.test.skip>true</maven.test.skip>
```

- open a terminal in the project folder:
```
    mvn install
    mvn spring-boot:run
```

Load maven project (pom.xml) in your favorite IDE:
- in NetBeans also add "NB SpringBoot" plugin: nb-springboot
https://github.com/AlexFalappa/nb-springboot

- or in Eclipse Marketplace -> install Spring Tools (is in Popular Tab) and restart Eclipse


-----

# 2. Configure JPA for database

In the sources folder, create a resource file: src/main/resources/application.properties
For MySQL add your db details in format: 
```
spring.datasource.url=jdbc:mysql://<server-ip>:<port>/<database_name>
```
[https://spring.io/guides/gs/accessing-data-mysql/]

For PostgreSQL add your db details in format: 
```
spring.datasource.url=jdbc:postgresql://<server-ip>:<port>/<database_name>
```
For H2 embeded database use:
```
spring.jpa.hibernate.ddl-auto=none
spring.datasource.url=jdbc:h2:~/h2db_test;AUTO_SERVER=TRUE;CACHE_SIZE=262144;AUTO_SERVER_PORT=9498
spring.datasource.username=sa
spring.datasource.password=sa
server.port = 8060
```
__Note__: Here, spring.jpa.hibernate.ddl-auto can be none, update, create, create-drop, refer to the Hibernate documentation for details.

## Create table for tests or use: spring.jpa.hibernate.ddl-auto=create
- create the test table (the sql cmd is for H2 / MySQL) using UUID for Primary Key and also create the index for queries as below:
```sql
 CREATE TABLE MY_TABLE_JPA (
   id varchar(36) NOT NULL, -- UUID PK - length is always 36 chars
   name varchar(100) NOT NULL,
   additional_id NUMERIC,
   data_list varchar(250),
   updates NUMERIC,
   created_at DATETIME,
   last_updated_at DATETIME,
   CONSTRAINT my_table_pk PRIMARY KEY (id));
   
CREATE INDEX my_table_name_idx ON MY_TABLE_JPA(name, last_updated_at desc);
```
__Note:__ 
- for database tool you can install [DBeaver]( https://dbeaver.io/ )
- Spring Boot runs schema-@@platform@@.sql automatically during startup. -all is the default for all platforms.
So, all sql commands can be automatically executed at springboot start if the sql cmds appear in the src/main/resources/schema-all.sql file 



-----

# 3. Create JPA entity class for storing data
[https://spring.io/guides/gs/accessing-data-jpa/]( https://spring.io/guides/gs/accessing-data-jpa/ )

- create a new java package (folder): apiserver.spring_jpa.entities
- create MyTableModel java class as in the example below:
```java
package apiserver.spring_jpa.entities;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/** @author */
@Entity
@Table(name = "MY_TABLE_JPA")
@XmlRootElement
@NamedQueries({
  @NamedQuery(
      name = "MyTableModel.findById",
      query = "SELECT t FROM MyTableModel t WHERE t.id = :id"),
  @NamedQuery(
      name = "MyTableModel.findByName",
      query = "SELECT t FROM MyTableModel t WHERE t.name = :name"),
})
public class MyTableModel implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "ID")
  private String id;

  @Column(name = "NAME")
  private String name;

  @Column(name = "DATA_LIST")
  private String dataList;

  @Column(name = "created_at")
  @Temporal(TemporalType.TIMESTAMP)
  private Date createdAt;

  @Column(name = "UPDATES")
  private long updates;

  @Column(name = "LAST_UPDATED_AT")
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastUpdatedAt;

  protected MyTableModel() {
    // constructor only for use by persistence infrastructure
  }

  public MyTableModel(JsonObject jsonRequest) {
    this.id = UUID.randomUUID().toString();
    if (jsonRequest.has("name")) {
      this.name = jsonRequest.get("name").getAsString();
    } else {
      String testName = "test-" + this.id;
      this.name = testName;
    }
    if (jsonRequest.has("dataList")) {
      if(jsonRequest.get("dataList").isJsonArray()){ // dataList can be sent as json array:
        this.dataList = jsonRequest.get("dataList").getAsJsonArray().toString();
      }else{
        this.dataList = jsonRequest.get("dataList").getAsString();
      }
    }
    this.createdAt = new java.sql.Timestamp(System.currentTimeMillis());
    this.lastUpdatedAt = createdAt;
    this.updates = 0;
  }

  public void update(JsonObject jsonRequest) {
    if (jsonRequest.has("name")) {
      this.name = jsonRequest.get("name").getAsString();
    }
    if (jsonRequest.has("dataList")) {
      this.dataList = jsonRequest.get("dataList").getAsString();
    }
    this.updates += 1;
    this.lastUpdatedAt = new Date(System.currentTimeMillis());
  }

  public JsonElement toJsonObject(Gson gson) {
    return gson.toJsonTree(this);
  }

// getters & setters [...]
}
```

- in DbTable1.java you must add get/set for all private fields

__Note__: 
- in NetBeans you can use the option: "create new Entity Class from Database connection"
- a NUMERIC field in database will be automatically converted into a BigDecimal java type which can be replaced with Long
- for PK with Auto Increment use:

```java
            @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            @Basic(optional = false)
            @Column(name = "ID")
            private Long id;
```
        
- option for Eclipse: create new JPA project and choose the previous db connection and the corresponding Catalog and Schema
    - then create JPA Entities from Tables
    - and copy the new generated classes in the spring project 

# 4. Create a repository interface class which extend CrudRepository

In entities package create a new java interface: MyTableRepository as in the example below:

```java
package apiserver.spring_jpa.entities;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/** @author */
public interface MyTableRepository extends CrudRepository<MyTableModel, String> {
  // Important Note: for nativeQuery use table and fields names as in the table create SQL (DDL) and
  // not the java entity class name:
  // use MY_TABLE_JPA amd not MyTableModel
  @Query(
      value =
          "SELECT * FROM MY_TABLE_JPA t WHERE t.name = ?1 order by t.last_updated_at desc limit 10",
      nativeQuery = true)
  public List<MyTableModel> findFirst10ByName(String name);
}

```
Using this class we will access the MyTableModel entity from database adn we will have all commands for CRUD.

-----

# 5. Create a new REST controller: MyTableController

- create a new java package (folder): apiserver.spring_jpa.controllers
- create MyTableController java class as in the example below and add @Autowired MyTableRepository

```java
package apiserver.spring_jpa.controllers;

import apiserver.spring_jpa.entities.MyTableModel;
import apiserver.spring_jpa.entities.MyTableRepository;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** @author */
@RestController
@RequestMapping("/rest-jpa")
public class MyTableController {

  private static final Logger LOG = Logger.getLogger(MyTableController.class.getName());
  // Gson instances are Thread-safe so you can reuse them freely across multiple threads:
  private Gson gson;

  @Autowired private MyTableRepository tblRepository;

  @PostConstruct
  public void init() {
    gson = new Gson(); // we will pass this instance for all api method
  }

// [...]
}
```

Spring introduced the @Autowired annotation for dependency injection.

__Note__: 
- all API method from MyTableController will be accessible without authorization because these methods are only for speed tests.
- in the example below we use curlj as an alias for curl command (see more info in Testing section): 
```
curlj='curl --header "Content-Type: application/json" -w "\n" -v'
```

## 5.1. REST method for /addNew record:

```java
  /*
  curlj http://localhost:8060/rest-jpa/addNew -d '{"name":"test1", "dataList" : [1,2,3]}'
  */
  @PostMapping(value = "/addNew", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> addNew(@RequestBody String request) {
    try {
      JsonObject jsonRequest = gson.fromJson(request, JsonObject.class);
      MyTableModel newRec = new MyTableModel(jsonRequest);
      tblRepository.save(newRec);
      JsonObject jsonResponse = new JsonObject();
      jsonResponse.addProperty("responseCode", 0);
      jsonResponse.addProperty(
          "message",
          "OK add new record in MyTable with name: " + newRec.getName());
      jsonResponse.addProperty("id", newRec.getId());
      return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
    } catch (JsonSyntaxException ex) {
      LOG.severe(ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end addNew

```

## 5.2 REST method for /update record: 

For transactions add method adnotation: @Transactional and the corresponding import:
    - import org.springframework.transaction.annotation.Transactional
[[https://spring.io/guides/gs/managing-transactions/]]

``/*
  curlj http://localhost:8060/rest-jpa/update -d '{"id":"81594578-1abd-4c1c-ae31-8f648ad69bad","name":"n7","dataList":"[71,72,77]"}'
  */
  @PostMapping(value = "/update", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> update(@RequestBody String request) {
    try {
      JsonObject jsonRequest = gson.fromJson(request, JsonObject.class);
      if (!jsonRequest.has("id")) {
        return new ResponseEntity("id tag not found in json request", HttpStatus.BAD_REQUEST);
      }
      String id = jsonRequest.get("id").getAsString();
      MyTableModel myRec = tblRepository.findById(id).orElse(null);
      if (myRec == null) {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("responseCode", -1);
        jsonResponse.addProperty("message", "Not found a record with id: " + id);
        return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
      }
      myRec.update(jsonRequest);
      tblRepository.save(myRec);
      JsonObject jsonResponse = new JsonObject();
      jsonResponse.addProperty("responseCode", 0);
      jsonResponse.addProperty("message", "OK updated record in MyTable for id: " + id);
      return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
    } catch (JsonSyntaxException ex) {
      LOG.severe(ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end update
```


## 5.3. REST method for /delete record:
```java
  /*
  curlj http://localhost:8060/rest-jpa/delete -d  '{"id":"81594578-1abd-4c1c-ae31-8f648ad69bad"}'
  */
  @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> delete(@RequestBody String request) {
    try {
      JsonObject jsonRequest = gson.fromJson(request, JsonObject.class);
      if (!jsonRequest.has("id")) {
        return new ResponseEntity("id tag not found in json request", HttpStatus.BAD_REQUEST);
      }
      String id = jsonRequest.get("id").getAsString();
      MyTableModel myRec = tblRepository.findById(id).orElse(null);
      if (myRec == null) {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("responseCode", -1);
        jsonResponse.addProperty("message", "Not found a record with id: " + id);
        return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
      }
      tblRepository.delete(myRec);
      JsonObject jsonResponse = new JsonObject();
      jsonResponse.addProperty("responseCode", 0);
      jsonResponse.addProperty("message", "OK deleted record in MyTable with id: " + id);
      return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
    } catch (JsonSyntaxException ex) {
      LOG.severe(ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end delete

```

## 5.4. REST API method for query records:

This method will call findFirst10ByName from the repository interface MyTableRepository.

```java
  /*
  curlj http://localhost:8060/rest-jpa/find-name -d  '{"name":"test1"}'
  */
  @PostMapping(value = "/find-name", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> findByName(@RequestBody String request) {
    try {
      JsonObject jsonRequest = gson.fromJson(request, JsonObject.class);
      if (!jsonRequest.has("name")) {
        return new ResponseEntity("name tag not found in json request", HttpStatus.BAD_REQUEST);
      }
      String name = jsonRequest.get("name").getAsString();
      List<MyTableModel> recList = tblRepository.findFirst10ByName(name);
      if (recList == null || recList.isEmpty()) {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("responseCode", -1);
        jsonResponse.addProperty("message", "Not found a record with name: " + name);
        return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
      }
      JsonObject jsonResponse = new JsonObject();
      jsonResponse.addProperty("responseCode", 0);
      jsonResponse.addProperty("message", "OK query records from DbTable1 with name: " + name);
      jsonResponse.add("resultList", genJSONArrayFromList(recList));
      return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
    } catch (JsonSyntaxException ex) {
      LOG.severe(ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end findByName

```

---

# 6. Test JPA

## Local test using curl / curlj:
- in linux or macOS create a new bash alias "curlj" for curl:
    - in centos or macos:
     nano ~/.bash_profile 
    - in debian:
     nano ~/.bashrc 
and add at the end:
```
alias curlj='curl --header "Content-Type: application/json" -w "\n" -v'
```
then save and execute in the same terminal:
```
source ~/.bash_profile
```

* for json pretty print add at the end of command: | json_pp
```
    curlj http://localhost:8060/rest/test2 -d '{"accessToken":1, "request":"test-api"}' | json_pp
``` 
 
- example:
```
  curlj http://localhost:8060/rest-jpa/addNew -d '{}'
  curlj http://localhost:8060/rest-jpa/addNew -d '{"name":"test2", "dataList" : [2,2,2]}'
  curlj http://localhost:8060/rest-jpa/find-name -d  '{"name":"test2"}'  | json_pp

````

## Speed test using a docker container and PostgreSQL:

- change application.properties as:
```
spring.jpa.hibernate.ddl-auto=none
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
spring.datasource.url=jdbc:postgresql://localhost:5432/dbtest?stringtype=unspecified
spring.datasource.username=u4dbtest
spring.datasource.password=pa554DbT
server.port = 8060
```

- in PostgreSQL create a new table with:
```sql
CREATE TABLE MY_TABLE_JPA (
   id varchar(36) NOT NULL, -- UUID PK - length is always 36 chars
   name varchar(100) NOT NULL,
   additional_id NUMERIC,
   data_list varchar(250),
   updates NUMERIC,
   created_at TIMESTAMP,
   last_updated_at TIMESTAMP,
   CONSTRAINT my_table_pk PRIMARY KEY (id));
CREATE INDEX my_table_name_idx ON MY_TABLE_JPA(name, last_updated_at desc);
```

- run mvn install and copy spring-jpa-0.0.1.jar in a new docker container:
```
docker run -it --cpus="2.0" --memory="2G" --memory-swap="2G" -p 8060:8060 --network="host" --name jpa_psql --detach openjdk:8u181-jdk-slim-stretch
docker cp ~/work/api_spring_jpa/target/spring-jpa-0.0.1.jar jpa_psql:/home
```

- open a linux terminal inside the docker container (exit with Ctrl+D or with cmd: exit):
```
docker exec -it jpa_psql bash
apt-get update && apt-get install procps nano mc curl
cd /home/
java -jar spring-jpa-0.0.1.jar > app.log &

```

-----

# 7. Spring Security with JWT
[https://auth0.com/blog/implementing-jwt-authentication-on-spring-boot/]

# 7.1 Create a new REST controller: UserController for /api-server path:

```java
package apiserver.spring_jpa.controllers;

import apiserver.spring_jpa.security.JWTAuthService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** @author */
@RestController
@RequestMapping("/api-server")
public class UserController {

  private static final Logger LOG = Logger.getLogger(UserController.class.getName());
  // Gson instances are Thread-safe so you can reuse them freely across multiple threads:
  private Gson gson;

  // ToDo: add here @Autowired private UserRepository userRepository;
  @PostConstruct
  public void init() {
    gson = new Gson(); // we will pass this instance for all api method
  }

  @ExceptionHandler
  void handleIllegalArgumentException(IllegalArgumentException e, HttpServletResponse response)
      throws IOException {
    response.sendError(HttpStatus.BAD_REQUEST.value());
  }

  // curlj http://localhost:8060/api-server/register -d  '{"username":"n1", "password":"123"}'
  @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> register(@RequestBody String request) {
    try {
      JsonObject jsonRequest = gson.fromJson(request, JsonObject.class);
      // JsonObject jsonRequest = (new JsonParser()).parse(request).getAsJsonObject();
      if (!jsonRequest.has("username") || !jsonRequest.has("password")) {
        return new ResponseEntity("incorrect json request", HttpStatus.BAD_REQUEST);
      }
      // ToDo - register user ...
      JsonObject jsonResponse = new JsonObject();
      jsonResponse.addProperty("responseCode", 0);
      jsonResponse.addProperty(
          "message", "OK register: " + jsonRequest.get("username").getAsString());
      return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
    } catch (JsonSyntaxException ex) {
      LOG.severe(ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end register

  // curlj http://localhost:8060/api-server/login -d  '{"username":"n1", "password":"123"}'
  // Note: the auth JWT is added in the header's reponse 
  @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> login(@RequestBody String request) {
    try {
      JsonObject jsonRequest = gson.fromJson(request, JsonObject.class);
      if (!jsonRequest.has("username") || !jsonRequest.has("password")) {
        return new ResponseEntity("incorrect json request", HttpStatus.BAD_REQUEST);
      }
      String username = jsonRequest.get("username").getAsString();
      // ToDo - login user ...
      String role = "USER";
      JsonObject jsonResponse = new JsonObject();
      jsonResponse.addProperty("responseCode", 0);
      jsonResponse.addProperty("message", "OK login: " + username);
      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.AUTHORIZATION, JWTAuthService.generateAuthentication(username, role));
      return new ResponseEntity(jsonResponse.toString(), headers, HttpStatus.OK);
    } catch (JsonSyntaxException ex) {
      LOG.severe(ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end login

  /*
    curlj http://localhost:8060/api-server/user-details

    curl -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuMSIsInJvbGUiOiJVU0VSIiwiZXhwIjoxNTQyNzUyODE1fQ.rAPrnpRzBNxNbpnLDM3QybNcT4chTH_HEbiM-IgotHq6lnOoa4aZX1eemh0gwn7XTuPDeeI9R1O0IuBJq97Nrw" http://localhost:8060/api-server/user-details -v
  */
  @GetMapping(value = "/user-details", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> getUserDetails() {
    try {
      // ToDo - get user's details from DB ...
      JsonObject jsonResponse = new JsonObject();
      jsonResponse.addProperty("responseCode", 0);
      jsonResponse.addProperty("message", "OK getUserDetails ... ");
      return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
    } catch (JsonSyntaxException ex) {
      LOG.severe(ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end login
}
```
__Note__: this class need to be updated with userRepository methods for create new user and logib.

-----

## 7.2 Add JWTAuthService class

- create a new java package (folder): apiserver.spring_jpa.security
- create SecurityConfig java class as in the example below:

This class will use io.jsonwebtoken lib package for generate and authenticate the JWT:
You will need to update the SECRET_KEY and the EXPIRATIONTIME (now is 5 minutes).

```java
package apiserver.spring_jpa.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** @author */
public class JWTAuthService {

  private static final Logger LOG = Logger.getLogger(JWTAuthService.class.getName());
  static final long EXPIRATIONTIME = 300_000; // 5 minutes

  static final String SECRET_KEY = "my_secret_key_for_JWT";

  static final String TOKEN_PREFIX = "Bearer";
  static final String HEADER_STRING = "Authorization";

  public static void addAuthentication(HttpServletResponse res, String username, String role) {
    res.addHeader(HEADER_STRING, TOKEN_PREFIX + " " + generateAuthentication(username, role));
  }

  public static String generateAuthentication(String username, String role) {

    Claims claims = Jwts.claims().setSubject(username);
    claims.put("role", role);
    String jwt =
        Jwts.builder()
            // .setSubject(username)
            .setClaims(claims)
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
            .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
            .compact();
    return TOKEN_PREFIX + " " + jwt;
  }

  public static Authentication getAuthentication(HttpServletRequest request) {
    // System.out.println("~~~~~ getAuthentication: " + request.getServletPath());
    String token = request.getHeader(HEADER_STRING);
    if (token != null) {
      // parse the token.
      String username = null;
      Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
      try {
        Claims claims =
            Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                .getBody();
        username = claims.getSubject();
        String role = (String) claims.get("role");
        grantedAuthorities.add(new SimpleGrantedAuthority(role));
        System.out.println("~~~~ claims: " + username + " - " + role);
      } catch (ExpiredJwtException ex) {
        request.setAttribute("expired", ex.getMessage());
      } catch (Exception ex) {
        LOG.info("getAuthentication: " + ex.toString());
      }
      return username != null
          ? new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities)
          : null;
    }
    return null;
  }
}
```

-----

## 7.3 Add AuthenticationFilter class:

This class will use JWTAuthService.
```java
package apiserver.spring_jpa.security;

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

/** @author */
public class AuthenticationFilter extends GenericFilterBean {

  private static final Logger LOG = Logger.getLogger(AuthenticationFilter.class.getName());

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    //System.out.println(".... AuthenticationFilter - doFilter ... " + ((HttpServletResponse) response).getStatus());
    Authentication authentication = null;
    if (((HttpServletResponse) response).getStatus() <= 200) {
      authentication = JWTAuthService.getAuthentication((HttpServletRequest) request);
      final String expiredMsg = (String) request.getAttribute("expired");
      if (expiredMsg != null) {
        ((HttpServletResponse) response).addHeader("UNAUTHORIZED", expiredMsg);
        // ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
      }
    }
    if (authentication != null) {
      LOG.info(
          "~~~~~ AuthenticationFilter - authentication "
              + authentication.getName()
              + " - role: "
              + authentication.getAuthorities().toString());
    }
    SecurityContextHolder.getContext().setAuthentication(authentication);
    filterChain.doFilter(request, response);
  }
}

```

-----

## 7.4 Add SecurityConfig class:

```java
package apiserver.spring_jpa.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** @author */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf()
        .disable()
        // make sure we use stateless session; session won't be used to store user's state:
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authorizeRequests()
        // permit all access to /rest-jpa/ for speed test and also for register and login api:
        .antMatchers("/rest-jpa/**", "/api-server/register", "/api-server/login")
        .permitAll()
        // all other api methods must be authenticated:
        .anyRequest()
        .authenticated()
        .and()
        // add filter for all other requests to check the presence of JWT in header:
        .addFilterBefore(new AuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
  }
}
```
We will permit public access to /rest-jpa/ for speed test and for user registration and login.
For all other api methods we will use a filter: AuthenticationFilter for validate JWT token from header's request.

-----

# 8. Test Security

- call /api-server/user-details without JWT:

```
curlj http://localhost:8060/api-server/user-details | json_pp 
```
we will receive:
  < HTTP/1.1 403 
  [...]
  {
   "message" : "Access Denied",
   "timestamp" : "2018-11-21T11:46:02.766+0000",
   "status" : 403,
   "error" : "Forbidden",
   "path" : "/api-server/user-details"
  }
  
- call /api-server/user-details using an expired JWT:
```
curlj -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuMSIsInJvbGUiOiJVU0VSIiwiZXhwIjoxNTQyNzUyODE1fQ.rAPrnpRzBNxNbpnLDM3QybNcT4chTH_HEbiM-IgotHq6lnOoa4aZX1eemh0gwn7XTuPDeeI9R1O0IuBJq97Nrw" http://localhost:8060/api-server/user-details | json_pp
```
we will receive:
  < HTTP/1.1 403 
  < UNAUTHORIZED: JWT expired at 2018-11-21T00:26:55Z. Current time: 2018-11-21T13:49:31Z, a difference of 48156518 milliseconds.  Allowed clock skew: 0 milliseconds.
  [...]
  {
     "status" : 403,
     "error" : "Forbidden",
     "timestamp" : "2018-11-21T11:49:31.539+0000",
     "path" : "/api-server/user-details",
     "message" : "Access Denied"
  }

- call /api-server/login method (the current implementation accept any username/password):
```
 curlj http://localhost:8060/api-server/login -d  '{"username":"n1", "password":"123"}' | json_pp
```
we will receive:
  < HTTP/1.1 200 
  < Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuMSIsInJvbGUiOiJVU0VSIiwiZXhwIjoxNTQyODAxNzg5fQ.aN8Gs2Js065PP1LrzlBSyRhjmD5d3YmgeppywvJmaVS2GjOri0EqbI7BGmuNl93D1y5NRW3p_fiy1GPp5NBesg
  [...]
  {
     "responseCode" : 0,
     "message" : "OK login: n1"
  }

- call /api-server/user-details using a valid JWT (from the above response):
```
curlj -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuMSIsInJvbGUiOiJVU0VSIiwiZXhwIjoxNTQyODAxNzg5fQ.aN8Gs2Js065PP1LrzlBSyRhjmD5d3YmgeppywvJmaVS2GjOri0EqbI7BGmuNl93D1y5NRW3p_fiy1GPp5NBesg" http://localhost:8060/api-server/user-details | json_pp
``` 
we will receive:
  < HTTP/1.1 200 
  [...]
  {
     "responseCode" : 0,
     "message" : "OK getUserDetails ... "
  }

-----


Have fun!
