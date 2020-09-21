# Speed Test
 ==========
 
# 1. Speed Test for insert + update using 2CPU app containers:

## Java Spring Boot + PostgreSQL JSONB:
>
    API Server Speed Test Stats:
    api_server_address: http://sa306.saturn.fastwebserver.de:8080/rest-postgresql
    Total test requests:    20 * 5000 = 100000
    Total OK API responses: 100000
    Total Errors responses: 0
    Total execution time: 73.350 sec
    Total requests per second: 1363.33
>

## Java Spring Boot + MongoDB:
>
    API Server Speed Test Stats:
    api_server_address: http://sa306.saturn.fastwebserver.de:8090/rest-mongodb
    Total test requests:    20 * 5000 = 100000
    Total OK API responses: 100000
    Total Errors responses: 0
    Total execution time: 62.436 sec
    Total requests per second: 1601.64
>

##  Node.js + PostgreSQL JSONB:
>
    API Server Speed Test Stats: 
    api_server_address: http://sa306.saturn.fastwebserver.de:3080
    Total test requests:    20 * 5000 = 100000
    Total OK API responses: 100000
    Total Errors responses: 0
    Total execution time: 66.523 sec 
    Total requests per second: 1503.24
>

## Total result for speed tests (more is better) :

|                  | PostgreSQL | MongoDB |
| ---------------- | ---------- |---------|
| Java Spring Boot |    1363    |   1601  |
| Node.js          |    1503    |     -   |


__Note__: using ` docker stats ` command notice that the java applications use all 200% of CPU so, in order to obtain better results we need to alocate more CPU to java app containers


# 2. New Tests

- update (copy) latest deployment app to containers:

docker container cp target/postgresql_rest-0.0.1.jar java_psql:/home

- restart api server by connecting to container (you will need to close the previous api server):

docker exec -it java_psql bash
java -jar /home/postgresql_rest-0.0.1.jar > /home/app.log &


# Tests Session V2:

## Speed test for SpringBoot PostgreSQL
java -jar target/speed_test-0.0.1.jar http://sa306.saturn.fastwebserver.de:8080/rest-postgresql 20 5000

API Server Speed Test Stats: 
api_server_address: http://sa306.saturn.fastwebserver.de:8080/rest-postgresql
Total test requests:    20 * 5000 = 100000
Total OK API responses: 100000
Total Errors responses: 0
Total execution time: 44.878 sec 
Total requests per second: 2228.26

## Speed test for SpringBoot MongoDB
java -jar target/speed_test-0.0.1.jar http://sa306.saturn.fastwebserver.de:8090/rest-mongodb 20 5000

API Server Speed Test Stats: 
api_server_address: http://sa306.saturn.fastwebserver.de:8090/rest-mongodb
Total test requests:    20 * 5000 = 100000
Total OK API responses: 100000
Total Errors responses: 0
Total execution time: 24.725 sec 
Total requests per second: 4044.49




## Speed test for SpringBoot PostgreSQL v2

api_server_address: http://sa306.saturn.fastwebserver.de:8080/rest-postgresql
Total test requests:    20 * 5000 = 100000
Total OK API responses: 100000
Total Errors responses: 0
Total execution time: 66.165 sec
Total requests per second: 1511.37

Total test requests:    25 * 10000 = 250000
Total OK API responses: 250000
Total Errors responses: 0
Total execution time: 183.279 sec
Total requests per second: 1364.04


## Speed test for SpringBoot MongoDB v2

docker exec -it mongo_srv mongo
show dbs
use dbtest
show collections
db.dbTable1.stats()
db.dbTable1.drop()
db.createCollection("dbTable1")
db.dbTable1.createIndex( { trailCreatedBy: 1, lastUpdatedAt: -1 } )
db.dbTable1.getIndexes()

Total test requests:    20 * 5000 = 100000
Total OK API responses: 100000
Total Errors responses: 0
Total execution time: 72.096 sec
Total requests per second: 1387.04


Total test requests:    25 * 10000 = 250000
Total OK API responses: 250000
Total Errors responses: 0
Total execution time: 439.494 sec
Total requests per second: 568.84

## Speed test for NodeJs PostgreSQL: java -jar target/speed_test-0.0.1.jar http://sa306.saturn.fastwebserver.de:3080 20 5000

API Server Speed Test Stats: 
api_server_address: http://sa306.saturn.fastwebserver.de:3080
Total test requests:    20 * 5000 = 100000
Total OK API responses: 0
Total Errors responses: 100000
Total execution time: 43.967 sec
Total requests per second: 2274.43


