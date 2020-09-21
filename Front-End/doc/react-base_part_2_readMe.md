
# React.js base project - Part 2
---------------------------------
**2018.10.24**

__Note__: For the best view, open this md document with Visual Studio Code or any other good markdown doc viewer.

This phase of the project continues the previous work from doc: react-base_part_1_readMe.md.

## 1. Implement REST WebServer API methods

### 1.1. prepare the Chrome browser for API development:
Because of 'Access-Control-Allow-Origin' restriction - [CORS](https://en.wikipedia.org/wiki/Cross-origin_resource_sharing)
we cannot communicate from local host (http://localhost:3000/) to the API server URL,
so, we need to configure the browser in development mode (disable security):
    * configure Chrome browser in development mode (the recommended option):
        - on Windows create a Chrome shortcut (the folder C:\work\tmp-dev-chrome must be created):
```
    "C:\Program Files (x86)\Google\Chrome\Application\chrome.exe" --user-data-dir="C:\work\tmp-dev-chrome" --disable-web-security
```
        - on macOS create the tmp folder ( ~/work/tmp-dev-chrome ) and a script named dev-chrome.sh :
`
    /Applications/Google\ Chrome.app/Contents/MacOS/Google\ Chrome --user-data-dir="~/work/tmp-dev-chrome" --disable-web-security & 
` 
        - on Linux create the tmp folder ( ~/work/tmp-dev-chrome ) and a script named dev-chrome.sh :
```
    google-chrome --user-data-dir="~/work/tmp-dev-chrome" --disable-web-security & 
```
    * Configure Firefox: install ‘CORS Everywhere’ addon. 
    Functionality can be toggled with the included button and is disabled by default. 

### 1.2. create the global class ConfigSettings
- in src/Utils/ create a new file: ConfigSettings.js
- add here all the global constants of the application as:
```JS
    export const SERVER_URL = "...";
    export const APP_VERSION = '0.0.1';
```

### 1.3. add API call for user login method:
- edit src/Components/LoginDialog.js and add import for axios and ConfigSettings.js:
```JS
    import axios from 'axios';
    import * as Cfg from '../Utils/ConfigSettings';
```
- change **doLogin** method as following:
```JS
    async doLogin() {
        //reset previous error msg:
        this.setState({ errorMessage: '' });
        if (this.state.email.trim() === '') {
            this.setState({ errorMessage: 'Email is required!' });
            return;
        }
        if (this.state.password.trim() === '') {
            this.setState({ errorMessage: 'Password is required!' });
            return;
        }
        const json = {
            USERNAME: this.state.email,
            PASSWORD: this.state.password,
            APP_VERSION: Cfg.APP_VERSION,
        };
        var self = this;
        await axios.post(Cfg.SERVER_URL + "/do-login-user", json)
          .then(function (response) {
            console.log("API server response: " + JSON.stringify(response.data));
            if(response.data.responseCode < 0){
                self.setState({ errorMessage: response.data.message });
            }else{
                global.isLogin = true; // ToDo: save user's data
                global.ACCESS_TOKEN = response.data.ACCESS_TOKEN;
                self.closeLogin();
                self.props.history.push('/home');  // browser redirect to home page for user after login ...
            }
          })
          .catch(function (error) {
            console.log(error);
          });
    }// doLogin
```
- test with and without email or password data in order to check the error message
- in Chrome - Deleopment Tools panel, in Netwotk tab check the communication with API server
- because we don't have the registration page ready we need to add a new user on server using manual curl call. 

### 1.4. add API call for register method:
- update RegisterDialog.js with the content from LoginDialog.js as following:
    - copy/update the imports for axios, Cfg and the correspondig react-bootstrap components
    - copy all the content of doLogin method in doRegister method and change the URL in axios to:
    ` Cfg.SERVER_URL + "/do-register-user" `
and ` self.closeLogin(); `  to `  self.closeRegister(); `    
    - in RegisterDialog.js, the render method, replace ` <h3>ToDo - Register Page</h3> ` 
with the content of <Form horizontal> ... </Form> from LoginDialog.js. 
    - in this new added Form change the button to:
```JS
    <Button bsStyle="primary" type="button" onClick={this.doRegister}>Register</Button>
```
- test by registering a few new users and then test login method

### 1.5. add API call for logout method:
- update UserHomePage.js as:
    - add new imports for axios, ConfigSettings and also add Alert from react-bootstrap:
```JS
    import { Button, ButtonToolbar, Alert } from 'react-bootstrap';
    import axios from 'axios';
    import * as Cfg from '../Utils/ConfigSettings';
```
    - change doLogout method:
```JS
    async doLogout() {
        const json = {
            ACCESS_TOKEN: global.ACCESS_TOKEN
        }
        await axios.post(Cfg.SERVER_URL + "/do-logout-user", json)
            .then(function (response) {
                console.log("API server response: " + JSON.stringify(response.data));
            })
            .catch(function (error) {
                console.log(error);
            });
        global.isLogin = false;
        this.props.history.push('/');
    }//end doLogout
```
- test logout

### 1.6. add API call for get user data method:
- update UserHomePage.js:
    - in constructor add userData field and a new method declararion:
```JS
    this.state = {
            errorMessage: '', userData: {}
        };
    this.requestGetUserData = this.requestGetUserData.bind(this);
```

    - add new method in UserHomePage.js:
```JS
    requestGetUserData() {
        const json = {
            ACCESS_TOKEN: global.ACCESS_TOKEN
        }
        var self = this;
        axios.post(Cfg.SERVER_URL + "/get-user-data", json)
            .then(function (response) {
                console.log("API server response: " + JSON.stringify(response.data));
                if (response.data.responseCode < 0) {
                    self.setState({ errorMessage: response.data.message });
                } else {
                    self.setState({ userData: response.data.queryData[0] });
                }
            })
            .catch(function (error) {
                console.log(error);
            });
    }//end requestGetUserData
```
    - in order to request userData from API server, update componentDidMount from UserHomePage.js as below:
```JS
    componentDidMount() {
        if (!MD.user.isLogged) {
            this.props.history.push('/');
        } else {
            this.requestGetUserData();
        }
    }
```

    - update render method from UserHomePage.js as:
```JS
    render() {
        let errorAlert = <span></span>;
        if (this.state.errorMessage !== '') {
            errorAlert = <Alert bsStyle="warning"><strong>{this.state.errorMessage}</strong></Alert>;
        }
        return (
            <div>
                <h3> User Home Page </h3>
                {errorAlert}
                <ButtonToolbar>
                    <Button bsStyle="warning" bsSize="large" active onClick={this.doLogout}>Logout</Button>
                </ButtonToolbar>
                <br /><hr />
                <pre style={{ textAlign: 'left' }}>
                    {JSON.stringify(this.state.userData, null, "\t")}
                </pre>
            </div>
        );
    }//end render
```
- tests all 4 API methods: register, login , get user data and logout

-------

## 2. Implement methods for ModelData class

With this new class **ModelData** we will manage the user data and save it to and loaded from the localStorage.

### 2.1 update ConfigSettings.js and add:
```JS
   /**
     * MAX_INACTIVITY_MS is in milliseconds
     * we accept max. 10 min of inactivity to validate the current session at reload / refresh browser
     */
    export const MAX_INACTIVITY_MS = 10 * 60 * 1000; 
```

### 2.2 in src/Utils/ create a new file: ModelData.js:
```JS
import * as Cfg from './ConfigSettings';
class User {
    constructor() {
        this.isLogged = false;
        this.loadFromLocalStorage();
    }
    /**
     * loadFromLocalStorage - load userData form browser's local storage if exist
     * if not exist in local storage then init userData with undefined
     */
    loadFromLocalStorage() {
        if (localStorage.getItem('userData') != null) {
            this.userData = JSON.parse(localStorage.getItem('userData'));
            console.log("OK loaded userData from localStorage: " + this.userData.jsLastAccessDate);
            // we accept max. 10 min of inactivity to validate the current session at reload / refresh browser:
            if (this.userData.jsLastAccessDate != null
                && (Date.now() - new Date(this.userData.jsLastAccessDate).getTime() < Cfg.MAX_INACTIVITY_MS)) {
                this.isLogged = true;
            }
        }else{
            initUserData.call(this);
        }
    }//end loadFromLocalStorage
    getUserData() {
        return this.userData;
    }
    setUserData(value) {
        this.userData = value;
        this.userData.jsLastAccessDate = new Date();
        this.isLogged = true;
        //save userData to browser's local storage:
        localStorage.setItem('userData', JSON.stringify(this.userData));
    }
    doLogout() {
        this.isLogged = false;
        initUserData.call(this);
        //remove userData from  browser's local storage:
        localStorage.removeItem('userData');
    }
    isLogged() {
        return this.isLogged;
    }
} //end class User
function initUserData() { //private method
    this.userData = {
        jsLastAccessDate: undefined,
    }
}
// export User class as a singleton:
export const user = new User();
```

### 2.3 update imports for ModelData:
    - LoginDialog.js, RegisterDialog.js and UserHomePage.js add:
```JS
    import * as MD from '../Utils/ModelData';
```
### 2.4 use MD.user object:
    - in App.js remove: ` global.isLogin = false; `
    - in LoginDialog.js - doLogin  remove the line: ` global.ACCESS_TOKEN = response.data.ACCESS_TOKEN; `
    - replace in all files: ` global.isLogin = true; ` with ` MD.user.setUserData(response.data); `
    - replace in UserHomePage.js: ` if (!global.isLogin) ` with ` if (!MD.user.isLogged) `
    - replace in all files global.ACCESS_TOKEN with MD.user.getUserData().ACCESS_TOKEN, so now we will use:
    ```
        const json = {
            ACCESS_TOKEN: MD.user.getUserData().ACCESS_TOKEN
    ```
    - in UserHomePage.js, in method requestGetUserData replace:
```
    else {
            self.setState({ userData: response.data.queryData[0] });
    }
```
with:
```
     else {
            MD.user.setUserData(response.data.queryData[0]);
            self.setState({ userData: response.data.queryData[0] });
     }
```
- run ` npm start ` and test by refreshing the browser after login.

__Note__: to verify **localStorage** in Chrome use the following steps:
    - open Deleopment Tools panel and select the Application tab
    - select 'Local Storage' for your browser's current address (http://localhost:3000)
    - and here you will find the key: userData and the corresponding value in json format.
    You can change the value for example the ACCESS_TOKEN or jsLastAccessDate value and refesh the browser.
