var pg = require('pg')

// spring.datasource.url=jdbc:postgresql://localhost:5432/dbtest?stringtype=unspecified
// spring.datasource.username=u4dbtest
// spring.datasource.password=pa554DbT

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