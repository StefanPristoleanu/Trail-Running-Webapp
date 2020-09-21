
# Start PostgreSQL server in docker

- use this or ./vnc.sa306.sh  for db port forwarding
ssh -L 5901:localhost:5901 -L 5432:localhost:5432 -i ~/.ssh/key_sa306 stefan_ssh@sa306.saturn.fastwebserver.de -p288

- Check active docker containers and start PostgreSQL if it is not running: 

```
  docker ps
  docker container ls --all
  docker container start postgres_srv
```

- for new db connection use:

```
  spring.datasource.url=jdbc:postgresql://localhost:5432/dbtest?stringtype=unspecified
  spring.datasource.username=u4dbtest
  spring.datasource.password=pa554DbT
```

# Create project tables:
The plan for this project is to use the data in the new JSONB format:
```SQL
CREATE TABLE USERS (
    --USER_ID VARCHAR(36) NOT NULL, --type UUID - PK
    USER_ID SERIAL,--an auto incremented field for primary key
    USERNAME VARCHAR(50) NOT NULL UNIQUE, -- user's email address
    PASSWORD VARCHAR(36) NOT NULL, -- PASSWORD: min 8 and max password chars: 24 and it will be encryped one-way MD5 in 32 chars
    NICKNAME VARCHAR(50) NOT NULL,
    USER_ROLE VARCHAR(20) NOT NULL, --default all registration users are with role USER
    REGISTRATED_AT TIMESTAMP NOT NULL,
    LAST_LOGIN_AT TIMESTAMP NOT NULL,
    CONSTRAINT users_pk PRIMARY KEY (USER_ID));

CREATE TABLE TRAILS (
    --an auto incremented field for primary key
    TRAIL_ID SERIAL,
    TRAIL_NAME VARCHAR(50) NOT NULL,
    TRAIL_CREATED_BY INTEGER, -- = USER_ID from TABLE_USERS
    TRAIL_CREATED_AT TIMESTAMP NOT NULL,
    TRAIL_TYPE VARCHAR(20), --bicycle, by foot
    TRAIL_DIFICULTY INTEGER,
    TRAIL_LENGTH INTEGER,
    DATA_JSON JSONB,
    CONSTRAINT trails_pk PRIMARY KEY (TRAIL_ID));
CREATE INDEX TRAILS_GIN_DATA ON TRAILS USING GIN (DATA_JSON); 
```

# client console test tool
`
    alias curlj='curl --header "Content-Type: application/json" -w "\n" -v'
`

  // curlj http://sa306.saturn.fastwebserver.de:9090/api-server/user/login -d  '{"username":"s1", "password":"123"}'

