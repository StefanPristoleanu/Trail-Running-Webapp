
const tbl = require('../entities/DbTable1.js');
const db = require('../common/Database.js');

// for postgresql transactions see:
// https://node-postgres.com/features/transactions

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
            //console.log('sql: ' + sql);
            //const res = await dbConn.query('SELECT * FROM DB_TABLE_1 WHERE id = $1', [1]);
            const dbResponse = await dbConn.query(sql);
            //console.log(dbResponse);//dbResponse.rows[0]);
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
        jsonResponse = { error: e.stack }
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
            //console.log(dbResponse);//dbResponse.rows[0]);
            if (dbResponse.rowCount > 0) {
                const data_json = dbResponse.rows[0].data_json;
                dbResponse = await dbConn.query(tbl.generateUpdate(data_json, req.body));
                //console.log(dbResponse);
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

// curlj http://localhost:3080/find-ownerId -d  '{"name":"n2"}'
module.exports.findByOwnerId = (req, res) => {
    const sql = tbl.generateFindByOwnerId(req.body);
	//console.log("SQL: " + sql);
    if (sql === '') {
        res.status(400).send('name tag not found in json request');
        return;
    }
    let jsonResponse = {};
    (async () => {
        const dbConn = await db.connMgr.connect()
        try {
            const dbResponse = await dbConn.query(sql);
            //console.log(dbResponse);//dbResponse.rows[0]);
            if (dbResponse.rowCount > 0) {
                jsonResponse = {
                    responseCode: 0,
                    message: 'OK query records from DB_TABLE_2 with name: ' + req.body.name,
                    resultList: dbResponse,
                };
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