import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import { Button, Modal, Form, FormControl, FormGroup, Alert, Col, ControlLabel, Checkbox } from 'react-bootstrap';
import axios from 'axios';
import * as Cfg from '../Utils/ConfigSettings';
import * as MD from '../Utils/ModelData';
import * as MyLib from '../Utils/MyLib';
import CheckboxDialog from './CheckboxDialog';

class RegisterDialog extends Component {
    constructor(props) {
        super(props);
        this.state = { showLogin: false, email: '', password: '', password2: '', nickname: '', errorMessage: '', checkInput: false }
        this.openRegister = this.openRegister.bind(this);
        this.closeRegister = this.closeRegister.bind(this);
        this.doRegister = this.doRegister.bind(this);
        this.changeCheck = this.changeCheck.bind(this);
    }//end constructor

    openRegister() { this.setState({ showRegister: true }); }

    closeRegister() { this.setState({ showRegister: false, errorMessage: '' }); }

    async doRegister() {
        //reset previous error msg:
        this.setState({ errorMessage: '' });
        if(this.state.checkInput === false){
            this.setState({ errorMessage: 'Please agree to the GDPR by ticking the box.' });
            return;
        }
        if (this.state.email.trim() === '' || !MyLib.validateEmail(this.state.email)) {
            this.setState({ errorMessage: 'Email format is required!' });
            return;
        }
        if (this.state.password.trim() === '') {
            this.setState({ errorMessage: 'Password is required!' });
            return;
        }

        if (!MyLib.validatePassword(this.state.password.trim())) {
            this.setState({ errorMessage: 'Password must be 7 to 15 characters which contain only characters, numeric digits, underscore and first character must be a letter.' });
            return;
        }
        //console.log("pass: " + MyLib.validatePassword(this.state.password.trim()));
        if (this.state.password != this.state.password2) {
            this.setState({ errorMessage: 'The passwords do not match!' });
            return;
        }
        const json = {
            username: this.state.email,
            password: this.state.password,
            nickname: this.state.nickname,
            appVersion: Cfg.APP_VERSION,
        };
        var self = this;
        await axios.post(Cfg.SERVER_URL + "user/register", json)
            .then(function (response) {
                console.log("login response: " + JSON.stringify(response.data));
                if (response.data.responseCode < 0) {
                    self.setState({ errorMessage: response.data.message });
                } else {
                    MD.user.setUserData(response.data);
                    self.closeRegister();
                }
            })
            .catch(function (error) {
                console.log(error);
            });
    }//end doRegister

    changeCheck(){
        if(this.state.checkInput === true)
            this.setState({ checkInput: false});
        else
        this.setState({ checkInput: true});
    }

    render() {
        let errorAlert = <span></span>;
        if (this.state.errorMessage !== '') {
            errorAlert = <Alert bsStyle="warning"><strong>{this.state.errorMessage}</strong></Alert>;
        }
        return (
            <div style={{ paddingLeft: '10px' }}>
                <Button bsStyle="success" active onClick={this.openRegister}>
                    Register
                </Button>
                <Modal show={this.state.showRegister} onHide={this.closeRegister}>
                    <Modal.Header closeButton>
                        <Modal.Title>Register</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        {errorAlert}
                        <Form horizontal>
                            <FormGroup controlId="formHorizontalEmail">
                                <Col componentClass={ControlLabel} sm={4}>Email:</Col>
                                <Col sm={7}>
                                    <FormControl type="email" placeholder="Email" onChange={(event) => this.setState({ email: event.target.value })} />
                                </Col>
                            </FormGroup>
                            <FormGroup controlId="formHorizontalPassword">
                                <Col componentClass={ControlLabel} sm={4}>Password:</Col>
                                <Col sm={7}>
                                    <FormControl type="password" placeholder="Password" onChange={(event) => this.setState({ password: event.target.value })} />
                                </Col>
                            </FormGroup>
                            <FormGroup controlId="formHorizontalPassword2">
                                <Col componentClass={ControlLabel} sm={4}>Re-type Password:</Col>
                                <Col sm={7}>
                                    <FormControl type="password" placeholder="Re-type password" onChange={(event) => this.setState({ password2: event.target.value })} />
                                </Col>
                            </FormGroup>
                            <FormGroup controlId="formHorizontalNickname">
                                <Col componentClass={ControlLabel} sm={4}>Nickname:</Col>
                                <Col sm={7}>
                                    <FormControl type="text" placeholder="Set a nickname" onChange={(event) => this.setState({ nickname: event.target.value })} />
                                </Col>
                            </FormGroup>
                            <FormGroup controlId="formCheckbox">
                                <Col componentClass={ControlLabel} sm={2}>
                                <CheckboxDialog />

                                </Col>
                                <Col sm={10}>
                                <Checkbox style={{paddingLeft: '150px'}} inline onClick={this.changeCheck}>                
                                </Checkbox>

                                </Col>
                                
                            </FormGroup>
                            <FormGroup>
                                <Col smOffset={4} sm={7}>
                                    <Button bsStyle="primary" type="button" onClick={this.doRegister}>Register</Button>
                                </Col>
                            </FormGroup>
                        </Form>
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
