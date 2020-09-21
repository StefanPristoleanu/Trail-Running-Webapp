
# Spring Boot - REST - JPA - PostgreSQL using JSONB
 ============

- v1.3 - 2018.11.15 - changes:
    - add more info about PostgreSQL case sensitivity
    - add example for content of test record in DB_TABLE_1 table
    - add speed test results
    - refactoring
    
- v1.2 - 2018.11.12 - changes:
    - change id key in json update/delete from long to string in order to have the same data type as for MongoDB
    - change id key in addNew response from "ID" to "id" for consistency of json keys
    - update findByName method to findFirst10ByName

- v1.1 - 2018.11.10 - changes:
    - because of errors with openjdk on debian/ubuntu you will need to add in pom.xml in <build> - <plugins> section useSystemClassLoader: false
    - in pom.xml add plugin for automatic reformats java source code files to comply with Google Java Style
- v1.0 - 2018.11.07 - first version

-----

__Requirements :__
- install Java JDK: [https://openjdk.java.net/]
- install Maven: [https://maven.apache.org/]
- access to PostgreSQL database: [https://www.postgresql.org/] or follow the steps in section 1

__Note :__
- I recommend creating a curl alias: __curlj__ with the command below (can be added to ~ / .bash_profile or ~ / .bashrc):
`
    alias curlj='curl --header "Content-Type: application/json" -w "\n" -v'
`
  - add at the end of the curlj cmd: ` | json_pp ` for formatted display of the json response
  - the -v parameter in curl can be removed if there is no need to display headers for request and response
  
-----


## 1. Install PostgreSQL:

- for CentOS/RedHat:
[https://tecadmin.net/install-postgresql-11-on-centos/]( https://tecadmin.net/install-postgresql-11-on-centos/ )

- for Ubuntu/Debian:
[https://tecadmin.net/install-postgresql-server-on-ubuntu/]( https://tecadmin.net/install-postgresql-server-on-ubuntu/ )

### Install PostgreSQL on Ubuntu 18.04
```bash
sudo apt-get install gnupg2
sudo apt-get install wget ca-certificates
wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -
sudo sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt/ `lsb_release -cs`-pgdg main" >> /etc/apt/sources.list.d/pgdg.list'
sudo apt-get update
sudo apt-get install postgresql postgresql-contrib
sudo su - postgres
psql --version
psql
\conninfo
\quit
exit
```
The commands below can be used to stop, start, enable and check PostgreSQL status:
```
sudo systemctl stop postgresql.service
sudo systemctl start postgresql.service
sudo systemctl enable postgresql.service
sudo systemctl status postgresql.service
```
__Note__: PostgreSQL by default is listening only on the local address 127.0.0.1 and using default port: 5432
You can change the port and the allocated memory in postgresql.conf file:
```
nano /etc/postgresql/11/main/postgresql.conf
systemctl restart postgresql.service
``` 

## 2. Create dbtest database in PostgreSQL:
```bash
sudo su - postgres 
createdb dbtest
psql dbtest
CREATE ROLE u4dbtest WITH SUPERUSER LOGIN PASSWORD 'pa554DbT';
\q
exit
```

### Create table for tests:
The plan for this project is to use the data in the new JSONB format:
```SQL
CREATE TABLE DB_TABLE_1 (
    ID SERIAL,
    DATA_JSON JSONB,
    CONSTRAINT db_table_pk PRIMARY KEY (ID));
CREATE INDEX IDX_GIN_DATA ON DB_TABLE_1 USING GIN (DATA_JSON); 
```
For more info about GIN (Generalized Inverted Index) see: 
[https://bitnine.net/blog-postgresql/postgresql-internals-jsonb-type-and-its-indexes/](https://bitnine.net/blog-postgresql/postgresql-internals-jsonb-type-and-its-indexes/)

Example for test record and dataJson field value:
```JSON
    {
        "id": "10",
        "dataJson": {
            "createdAt": "2018-11-07 15:31:49.233",
            "lastUpdatedAt": "2018-11-07 15:32:43.083",
            "dataList": [1, 2, 3],
            "name": "test_10",
            "counterUpdates": 2
        }
    }
```

```bash
sudo su - postgres 
psql dbtest
CREATE TABLE DB_TABLE_1 (ID SERIAL, DATA_JSON JSONB, CONSTRAINT db_table_pk PRIMARY KEY (ID));
CREATE INDEX IDX_GIN_DATA ON DB_TABLE_1 USING GIN (DATA_JSON); 
\dt
\q
exit
```

__NOTE__: In PostgreSQL keywords and unquoted identifiers are case insensitive and names of identifiers, your table and column names, are converted to lowercase by default. The test table name: DB_TABLE_1 will be converted to db_table_1 in database. 

-----

## 2. Create Spring Boot project

Create a new java maven project SpringBoot using pom.xml generated with:
__Spring Initializr:__
[https://start.spring.io/]( https://start.spring.io/ )

- select "Switch to the full version"
- add for Group: api-server (or any other relevant name)
- Artifact: postgresql_rest (or psql-rest)
- Name: api_spring_postgresql
- and check: Web, Jersey (JAX-RS), JPA, JDBC, PostgreSQL (select the databases you need)
- generate/download the spring project zip and unzip the archive in the working folder
- edit pom.xml and add in <dependencies> section:
    - org.json package for JSONOnject class
    - spring-boot-configuration-processor for IDE recompilation of the project
    
__Note__: for json parsing benchmark see:
[https://github.com/fabienrenaud/java-json-benchmark]

```xml
    <dependency>
	  <groupId>org.json</groupId>
	  <artifactId>json</artifactId>
	  <version>20180813</version>
	  <type>jar</type>
	 </dependency>
	 <dependency>
	  <groupId>org.springframework.boot</groupId>
	  <artifactId>spring-boot-configuration-processor</artifactId>
	  <version>2.0.3.RELEASE</version>
	  <type>jar</type>
	 </dependency>
```

- edit pom.xml and add in <build> - <plugins> section:
    - fmt-maven-plugin for automatic reformats java source code files to comply with Google Java Style
    - useSystemClassLoader: false for maven-surefire-plugin in order to fix some build errors in OpenJDK on ubuntu/debian:
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
```

__Note__: Spring Boot Tutorials:
>
- [https://spring.io/guides/gs/rest-service/]( https://spring.io/guides/gs/rest-service/ )
- [https://spring.io/guides/gs/accessing-data-rest/]( https://spring.io/guides/gs/accessing-data-rest/ )
- [https://spring.io/guides/gs/accessing-data-jpa/]( https://spring.io/guides/gs/accessing-data-jpa/ )
>

Load maven project (pom.xml) in your favorite IDE:
- in NetBeans also add "NB SpringBoot" plugin: nb-springboot
https://github.com/AlexFalappa/nb-springboot

- or in Eclipse Marketplace -> install Spring Tools (is in Popular Tab) and restart Eclipse

-----

## 3. Configure JPA for PostgreSQL database

In the sources folder, create a resource file: src/main/resources/application.properties
For PostgreSQL add your db details in format: 
`
spring.datasource.url=jdbc:postgresql://<server-ip>:<port>/<database_name>
`
For our PostgreSQL configuration add in application.properties file:
```
spring.jpa.hibernate.ddl-auto=none
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
spring.datasource.url=jdbc:postgresql://localhost:5432/dbtest?stringtype=unspecified
spring.datasource.username=u4dbtest
spring.datasource.password=pa554DbT
server.port = 8080
```
__Note__: we need to add jdbc param: "stringtype=unspecified" because we need the database to convert string to the corresponding db format (jsonb in our case).

Now you can start the spring boot project and check the logs.

-----

## 4. Create JPA entity class for storing data in PostgreSQL
[https://spring.io/guides/gs/accessing-data-jpa/]( https://spring.io/guides/gs/accessing-data-jpa/ )

- create a new java package (folder): apiserver.postgresql_rest.entities
- create DbTable1 java class as in the example below:
```java
package apiserver.postgresql_rest.entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import org.json.JSONObject;

@Entity
@Table(name = "DB_TABLE_1")
@XmlRootElement
public class DbTable1 implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    @Basic(optional = false)
    private Long id;
    private String dataJson; // a JSONObject in format:

    protected DbTable1() {
    }
    public DbTable1(JSONObject jsonObj) {
        Date d = new java.sql.Timestamp(System.currentTimeMillis());
        jsonObj.put("createdAt", d);
        jsonObj.put("lastUpdatedAt", d);
        jsonObj.put("counterUpdates", 0);
        dataJson = jsonObj.toString();
    }

    public void update(JSONObject jsonRequest) {
        JSONObject dataJsonObj = new JSONObject(this.dataJson);
        int counter =  dataJsonObj.getInt("counterUpdates");
        dataJsonObj.put("counterUpdates", counter +  1);
        dataJsonObj.put("lastUpdatedAt", new java.sql.Timestamp(System.currentTimeMillis()));
        dataJsonObj.put("name", jsonRequest.get("name"));
        dataJsonObj.put("dataList", jsonRequest.getJSONArray("dataList"));
        dataJson = dataJsonObj.toString();
    }
    public JSONObject toJSONObject() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("id", this.id);
        jsonObj.put("dataJson", new JSONObject(this.dataJson));
        return jsonObj;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getDataJson() {
        return dataJson;
    }
    public void setDataJson(String dataJson) {
        this.dataJson = dataJson;
    }
}
```
__Note__: optional the entity classes can be auto generated in NetBeans via "create new Entity Class from Database connection" or in Eclipse using "create JPA Entities from Tables" option.

-----

## 5. Create a repository interface class which extend CrudRepository

In entities package create a new java interface: DbTable1Repository as in the example below:

```java
package apiserver.postgresql_rest.entities;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface DbTable1Repository extends CrudRepository<DbTable1, Long> {

    // for nativeQuery use table and fields names as in the table create SQL (DDL) and not as in java entity class!
    @Query(value = "SELECT id, data_json FROM DB_TABLE_1 t WHERE t.data_json->>'name' = ?1 limit 10", nativeQuery = true)
    public List<DbTable1> findFirst10ByName(String name);
    
    @Query(value = "SELECT id, data_json FROM DB_TABLE_1 t WHERE CAST (t.data_json->>'counterUpdates' AS INTEGER) >= ?1 limit 10", nativeQuery = true)
    public List<DbTable1> findTop10ByCounterUpdatesGreaterThan(long minCounterUpdates);
}
```
__Note__: for PostgreSQL JSONB operators see:
[https://www.postgresql.org/docs/11/functions-json.html]( https://www.postgresql.org/docs/11/functions-json.html )

-----

## 6. Create a new API REST controller
- create a new java package (folder): apiserver.postgresql_rest.controllers
- create DbTable1Controller java class as in the example below and add @Autowired DbTable1Repository

```java
package apiserver.postgresql_rest.controllers;

import apiserver.postgresql_rest.entities.DbTable1;
import apiserver.postgresql_rest.entities.DbTable1Repository;
import java.util.List;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/rest-postgresql")
public class DbTable1Controller {

    @Autowired
    private DbTable1Repository repository;

//[...]
```
Spring introduced the @Autowired annotation for dependency injection.

### 6.1. REST method for /addNew record:
```java
    // curlj http://localhost:8080/rest-postgresql/addNew -d  '{"name":"n1", "dataList":[1,2,3,4]}'
    @PostMapping(value = "/addNew", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
      public ResponseEntity<?> addNew(@RequestBody String request) {
        try {
          JSONObject jsonRequest = new JSONObject(request);
          if (!jsonRequest.has("name") || !jsonRequest.has("dataList")) {
            return new ResponseEntity("incorrect json request", HttpStatus.BAD_REQUEST);
          }
          DbTable1 newRec = new DbTable1(jsonRequest);
          repository.save(newRec);
          JSONObject jsonResponse = new JSONObject();
          jsonResponse.put("responseCode", 0);
          jsonResponse.put(
              "message", "OK add new record in DbTable1 with name: " + jsonRequest.getString("name"));
          jsonResponse.put("id", newRec.getId() + "");
          // System.out.println("newRec: " + newRec.toString());
          return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
        } catch (JSONException ex) {
          Logger.getLogger(DbTable1Controller.class.getName()).log(Level.SEVERE, null, ex.toString());
          return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
        }
      } // end addNew

```

### 6.2. REST method for /update record:
For transactions add method adnotation: @Transactional and the corresponding import:
    - import org.springframework.transaction.annotation.Transactional
[[https://spring.io/guides/gs/managing-transactions/]]

```java
    // curlj http://localhost:8080/rest-postgresql/update -d  '{"id":"7", "name":"n7", "dataList":[71,72,77]}'
    @PostMapping(value = "/update", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> update(@RequestBody String request) {
        try {
            JSONObject jsonRequest = new JSONObject(request);
            if (!jsonRequest.has("id")) {
                return new ResponseEntity("id tag not found in json request", HttpStatus.BAD_REQUEST);
            }
            Long id = Long.valueOf(jsonRequest.getString("id"));
            DbTable1 myRec = repository.findById(id).orElse(null);
            if (myRec == null) {
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("responseCode", -1);
                jsonResponse.put("message", "Not found a record with id: " + id);
                return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
            }
            myRec.update(jsonRequest);
            repository.save(myRec);
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("responseCode", 0);
            jsonResponse.put("message", "OK updated record in DbTable1 for id: " + id );
            return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
        } catch (JSONException ex) {
            Logger.getLogger(DbTable1Controller.class.getName()).log(Level.SEVERE, null, ex.toString());
            return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
        }
    }// end update
```

### 6.3. REST method for /delete record:
```java
    // curlj http://localhost:8080/rest-postgresql/delete -d  '{"id":"9"}'
    @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> delete(@RequestBody String request) {
        try {
            JSONObject jsonRequest = new JSONObject(request);
            if (!jsonRequest.has("id")) {
                return new ResponseEntity("id tag not found in json request", HttpStatus.BAD_REQUEST);
            }
            Long id = Long.valueOf(jsonRequest.getString("id"));
            DbTable1 myRec = repository.findById(id).orElse(null);
            if (myRec == null) {
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("responseCode", -1);
                jsonResponse.put("message", "Not found a record with id: " + id);
                return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
            }
            repository.delete(myRec);
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("responseCode", 0);
            jsonResponse.put("message", "OK deleted record in DbTable1 with id: " + id );
            return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
        } catch (JSONException ex) {
            Logger.getLogger(DbTable1Controller.class.getName()).log(Level.SEVERE, null, ex.toString());
            return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
        }
    }// end delete
```

### 6.4. REST method for /find-name method:
This method use NativeQuery in DbTable1Repository interface.
```java
    // curlj http://localhost:8080/rest-postgresql/find-name -d  '{"name":"n2"}'
    @PostMapping(value = "/find-name", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> findByName(@RequestBody String request) {
        try {
            JSONObject jsonRequest = new JSONObject(request);
            if (!jsonRequest.has("name")) {
                return new ResponseEntity("name tag not found in json request", HttpStatus.BAD_REQUEST);
            }
            String name = jsonRequest.getString("name");
            List<DbTable1> recList = repository.findFirst10ByName(name);
            if (recList == null || recList.isEmpty()) {
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("responseCode", -1);
                jsonResponse.put("message", "Not found a record with name: " + name);
                return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
            }
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("responseCode", 0);
            jsonResponse.put("message", "OK query records from DbTable1 with name: " + name );
            jsonResponse.put("resultList", genJSONArrayFromList(recList));
            return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
        } catch (JSONException ex) {
            Logger.getLogger(DbTable1Controller.class.getName()).log(Level.SEVERE, null, ex.toString());
            return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
        }
    }// end findByName
  
    private JSONArray genJSONArrayFromList(List<DbTable1> recList){
      JSONArray jsonArray = new JSONArray();
      recList.forEach((rec) -> {
          jsonArray.put(rec.toJSONObject());
        });
      return jsonArray;
    }
```

### 6.5. REST method for custom query method:
```java
    // curlj http://localhost:8080/rest-postgresql/query -d  '{"minCounterUpdates": 2}'
    @PostMapping(value = "/query", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> query(@RequestBody String request) {
        try {
            JSONObject jsonRequest = new JSONObject(request);
            if (!jsonRequest.has("minCounterUpdates")) {
                return new ResponseEntity("minCounterUpdates tag not found in json request", HttpStatus.BAD_REQUEST);
            }
            long minCounterUpdates = jsonRequest.getLong("minCounterUpdates");
            List<DbTable1> recList = repository.findTop10ByCounterUpdatesGreaterThan(minCounterUpdates);
            if (recList == null || recList.isEmpty()) {
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("responseCode", -1);
                jsonResponse.put("message", "Not found a record with counterUpdates > " + minCounterUpdates);
                return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
            }
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("responseCode", 0);
            jsonResponse.put("message", "OK query records from DbTable1 with counterUpdates > " + minCounterUpdates );
            jsonResponse.put("resultList", genJSONArrayFromList(recList));
            return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
        } catch (JSONException ex) {
            Logger.getLogger(DbTable1Controller.class.getName()).log(Level.SEVERE, null, ex.toString());
            return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
        }
    }// end query
```

-----

## 7. Testing

### 7.1 test REST API methods:

In linux and macOS create a new bash alias "curlj" for curl:
- in centos or macos:
     nano ~/.bash_profile  
- in ubuntu/debian:
     nano ~/.bashrc 
and add at the end of file:
```
alias curlj='curl --header "Content-Type: application/json" -w "\n" -v'
```
then save and execute in the same terminal:
    source ~/.bash_profile 
or
    source ~/.bashrc 

For json pretty print add at the end of command: | json_pp
 
* /rest-postgresql/addNew : 
```bash
curlj http://localhost:8080/rest-postgresql/addNew -d  '{"name":"n1", "dataList":[1,2,3]}'
```
>> {"id":14,"message":"OK add new record in DbTable1 for name: n1","responseCode":0}

* /rest-postgresql/update :
```
curlj http://localhost:8080/rest-postgresql/update -d  '{"id":"14", "name":"n1", "dataList":[10,20,30]}'
```
>> {"message":"OK updated record in DbTable1 for id: 14","responseCode":0}

* /rest-postgresql/delete :
```
curlj http://localhost:8080/rest-postgresql/delete -d  '{"id":"14"}'
```
>> {"message":"OK deleted record in DbTable1 with id: 14","responseCode":0}

* add more new records and updates: 
```bash
curlj http://localhost:8080/rest-postgresql/addNew -d  '{"name":"n10", "dataList":[1,2,3]}'

curlj http://localhost:8080/rest-postgresql/addNew -d  '{"name":"n11", "dataList":[11,12,13]}'

curlj http://localhost:8080/rest-postgresql/addNew -d  '{"name":"n12", "dataList":[12,2,3,4,5,6]}'

curlj http://localhost:8080/rest-postgresql/update -d  '{"id":"15", "name":"n10", "dataList":[10,20,30]}'
```

* /rest-postgresql/find-name :
```
curlj http://localhost:8080/rest-postgresql/find-name -d  '{"name":"n11"}'
```
>> {"message":"OK query records from DbTable1 with name: n11","resultList":[{"id":"16","dataJson":{"createdAt":"2018-11-07 18:22:54.888","lastUpdatedAt":"2018-11-07 18:22:54.888","dataList":[11,12,13],"name":"n11","counterUpdates":0}}],"responseCode":0}

* /rest-postgresql/query :
```
curlj http://localhost:8080/rest-postgresql/query -d  '{"minCounterUpdates": 2}'
```
>> {"message":"OK query records from DbTable1 with counterUpdates > 2","resultList":[{"id":"10","dataJson":{"createdAt":"2018-11-07 15:31:49.233","lastUpdatedAt":"2018-11-07 15:32:43.083","dataList":[71,72,77],"name":"n7","counterUpdates":2}}],"responseCode":0}


### 7.2 Check PostgreSQL:
```bash
sudo su - postgres 
psql dbtest
select * from db_table_1;
select data_json->>'dataList' as dataList from db_table_1 where data_json->>'name'='n7';
```

### 7.3 Speed Test Results for Spring Boot + PostgreSQL:

    API Server Speed Test Stats: 
    api_server_address: http://sa306.saturn.fastwebserver.de:8080/rest-postgresql
    Total test requests:    20 * 5000 = 100000
    Total OK API responses: 100000
    Total Errors responses: 0
    Total execution time: 75.954 sec 
    Total requests per second: 1316.59


------

Have fun!
