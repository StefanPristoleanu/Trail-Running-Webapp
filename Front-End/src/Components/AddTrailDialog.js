import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import { Button, Modal, Form, FormControl, FormGroup, Alert, Col, ControlLabel } from 'react-bootstrap';
import axios from 'axios';
import * as Cfg from '../Utils/ConfigSettings';
import * as MD from '../Utils/ModelData';
import * as MyLib from '../Utils/MyLib';
import LoginDialog from './LoginDialog';

class AddTrailDialog extends Component {
    constructor(props) {
        super(props);
        this.state = {
            showLogin: false, trailName: '', trailDifficulty: '', trailLength: 0, trailType: '', trailDescription: '', trailLikes: 0, errorMessage: '',
            deltaElevation: 0, trailSlope: 0
        }
        this.openDialog = this.openDialog.bind(this);
        this.closeDialog = this.closeDialog.bind(this);
        this.composeAddTrailJSON = this.composeAddTrailJSON.bind(this);
    }//end constructor

    openDialog() {
        const google = window.google;
        let myPoly = new google.maps.Polyline({ path: global.newTrail, geodesic: true, strokeColor: '#FF0000', strokeOpacity: 0.5, strokeWeight: 5 });
        //let trailLength = (google.maps.geometry.spherical.computeLength(myPoly.getPath()) / 1000).toFixed(2);
        let trailLength = (google.maps.geometry.spherical.computeLength(myPoly.getPath()));
        this.setState({ trailLength: Math.round(trailLength), showRegister: true });
    }

    closeDialog() { this.setState({ showRegister: false, errorMessage: '' }); }

    composeAddTrailJSON() {
        if(global.newTrail.length < 2){
            this.state.errorMessage = "Please activate Edit Mode and construct a trail.";
            this.closeDialog();
            return;
        }
        const google = window.google;
        let myPoly = new google.maps.Polyline({ path: global.newTrail, geodesic: true, strokeColor: '#FF0000', strokeOpacity: 0.5, strokeWeight: 5 });
        //let trailLength = (google.maps.geometry.spherical.computeLength(myPoly.getPath()) / 1000).toFixed(2);
        let trailLength = (google.maps.geometry.spherical.computeLength(myPoly.getPath()));

        console.log("global.newTrail:" + JSON.stringify(global.newTrail));
        var elevator = new google.maps.ElevationService;
        // Initiate the location request
        let path = [];
        for (let i = 0; i < global.newTrail.length; i++) {
            path[i] = global.newTrail[i];
        }
        console.log("~~~~path: " + path);
        var self = this;

        elevator.getElevationAlongPath({
            'path': path,
            'samples': 256
        }, async function (elevations, status) {
            let deltaElevation = 0;
            let trailSlope = 0;
            try {
                if (status === 'OK') {
                    //google.load('visualization', '1', {packages: ['columnchart']});

                    // Retrieve the first result
                    console.log('The elevation at this point <br>is ' +
                        JSON.stringify(elevations) + ' meters.');
                    let maxElevation = 0;
                    let minElevation = 10000;
                    for (let i = 0; i < elevations.length; i++) {
                        if (elevations[i].elevation > maxElevation)
                            maxElevation = elevations[i].elevation;
                        if (elevations[i].elevation < minElevation)
                            minElevation = elevations[i].elevation;
                    }
                    deltaElevation = maxElevation - minElevation;
                    trailSlope = elevations[elevations.length - 1].elevation - elevations[0].elevation;
                    console.log("MIN point: " + minElevation + " and MAX point: " + maxElevation + "\nSTART: " + elevations[0].elevation + " and END: " + elevations[elevations.length - 1].elevation);
                    /*var chartDiv = document.getElementById('elevation_chart');
                    var chart = new google.visualization.ColumnChart(chartDiv);
                    // Extract the data from which to populate the chart.
                    // Because the samples are equidistant, the 'Sample'
                    // column here does double duty as distance along the
                    // X axis.
                    var data = new google.visualization.DataTable();
                    data.addColumn('string', 'Sample');
                    data.addColumn('number', 'Elevation');
                    for (var i = 0; i < elevations.length; i++) {
                        data.addRow(['', elevations[i].elevation]);
                    }
                    // Draw the chart using the data within its DIV.
                    chart.draw(data, {
                        height: 150,
                        legend: 'none',
                        titleY: 'Elevation (m)'
                    });
                } else {
                    console.log('Elevation service failed due to: ' + status);*/
                }
            } finally {
                console.log("old path: " + JSON.stringify(global.newTrail));
                let json;
                if (global.isSnapToRoad) {
                    await MyLib.runSnapToRoad(global.newTrail);
                    json = {
                        coordinates: global.newTrailSnapToRoad,
                        trailName: self.state.trailName,
                        trailDifficulty: self.state.trailDifficulty,
                        trailLength: trailLength,
                        trailType: self.state.trailType,
                        trailDescription: self.state.trailDescription,
                        deltaElevation: deltaElevation,
                        trailSlope: trailSlope
                    }
                } else {
                    json = {
                        coordinates: global.newTrail,
                        trailName: self.state.trailName,
                        trailDifficulty: self.state.trailDifficulty,
                        trailLength: trailLength,
                        trailType: self.state.trailType,
                        trailDescription: self.state.trailDescription,
                        deltaElevation: deltaElevation,
                        trailSlope: trailSlope
                    }
                }
                console.log(JSON.stringify(json));
                self.closeDialog();
                self.props.callbackFunctionAddTrails(json);
            }
        });



    }

    render() {
        let errorAlert = <span></span>;
        if (this.state.errorMessage !== '') {
            errorAlert = <Alert bsStyle="warning"><strong>{this.state.errorMessage}</strong></Alert>;
        }
        return (
            <div style={{ paddingLeft: '10px' }}>
                <Button bsStyle="primary" active onClick={this.openDialog}>
                    Add Trail
                </Button>
                <Modal show={this.state.showRegister} onHide={this.closeDialog}>
                    <Modal.Header closeButton>
                        <Modal.Title>Add Trail</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        {errorAlert}
                        <Form horizontal>
                            <FormGroup controlId="formHorizontalTrailName">
                                <Col componentClass={ControlLabel} sm={4}>Trail Name:</Col>
                                <Col sm={7}>
                                    <FormControl type="text" placeholder="Set a name" onChange={(event) => this.setState({ trailName: event.target.value })} />
                                </Col>
                            </FormGroup>
                            <FormGroup controlId="formHorizontalTrailDifficulty">
                                <Col componentClass={ControlLabel} sm={4}>Difficulty:</Col>
                                <Col sm={7}>
                                    <FormControl componentClass="select" placeholder="How difficult was the trail" onChange={(event) => this.setState({ trailDifficulty: event.target.value })}>
                                        <option value="1">Easy</option>
                                        <option value="2">Moderate</option>
                                        <option value="3">Difficult</option>
                                    </FormControl>
                                </Col>
                            </FormGroup>
                            <FormGroup controlId="formHorizontalTrailLength">
                                <Col componentClass={ControlLabel} sm={4}>Computed Trail Length:</Col>
                                <Col sm={7}>
                                    <b>{this.state.trailLength} meters</b>
                                </Col>
                            </FormGroup>
                            <FormGroup controlId="formHorizontalTrailType">
                                <Col componentClass={ControlLabel} sm={4}>Trail Type:</Col>
                                <Col sm={7}>
                                    <FormControl componentClass="select" placeholder="Select" onChange={(event) => this.setState({ trailType: event.target.value })}>
                                        <option value="walking">Walking</option>
                                        <option value="running">Running</option>
                                        <option value="cycling">Cycling</option>
                                    </FormControl>
                                </Col>
                            </FormGroup>
                            <FormGroup controlId="formHorizontalTrailDescription">
                                <Col componentClass={ControlLabel} sm={4}>Description:</Col>
                                <Col sm={7}>
                                    <FormControl componentClass="textarea" type="text" placeholder="Optional" onChange={(event) => this.setState({ trailDescription: event.target.value })} />
                                </Col>
                            </FormGroup>
                            <FormGroup>
                                <Col smOffset={4} sm={7}>
                                    <Button bsStyle="success" type="button" onClick={this.composeAddTrailJSON}>Add</Button>
                                    <Button active onClick={this.props.callbackFunctionClearTrails}>
                                        Clear Trail
                                    </Button>
                                </Col>
                                <div id="elevation_chart"></div>

                            </FormGroup>
                        </Form>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button onClick={this.closeDialog}>Close</Button>
                    </Modal.Footer>
                </Modal>
            </div >
        );
    }// end render
}
export default withRouter(AddTrailDialog);
