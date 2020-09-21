import React, { Component } from 'react';
import { Button, ButtonToolbar, Nav, NavItem, navInstance, Modal, Form, FormControl, FormGroup, Alert, Col, ControlLabel } from 'react-bootstrap';
import { withRouter } from 'react-router-dom';

import RegisterDialog from '../Components/RegisterDialog.js';
import LoginDialog from '../Components/LoginDialog.js';
import * as MD from '../Utils/ModelData';

class NavBar extends Component {
    constructor(props) {
        super(props);
        this.state = {
            showPopup: false,
        }

        this.openHome = this.openHome.bind(this);
        this.openUserDetails = this.openUserDetails.bind(this);
        this.doLogout = this.doLogout.bind(this);
        this.openPopup = this.openPopup.bind(this);
        this.closePopup = this.closePopup.bind(this);
    }//end constructor

    openHome() {
        if (!MD.user.isLogged) {
            this.props.history.push('/');
        } else {
            this.props.history.push('/home');
        }
    }

    openUserDetails() {
        this.props.history.push('/user-details');
    }

    async doLogout() {
        const json = {
            ACCESS_TOKEN: MD.user.getUserData().ACCESS_TOKEN
        }
        MD.user.doLogout();
        this.props.history.push('/');
        this.closePopup();
    }//end doLogout

    openPopup() { this.setState({ showPopup: true }); }

    closePopup() { this.setState({ showPopup: false }); }

    render() {
        var divStyle = {
            color: 'red',

        };

        let buttons;
        if (!MD.user.isLogged) {
            buttons = <div><div style={{ float: 'right', backgroundColor: 'black' }}>
                <RegisterDialog />
            </div>
                <div style={{ float: 'right', backgroundColor: 'black' }}>
                    <LoginDialog />
                </div></div>
        } else {
            buttons = <div><div><Button bsStyle="default" onClick={this.openUserDetails}>
                User Details
        </Button></div> <div style={{ float: 'right', backgroundColor: 'black' }}>
                    <Button bsStyle="warning" onClick={this.openPopup}> Logout </Button>
                </div></div>
        }

        return (
            <div style={{ paddingLeft: '10px', backgroundColor: 'black' }}>
                <Nav style={divStyle} bsStyle="pills" onSelect={this.handleSelect}>
                    <Button bsStyle="info" onClick={this.openHome}>
                        Home
                    </Button>

                    {buttons}
                </Nav>
                <Modal show={this.state.showPopup} onHide={this.closePopup}>
                    <Modal.Header closeButton>
                        <Modal.Title>Logout</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        Please confirm logout.
                    </Modal.Body>
                    <Modal.Footer>
                        <Button bsStyle="warning" type="button" onClick={this.doLogout}>Logout</Button>
                        <Button onClick={this.closePopup}>Cancel</Button>
                    </Modal.Footer>
                </Modal>
            </div>
        )
    }// render
}

export default withRouter(NavBar);