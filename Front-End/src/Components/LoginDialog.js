import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import { Button, Modal, Form, FormControl, FormGroup, Alert, Col, ControlLabel } from 'react-bootstrap';
import axios from 'axios';
import * as Cfg from '../Utils/ConfigSettings';
import * as MD from '../Utils/ModelData';

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
            username: this.state.email,
            password: this.state.password,
            appVersion: Cfg.APP_VERSION,
        };
        var self = this;
        await axios.post(Cfg.SERVER_URL + "user/login", json)
          .then(function (response) {
            console.log("API server response: " + JSON.stringify(response.data));
            if(response.data.responseCode < 0){
                self.setState({ errorMessage: response.data.message });
            }else{
                let authToken = response.headers.authorization;
                axios.defaults.headers.common['Authorization'] = authToken;
                MD.user.setUserData(response.data);
                MD.user.setUserAuth(authToken);
                self.closeLogin();
                self.props.history.push('/home');  // browser redirect to home page for user after login ...
            }
          })
          .catch(function (error) {
            console.log(error);
          });
    }// doLogin

    render() {
        let errorAlert = <span></span>;
        if (this.state.errorMessage !== '') {
            errorAlert = <Alert bsStyle="warning"><strong>{this.state.errorMessage}</strong></Alert>;
        }
        return (
            <div style={{ paddingLeft: '10px' }}>
                <Button bsStyle="primary" active onClick={this.openLogin}>
                    Login
                </Button>
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
