 CREATE TABLE USERS (
    USER_ID SERIAL,--an auto incremented field for primary key
    USERNAME VARCHAR(50) NOT NULL UNIQUE, -- user's email address
    PASSWORD VARCHAR(64) NOT NULL, -- PASSWORD: min 8 and max password chars: 24 and it will be encryped one-way SHA in 64 chars
    NICKNAME VARCHAR(50) NOT NULL,
    USER_ROLE VARCHAR(20) NOT NULL, --default all registration users are with role USER
    REGISTRATED_AT TIMESTAMP NOT NULL,
    LAST_LOGIN_AT TIMESTAMP NOT NULL,
    LIKED_TRAILS JSON,  --Using JSON instead of JSONB in order to keep the chronological order of likes
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
     TRAIL_DESCRIPTION VARCHAR(200),
     TRAIL_NO_OF_LIKES INTEGER,
     TRAIL_INCIDENTS INTEGER,
     DELTA_ELEVATION DOUBLE PRECISION,
     TRAIL_SLOPE DOUBLE PRECISION,
     DATA_JSON JSONB,
     CONSTRAINT trails_pk PRIMARY KEY (TRAIL_ID));
 CREATE INDEX TRAILS_GIN_DATA ON TRAILS USING GIN (DATA_JSON);
 
 ALTER TABLE trails 
 ADD CONSTRAINT owner_id_fk FOREIGN KEY (trail_created_by) REFERENCES users (user_id);
 
