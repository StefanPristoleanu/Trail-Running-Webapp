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

app.post('/find-ownerId', [
    MyController.findByOwnerId
]);
// start API server using config.port
app.listen(config.port, function () {
    console.log('API Node.js Server listening at port %s', config.port);
});
