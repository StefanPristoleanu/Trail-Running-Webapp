
Node.js - REST - MongoDB
============

- v1.0 - 2018.12.09 - first version

-----
 
**References:**
[http://mongodb.github.io/node-mongodb-native/3.1/quick-start/quick-start/]


-----
 
**Requirements:**
- install Node.js: [https://nodejs.org/en/]
- access to MondoDB database: [https://www.mongodb.com/]

__Note :__
- I recommend creating a curl alias: __curlj__ with the command below (can be added to ~ / .bash_profile or ~ / .bashrc):
`
    alias curlj='curl --header "Content-Type: application/json" -w "\n" -v'
`
  - add at the end of the curlj cmd: ` | json_pp ` for formatted display of the json response
  - the -v parameter in curl can be removed if there is no need to display headers for request and response
  
-----

```
mkdir api_nodejs_mongodb
cd api_nodejs_mongodb
```

- use npm init tp create package.json and for "entry point" type: server.js
```
npm init
npm install express body-parser moment fs-extra mongodb --save
```

- I recommend installing Nodemon as a dev dependency. It’s a simple little package that automatically restarts your server when files change:
```
npm install eslint nodemon --save-dev 
```


npm install express-mongo-db --save 

- then add/update the package.json - scripts section with:  
```
 , "start": "node app/server.js", "dev": "nodemon server.js"
 
```

- the package.json file:
```JSON
{
  "name": "api_nodejs_mongodb",
  "version": "1.0.0",
  "description": "Test API project for Node.js and MongoDB",
  "main": "server.js",
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
    "mongodb": "^3.1.10"
  },
  "devDependencies": {
    "eslint": "^5.10.0",
    "nodemon": "^1.18.7"
  }
}
```

```
curlj http://localhost:3090/addNew -d  '{"name":"nodejs_10", "dataList":[1,2,3,4]}'

curlj http://localhost:3090/update -d  '{"id":"5c1024fe10bbab67d588788a", "name":"nodejs_10up", "dataList":[10,20,30,40]}'


```
