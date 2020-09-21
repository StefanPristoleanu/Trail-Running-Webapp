
# Node.js - REST - PostgreSQL using JSONB
 ============
 

- v1.0 - 2018.11.15 - first version

-----
__Requirements__:
- install Node.js: [https://nodejs.org/en/]
- access to PostgreSQL database: [https://www.postgresql.org/]

-----

[https://eslint.org/docs/user-guide/getting-started]
[https://expressjs.com/]
[https://www.npmjs.com/package/body-parser]
[http://momentjs.com/docs/]
[https://github.com/jprichardson/node-fs-extra]
[https://node-postgres.com/]
 
```bash
mkdir api_nodejs_postgresql
cd api_nodejs_postgresql
```

- use npm init tp create package.json and for "entry point" type: server.js
```
npm init
npm install --save express body-parser moment fs-extra pg
```

- I recommend installing Nodemon as a dev dependency. It’s a simple little package that automatically restarts your server when files change:
```
npm install eslint nodemon --save-dev 
```
- then add/update the package.json - scripts section with: "dev": "nodemon server.js"
- the package.json file:
```JSON
 {
  "name": "api_nodejs_postgresql",
  "version": "0.0.1",
  "description": "Test API project for Node.js and PostgreSQL",
  "main": "app/server.js",
  "scripts": {
    "test": "echo \"Error: no test specified\" && exit 1",
    "start": "node app/server.js",
    "dev": "nodemon app/server.js"
  },
  "author": "",
  "license": "ISC",
  "dependencies": {
    "body-parser": "^1.18.3",
    "express": "^4.16.4",
    "fs-extra": "^7.0.1",
    "moment": "^2.22.2",
    "pg": "^7.6.1"
  },
  "devDependencies": {
    "eslint": "^5.9.0",
    "nodemon": "^1.18.6"
  }
}
```

- create a new folder app and here create a file: server.js with content:
```JS
"use strict";

const express = require('express');
const bodyParser = require('body-parser');
const config = require('./common/ConfigSettings.js');
const MyController = require('./controllers/DbTable1Controller.js');

const app = express();
app.use(bodyParser.json());

// API routes configuration:
app.get('/', (req, res) => {
    res.send('API Node.js Server is running!')
});
app.post('/addNew', [
    MyController.addNew
]);

app.post('/update', [
    MyController.update
]);

// start API server using config.port
app.listen(config.port, function () {
    console.log('API Node.js Server listening at port %s', config.port);
});
```

- create a new folder app/common and a new file: app/common/ConfigSettings.js with content:
```JS
module.exports = {
    "port": 3080,
    "environment": "dev",
};
```

- create a new file app/common/Database.js with content:
```JS
var pg = require('pg')
const dbConfig = {
    host: 'localhost', // 
    port: 5432,
    database: 'dbtest', // name of the database
    user: 'u4dbtest', // name of the user account
    password: 'pa554DbT',
    max: 50, // max number of clients in the pool
    idleTimeoutMillis: 30000
}
var pool = new pg.Pool(dbConfig);
module.exports = {
    "connMgr": pool,
};
```

- create a new folder app/entities for table's models and a new file: app/entities/DbTable1.js with content:
```JS
"use strict";
var moment = require('moment');
class DbTable1 {
    // we add ' RETURNING id' at the end of the sql for insert so that we can get the 'id' for auto-incremented PK
    generateInsert(data_json) {
        if (data_json === undefined || data_json === null || data_json.name === undefined) {
            return '';
        }
        data_json.createdAt = moment(new Date()).format("YYYY-MM-DD HH:mm:ss");
        data_json.lastUpdatedAt = data_json.createdAt;
        data_json.counterUpdates = 0;
        let sql = "INSERT INTO db_table_1 (data_json) VALUES('" + JSON.stringify(data_json) +
        "')  RETURNING id";
        return sql;
    };

    generateFinByPK(jsonRequest){
        if (jsonRequest === undefined || jsonRequest === null || jsonRequest.id === undefined) {
            return '';
        }
        let sql = "SELECT data_json from db_table_1 where id=" + parseInt(jsonRequest.id);
        return sql;
    }

    generateUpdate(data_json, jsonRequest) {
        if (jsonRequest === undefined || jsonRequest === null || jsonRequest.id === undefined) {
            return '';
        }
        if(jsonRequest.name !== undefined){
            data_json.name = jsonRequest.name;
        }
        if(jsonRequest.dataList !== undefined){
            data_json.dataList = jsonRequest.dataList;
        }
        data_json.lastUpdatedAt = moment(new Date()).format("YYYY-MM-DD HH:mm:ss");
        data_json.counterUpdates += 1;
        let sql = "UPDATE db_table_1 SET data_json='" + JSON.stringify(data_json) +
        "' WHERE id=" + parseInt(jsonRequest.id);
        return sql;
    };
}
const dbTable1 = new DbTable1();
module.exports = dbTable1;
```

- create a new folder app/controllers and a new file: app/controllers/DbTable1Controller.js with content:
```JS
const tbl = require('../entities/DbTable1.js');
const db = require('../common/Database.js');
// curlj http://localhost:3080/addNew -d  '{"name":"nodejs_10", "dataList":[1,2,3,4]}'
module.exports.addNew = (req, res) => {
    let sql = tbl.generateInsert(req.body);
    if (sql === '') {
        res.status(400).send('Bad Request');
        return;
    }
    let jsonResponse = {};
    (async () => {
        const dbConn = await db.connMgr.connect()
        try {
            const dbResponse = await dbConn.query(sql);
            if (dbResponse.rowCount > 0) {
                jsonResponse = {
                    responseCode: 0,
                    message: 'OK add new record in DbTable1 with name: ' + req.body.name,
                    id: dbResponse.rows[0].id + ''
                };
            }
        } finally {
            res.status(200).send(jsonResponse);
            dbConn.release();
        }
    })().catch(e => {
        console.log(e.stack)
        jsonResponse = {error: e.stack}
    });
};

// curlj http://localhost:3080/update -d  '{"id":"7", "name":"nodejs_7", "dataList":[71,72,77]}'
module.exports.update = (req, res) => {
    const sql = tbl.generateFinByPK(req.body);
    if (sql === '') {
        res.status(400).send('id tag not found in json request');
        return;
    }
    let jsonResponse = {};
    (async () => {
        const dbConn = await db.connMgr.connect()
        try {
            let dbResponse = await dbConn.query(sql);
            if (dbResponse.rowCount > 0) {
                const data_json = dbResponse.rows[0].data_json;
                dbResponse = await dbConn.query(tbl.generateUpdate(data_json, req.body));
                if (dbResponse.rowCount > 0) {
                    jsonResponse = {
                        responseCode: 0,
                        message: 'OK updated record in DbTable1 for id: ' + req.body.id,
                    }
                }
            } else {
                jsonResponse = {
                    responseCode: -1,
                    message: 'Not found a record with id: ' + req.body.id,
                }
            }
        } finally {
            res.status(200).send(jsonResponse);
            dbConn.release();
        }
    })().catch(e => {
        console.log(e.stack)
        jsonResponse = { error: e.stack }
    });
};
```
- for postgresql transactions see:
[https://node-postgres.com/features/transactions]

- for development and hot deployment (using nodemon) start with:
```
npm run dev
```
or simply execute: npm start

- open a browser at:  http://localhost:3080/

-----

# Test in docker container

## Install Node.js app for PostgreSQL in docker container:
```
docker pull node:10-slim
docker image ls -a

docker run -it --cpus="2.0" --memory="2G" --memory-swap="2G" -p 3080:3080 --network="host" --name nodejs_psql --detach node:10-slim
```

- copy node.js project to container:
```
docker cp ~/work/api_nodejs_postgresql nodejs_psql:/home
```

- open a linux terminal inside the docker container (exit with Ctrl+D or with cmd: exit):
```
docker exec -it nodejs_psql bash
apt-get update && apt-get install procps nano mc curl
node -v
cd /home/api_nodejs_postgresql
npm install
npm start &

```

- on docker host server test with:
```
curlj http://localhost:3080/addNew -d  '{"name":"nodejs_11", "dataList":[1,2,3,4]}'
curlj http://localhost:3080/update -d  '{"id":"1852259", "name":"nodejs_11", "dataList":[11,12, 13]}'
```








