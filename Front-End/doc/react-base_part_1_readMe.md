
# React.js base project - Part 1
---------------------------------
**2018.10.24**

__Note__: For the best view, open this md document with Visual Studio Code or any other good markdown doc viewer.

## 1. Preparing the development environment:

### 1.1. install nodejs and git
#### - install nodejs:
- download and install [nodejs]( https://nodejs.org/en/download/ )
- add in PATH the coressponding folder, so you should be able to invoke node and npm on your command line 
- in Windows -> System properties -> Environment Variables add in PATH: C:\programs\node
- in Linux as root: `` yum install nodejs `` or `` sudo apt-get install nodejs ``
- in macOS use tutorial: https://medium.com/@krissanawat/2-ways-to-install-nodejs-on-macos-the-beginners-guide-55c457e66441
- test in console with:
```
    node --version 
    npm --version 
```

#### - install git:
- download and install [git client]( https://git-scm.com/download/ )
- add in PATH the coressponding folder, so you should be able to invoke git on your command line 
- in Windows -> System properties -> Environment Variables add in PATH:  C:\programs\git\cmd
- in Linux install with `` yum install git `` or `` sudo apt-get install git `` 
- in Linux/MaxOS you can add the git path to PATH var by edit the file: `` nano ~/.bash_profile `` or `` nano ~/.bashrc `` 
- test in console with: 
`` git --version `` 

### 1.2. create the app framework:
#### - install global [React.js]( https://reactjs.org/ ):
`` npm install create-react-app -g `` 

#### - create the react basic app or checkout from svn/git:
``` 
    create-react-app react-base 
``` 
#### - install [React-Bootstrap]( https://react-bootstrap.github.io/ ):
__Note__:  for all following cmds you need to open a terminal in the project folder root (/react-base/) 
``` 
    cd react-base
    npm install bootstrap@3 react-bootstrap ajv --save
    npm install font-awesome --save 
    npm install react-router-dom react-router-bootstrap --save
    npm install axios --save
```  
- start the app with command: `` npm start `` and open a browser at [http://localhost:3000/]( http://localhost:3000/ )

---

## 2. Develop a basic react app

__Note__: 
            - you can skip step 1.2 if you start from an existing project (git/svn or a zip archive) and run:
            ` npm install `
            - use your favorite JavaScript IDE, I recommand **Visual Studio Code** available for Windows, Linux and macOS:
            [https://code.visualstudio.com/]( https://code.visualstudio.com/ )

### 2.1. create first pages: 
-  create the following folders in <project_root/src/>: Components, Pages and Utils
#### -  create PublicHomePage.js file in /Pages/:
```JS
import React, { Component } from 'react';
import { withRouter} from 'react-router-dom';
import RegisterDialog from '../Components/RegisterDialog.js';
import LoginDialog from '../Components/LoginDialog.js';
import { ButtonToolbar} from 'react-bootstrap';
class PublicHomePage extends Component {
  constructor(props) {
    super(props);
    this.state = {
    };
  }//end constructor
  render() {
    return (
      <div>
        <h3> Public Home Page </h3>
        <ButtonToolbar>
          <LoginDialog/>
          <RegisterDialog/>
        </ButtonToolbar>
      </div>
    );
  }//end render
}
export default withRouter(PublicHomePage);
```

#### - create UserHomePage.js file in /Pages/ :
```JS
import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import { Button, ButtonToolbar } from 'react-bootstrap';
class UserHomePage extends Component {
    constructor(props) {
        super(props);
        this.state = { errorMessage: '' };
        this.doLogout = this.doLogout.bind(this);
    }//end constructor
    componentWillMount() {
        if (!global.isLogin) {
            this.props.history.push('/');
        }
    }
    async doLogout() {
        global.isLogin = false;// ToDo ...
        this.props.history.push('/');
    }
    render() {
        return (
            <div>
                <h3> User Home Page </h3>
                <ButtonToolbar>
                    <Button bsStyle="warning" bsSize="large" active onClick={this.doLogout}>Logout</Button>
                </ButtonToolbar>
            </div>
        );
    }//end render
}
export default withRouter(UserHomePage); 
```

#### - create LoginDialog.js file in /Components/ with a visible component: "Login" button and a popup modal window: 
- you can test the "login" with any non empty email addres for navigate to UserHome page
- for any empty login email address an error will be displayed in popup modal window
- __ToDo__: the communication with API Server will be implemented in a future task 

```JS
import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import { Button, Modal, Form, FormControl, FormGroup, Alert, Col, ControlLabel } from 'react-bootstrap';
class LoginDialog extends Component {
    constructor(props) {
        super(props);
        this.state = {
            showLogin: false, email: '', password: '', errorMessage: ''
        }
        this.openLogin = this.openLogin.bind(this);
        this.closeLogin = this.closeLogin.bind(this);
        this.doLogin = this.doLogin.bind(this);
    }//end constructor
    openLogin() { this.setState({ showLogin: true }); }
    closeLogin() { this.setState({ showLogin: false, errorMessage: '' }); }
    async doLogin() {
        console.log('~~~ login - email: ' + this.state.email);
        this.setState({ errorMessage: '' });
        if (this.state.email.trim() === '') {
            this.setState({ errorMessage: 'Email is required!' });
            return;
        }
        if (this.state.password.trim() === '') {
            this.setState({ errorMessage: 'Password is required!' });
            return;
        }
        global.isLogin = true;// ToDo ...
        this.closeLogin();
        this.props.history.push('/home');  // browser redirect to home page for user after login ... 
    }// doLogin
    render() {
        let errorAlert = <span></span>;
        if (this.state.errorMessage !== '') {
            errorAlert = <Alert bsStyle="warning"><strong>{this.state.errorMessage}</strong></Alert>;
        }
        return (
            <div style={{ paddingLeft: '10px' }}>
                <Button bsStyle="primary" bsSize="large" active onClick={this.openLogin}>Login</Button>
                <Modal show={this.state.showLogin} onHide={this.closeLogin}>
                    <Modal.Header closeButton>
                        <Modal.Title>Login</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        {errorAlert}
                        <Form horizontal>
                            <FormGroup controlId="formHorizontalEmail">
                                <Col componentClass={ControlLabel} sm={2}>Email:</Col>
                                <Col sm={10}>
                                    <FormControl type="email" placeholder="Email" onChange={(event) => this.setState({ email: event.target.value })} />
                                </Col>
                            </FormGroup>
                            <FormGroup controlId="formHorizontalPassword">
                                <Col componentClass={ControlLabel} sm={2}>Password:</Col>
                                <Col sm={10}>
                                    <FormControl type="password" placeholder="Password" onChange={(event) => this.setState({ password: event.target.value })} />
                                </Col>
                            </FormGroup>
                            <FormGroup>
                                <Col smOffset={2} sm={10}>
                                    <Button bsStyle="primary" type="button" onClick={this.doLogin}>Login</Button>
                                </Col>
                            </FormGroup>
                        </Form>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button onClick={this.closeLogin}>Close</Button>
                    </Modal.Footer>
                </Modal>
            </div>
        );
    }// end render
}
export default withRouter(LoginDialog);
```
#### - create RegisterDialog.js file in /Components/ with one visible component: "Register" button and a popup modal window:
- __ToDo__: this component will be implemented in a future task

```JS
import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import { Button, Modal, Alert} from 'react-bootstrap';
class RegisterDialog extends Component {
    constructor(props) {
        super(props);
        this.state = { showLogin: false, email: '', password: '', errorMessage: ''}
        this.openRegister = this.openRegister.bind(this);
        this.closeRegister = this.closeRegister.bind(this);
        this.doRegister = this.doRegister.bind(this);
    }//end constructor
    openRegister() { this.setState({ showRegister: true }); }
    closeRegister() { this.setState({ showRegister: false, errorMessage: '' }); }
    async doRegister(){       
    }
    render() {
        let errorAlert = <span></span>;
        if (this.state.errorMessage !== '') {
            errorAlert = <Alert bsStyle="warning"><strong>{this.state.errorMessage}</strong></Alert>;
        }
        return (
            <div style={{ paddingLeft: '10px' }}>
                <Button bsStyle="success" bsSize="large" active onClick={this.openRegister}>
                    Register
                </Button>
                <Modal show={this.state.showRegister} onHide={this.closeRegister}>
                    <Modal.Header closeButton>
                        <Modal.Title>Register</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        {errorAlert}
                        <h3>ToDo - Register Page</h3>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button onClick={this.closeRegister}>Close</Button>
                    </Modal.Footer>
                </Modal>
            </div>
        );
    }// end render
}
export default withRouter(RegisterDialog);
```

### 2.2 update App.js for css and router:
- add import for bootstrap and font-awesome css
- add import for router
- create first router switch and update the render method as below: 

```JS 
import React, { Component } from 'react';
import './App.css';
import 'bootstrap/dist/css/bootstrap.css';
import 'bootstrap/dist/css/bootstrap-theme.css';
import 'font-awesome/css/font-awesome.min.css';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';
// add HashRouter, HashHistory for Corodva Hybrid Mobile App
import PublicHome from './Pages/PublicHomePage.js';
import UserHome from './Pages/UserHomePage.js';
class App extends Component {
    constructor(props) {
        super(props);
        console.log('App start ...');
        global.isLogin = false;
    }
    myRouterSwitch() {
        return (
            <Switch>
                <Route exact path="/" component={PublicHome} />
                <Route path="/home" component={UserHome} />
            </Switch>
        );
    }//end myRouterSwitch
    render() {
        let routerSwitch = this.myRouterSwitch();
        return (
            <div className="App">
                <Router history={Router.HistoryLocation} >
                    {routerSwitch}
                </Router>
            </div>
        );
    }//end render
}
export default App;
```

### 2.3 run React app with `` npm start `` and test in browser at:
[http://localhost:3000/]( http://localhost:3000/ )


__For React Bootstrap components__: check [react-bootstrap.github.io]( https://react-bootstrap.github.io/components/buttons/ )

Also check the doc: React.js base project - Part 2 (react-base_part_2_readMe.md). 

Have fun! :)
