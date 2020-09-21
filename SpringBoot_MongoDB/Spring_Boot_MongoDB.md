
# Spring Boot - REST - JPA - MongoDB 
=====
- v1.2 - 2018.11.12 - changes:
    - change id key in addNew response from "ID" to "id" for consistency of json keys
    - update findByName method to findFirst10ByName

- v1.1 - 2018.11.10 - changes:
    - because of errors in docker comment/remove from pom.xml the dependencies for:
        * spring-boot-starter-data-jpa
        * spring-boot-starter-jdbc
    - because of errors with openjdk on debian/ubuntu you will need to add in pom.xml in build - plugins section:
    ```xml
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <configuration>
            <useSystemClassLoader>false</useSystemClassLoader>
            </configuration>
        </plugin>
    ```
    - for automatic reformats java source code files to comply with Google Java Style add in pom.xml in build - plugins section:
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
    ``` 
- v1.0 - 2018.11.07 - first version
-----

## 1. Install MongoDB:

- for CentOS/RedHat:
[https://docs.mongodb.com/manual/tutorial/install-mongodb-on-red-hat/]( https://docs.mongodb.com/manual/tutorial/install-mongodb-on-red-hat/ )

- for Ubuntu/Debian:
[https://www.digitalocean.com/community/tutorials/how-to-install-mongodb-on-ubuntu-18-04]( https://www.digitalocean.com/community/tutorials/how-to-install-mongodb-on-ubuntu-18-04 )

### Install MongoDB on Ubuntu 18.04

- installing MongoDB:
```
sudo apt update
sudo apt install mongodb
```

- checking the Service and Database:
`
sudo systemctl status mongodb
mongo --eval 'db.runCommand({ connectionStatus: 1 })'
`

- managing the MongoDB Service:
`
sudo systemctl stop mongodb
sudo systemctl start mongodb
sudo systemctl status mongodb
`
__Note__: MongoDB by default is listening only on the local address 127.0.0.1 and using default port: 27017 and is configured in /etc/mongod.conf

- create database: dbtest and a database user for API Server access: dbuser:

```bash
mongo
>  s
> use dbtest
> db.createUser(
  {
   user: "dbuser",
   pwd: "Pa551234",
   roles: [ "readWrite"]
  })
> exit
```

- test dbuser access:
```bash
mongo -u dbuser -p Pa551234 dbtest
> db
> show collections
> help
> exit
```
- for MongoDB Free Monitoring at https://cloud.mongodb.com/freemonitoring/cluster/<...> run cmds:
```bash
mongo
> db.enableFreeMonitoring()
```
and copy the URL link for cloud.mongodb.com/freemonitoring/...
To permanently disable this reminder, run the following command: db.disableFreeMonitoring()

- MongoDB database client:
[https://www.mongodb.com/download-center/compass]( https://www.mongodb.com/download-center/compass )

-----

## 2. Create Spring Boot project

Create a new java maven project SpringBoot using pom.xml generated with:
__Spring Initializr:__
[https://start.spring.io/]( https://start.spring.io/ )

- select "Switch to the full version"
- add for Group: api-server (or any other relevant name)
- Artifact: mongodb_rest (or ...)
- and check: Web, Jersey (JAX-RS), JPA, JDBC and from <NoSQL>: MongoDB and Embedded MongoDB (if you want to use this for unit tests)
- download the spring project zip and unzip the archive in the working folder
- edit pom.xml and add in dependencies tab:
    - we will need: org.json package for JSONOnject class
    - we will need: pring-boot-configuration-processor for IDE recompilation of the project
    - __Note__: because we can have errors when running the application in docker container is better to comment in pom.xml the dependencies (or try to not select this options in https://start.spring.io !): 
            spring-boot-starter-data-jpa
            spring-boot-starter-jdbc
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
- open a terminal in the project folder:
```
    mvn install
```

__Note__: Spring Boot Tutorials:
>
- [https://spring.io/guides/gs/rest-service/]( https://spring.io/guides/gs/rest-service/ )
- [https://spring.io/guides/gs/accessing-data-rest/]( https://spring.io/guides/gs/accessing-data-rest/ )
- [https://spring.io/guides/gs/accessing-data-jpa/]( https://spring.io/guides/gs/accessing-data-jpa/ )
- [https://spring.io/guides/gs/accessing-data-mongodb/]( https://spring.io/guides/gs/accessing-data-mongodb/ )
- [https://spring.io/guides/gs/accessing-mongodb-data-rest/]( https://spring.io/guides/gs/accessing-mongodb-data-rest/ )
- [https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/]( https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/)
>

Load maven project (pom.xml) in your favorite IDE:
- in NetBeans also add "NB SpringBoot" plugin: nb-springboot
https://github.com/AlexFalappa/nb-springboot

- or in Eclipse Marketplace -> install Spring Tools (is in Popular Tab) and restart Eclipse


## 3. JPA for MongoDB database

In the sources folder, create a resource file: src/main/resources/application.properties
For MongoDB add your db details in format: 
`
spring.data.mongodb.uri=mongodb://<user>:<pass>@<server-ip>:<port>/<database_name>
`

For our MongoDB configuration add in application.properties file:
```
spring.data.mongodb.uri=mongodb://dbuser:Pa551234@localhost:27017/dbtest
server.port = 8090
```

Now you can start the spring boot project and check the logs.


## 4. Create JPA entity class for storing data in MongoDB
[https://spring.io/guides/gs/accessing-data-jpa/]( https://spring.io/guides/gs/accessing-data-jpa/ )

- create a new java package (folder): apiserver.mongodb_rest.entities
- create DbTable1 java class as in the example below

__Note__: for simplicity we will name the class "DbTable1" although in MongoDB we work with collections.

```java
package apiserver.mongodb_rest.entities;

import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.springframework.data.annotation.Id;

public class DbTable1 {

    @Id
    private String id; // UUID automatically updated by MongoDB
    private String name;
    private Date createdAt;
    private Date lastUpdatedAt;
    private long counterUpdates;
    private List<Object> dataList; // a JSONArray with any data

    protected DbTable1() {
    }

    public DbTable1(String name, JSONArray jsonArray) {
        this.name = name;
        this.createdAt = new java.sql.Timestamp(System.currentTimeMillis());
        this.lastUpdatedAt = this.createdAt;
        this.counterUpdates = 0;
        this.dataList = jsonArray.toList();
    }

    public void update(String name, JSONArray jsonArray) {
        this.name = name;
        this.lastUpdatedAt = new java.sql.Timestamp(System.currentTimeMillis());
        this.counterUpdates += 1;
        this.dataList = jsonArray.toList();
    }
// [...]
```
- add in the above java class get and set methods for all fields


## 5. Create a repository interface class which extend CrudRepository

In entities package create a new java class: DbTable1Repository as in the example below:

```java
package apiserver.mongodb_rest.entities;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DbTable1Repository extends MongoRepository<DbTable1, String> {

    // https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/
    public List<DbTable1> findFirst10ByName(String name);
    
    public List<DbTable1> findTop10ByCounterUpdatesGreaterThan(long minCounterUpdates);   
}
```
__Note__: For more info about Spring Data MongoDB check:
[https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/]( https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/ )


## 6. Create a new REST controller
- create a new java package (folder): apiserver.mongodb_rest.controllers
- create DbTable1Controller java class as in the example below

```java
package apiserver.mongodb_rest.controllers;

import apiserver.mongodb_rest.entities.DbTable1;
import apiserver.mongodb_rest.entities.DbTable1Repository;
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
@RequestMapping("/rest-mongodb")
public class DbTable1Controller {

    @Autowired
    private DbTable1Repository repository;

//[...]
```
Spring introduced the @Autowired annotation for dependency injection.

### 6.1. REST method for /addNew record (document in MongoDB):
```java
    // curlj http://localhost:8090/rest-mongodb/addNew -d  '{"name":"n1", "dataList":[1,2,3,4]}'
    @PostMapping(value = "/addNew", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> addNew(@RequestBody String request) {
        try {
            JSONObject jsonRequest = new JSONObject(request);
            DbTable1 newRec = new DbTable1(
                    jsonRequest.getString("name"),
                    jsonRequest.getJSONArray("dataList"));
            repository.save(newRec);
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("responseCode", 0);
            jsonResponse.put("message", "OK add new record in DbTable1 for name: " + newRec.getName());
            jsonResponse.put("id", newRec.getId());
            return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
        } catch (JSONException ex) {
            Logger.getLogger(DbTable1Controller.class.getName()).log(Level.SEVERE, null, ex.toString());
            return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
        }
    }// end addNew
```

### 6.2. REST method for /update record:
```java
    // curlj http://localhost:8090/rest-mongodb/update -d  '{"id":"5be19c85873c3073650ba312", "name":"n1", "dataList":[1,2,3,4]}'
    @PostMapping(value = "/update", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> update(@RequestBody String request) {
        try {
            JSONObject jsonRequest = new JSONObject(request);
            if (!jsonRequest.has("id")) {
                return new ResponseEntity("id tag not found in json request", HttpStatus.BAD_REQUEST);
            }
            String id = jsonRequest.getString("id");
            DbTable1 myRec = repository.findById(id).orElse(null);
            if (myRec == null) {
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("responseCode", -1);
                jsonResponse.put("message", "Not found a record with id: " + id);
                return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
            }
            myRec.update(jsonRequest.getString("name"), jsonRequest.getJSONArray("dataList"));
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
    // curlj http://localhost:8090/rest-mongodb/delete -d  '{"id":"5be1976a873c30733c9da283"}'
    @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> delete(@RequestBody String request) {
        try {
            JSONObject jsonRequest = new JSONObject(request);
            if (!jsonRequest.has("id")) {
                return new ResponseEntity("id tag not found in json request", HttpStatus.BAD_REQUEST);
            }
            String id = jsonRequest.getString("id");
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
This is a simple call for findByName method from DbTable1Repository interface.
You can add in this interface new methods to search for any field or combination of fields. 

```java
    // curlj http://localhost:8090/rest-mongodb/find-name -d  '{"name":"n1"}'
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
            jsonResponse.put("resultList", new JSONArray(recList));
            return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
        } catch (JSONException ex) {
            Logger.getLogger(DbTable1Controller.class.getName()).log(Level.SEVERE, null, ex.toString());
            return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
        }
    }// end findByName
```

### 6.5. REST method for custom query method:
__Note__: for more details ckeck:
[https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/]( https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/ )

```java
    // curlj http://localhost:8090/rest-mongodb/query -d  '{"minCounterUpdates": 2}'
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
            jsonResponse.put("resultList", new JSONArray(recList));
            return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
        } catch (JSONException ex) {
            Logger.getLogger(DbTable1Controller.class.getName()).log(Level.SEVERE, null, ex.toString());
            return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
        }
    }// end query
```

## 7. Testing

### 7.1 test REST API methods:

In linux and macOS create a new bash alias "curlj" for curl:
- in centos or macos:
    ` nano ~/.bash_profile ` 
- in ubuntu/debian:
    ` nano ~/.bashrc `
and add at the end of file:
```
alias curlj='curl --header "Content-Type: application/json" -w "\n" -v'
```
then save and execute in the same terminal:
` source ~/.bash_profile `

For json pretty print add at the end of command: | json_pp
 
* /rest-mongodb/addNew : 
```bash
curlj http://localhost:8090/rest-mongodb/addNew -d  '{"name":"n1", "dataList":[1,2,3]}'
```
>> {"id":"5be2016b873c30759b59468f","message":"OK add new record in DbTable1 for name: n1","responseCode":0}

* /rest-mongodb/update :
```
curlj http://localhost:8090/rest-mongodb/update -d  '{"id":"5be2016b873c30759b59468f", "name":"n1", "dataList":[10,20,30]}'
```
>> {"message":"OK updated record in DbTable1 for id: 5be2016b873c30759b59468f","responseCode":0}

* /rest-mongodb/delete :
```
curlj http://localhost:8090/rest-mongodb/delete -d  '{"id":"5be2016b873c30759b59468f"}'
```
>> {"message":"OK deleted record in DbTable1 with id: 5be2016b873c30759b59468f","responseCode":0}

* add more new records and updates: 
```bash
curlj http://localhost:8090/rest-mongodb/addNew -d  '{"name":"n10", "dataList":[1,2,3]}'

curlj http://localhost:8090/rest-mongodb/addNew -d  '{"name":"n11", "dataList":[11,12,13]}'

curlj http://localhost:8090/rest-mongodb/addNew -d  '{"name":"n12", "dataList":[12,2,3,4,5,6]}'

curlj http://localhost:8090/rest-mongodb/update -d  '{"id":"5be2036e873c30759b594690", "name":"n10", "dataList":[10,20,30]}'
```

* /rest-mongodb/find-name :
```
curlj http://localhost:8090/rest-mongodb/find-name -d  '{"name":"n11"}'
```
>> {"message":"OK query records from DbTable1 with name: n11","resultList":[{"createdAt":"Tue Nov 06 23:11:24 EET 2018","lastUpdate":"Tue Nov 06 23:11:24 EET 2018","dataList":[11,12,13],"name":"n11","id":"5be2037c873c30759b594691","counterUpdates":0}],"responseCode":0}

* /rest-mongodb/query :
```
curlj http://localhost:8090/rest-mongodb/query -d  '{"minCounterUpdates": 2}'
```
>> {"message":"OK query records from DbTable1 with counterUpdates > 2","resultList":[{"createdAt":"Tue Nov 06 23:11:10 EET 2018","lastUpdate":"Tue Nov 06 23:15:32 EET 2018","dataList":[10,20,30,41],"name":"n10","id":"5be2036e873c30759b594690","counterUpdates":3}],"responseCode":0}


### 7.2 Check MongoDB
```bash
mongo -u dbuser -p Pa551234 dbtest
> show collections
> db.dbTable1.find()
> db.dbTable1.find({name:"n1"})
```

--------------------------------------------------------------------------
