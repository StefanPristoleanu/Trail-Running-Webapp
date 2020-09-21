"use strict";

var moment = require('moment');

class DbTable1 {

    // we add ' RETURNING id' at the end of the sql for insert so that we can get the 'id' for auto-incremented PK
    generateInsert(jsonRequest) {
        if (jsonRequest === undefined || jsonRequest === null || jsonRequest.name === undefined) {
            return '';
        }
        let createdAt = moment(new Date()).format("YYYY-MM-DD HH:mm:ss");
        let lastUpdatedAt = createdAt;
		//console.log(JSON.stringify(jsonRequest));
        jsonRequest.dataList.counterUpdates = 0;
        //console.log('addNew - jsonRequest: ' + JSON.stringify(jsonObj));
		
		let sql = "INSERT INTO db_table_2(name, data_json, created_at, last_updated_at, trail_created_by) VALUES ('"+jsonRequest.name+"', '"+JSON.stringify(jsonRequest.dataList)+
			"', '"+createdAt+"', '"+lastUpdatedAt+"', "+jsonRequest.ownerId+")RETURNING id";
        return sql;
    };

    generateFinByPK(jsonRequest){
        if (jsonRequest === undefined || jsonRequest === null || jsonRequest.id === undefined) {
            return '';
        }
        let sql = "SELECT data_json from db_table_2 where id=" + parseInt(jsonRequest.id);
        return sql;
    };

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
        let sql = "UPDATE db_table_2 SET data_json='" + JSON.stringify(data_json) +
        "' WHERE id=" + parseInt(jsonRequest.id);
        return sql;
    };

    generateFindByOwnerId(jsonRequest){
        if (jsonRequest === undefined || jsonRequest === null || jsonRequest.ownerId === undefined) {
            return '';
        }
        let sql = "SELECT * FROM DB_TABLE_2 t WHERE t.TRAIL_CREATED_BY = " + jsonRequest.ownerId + " limit 10";
        return sql;
    };
}

// export DbTable1 class as a singleton:
const dbTable1 = new DbTable1();
module.exports = dbTable1;
