import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import { Button, ButtonToolbar, Alert, Panel, Form, FormControl, FormGroup, Col, ControlLabel } from 'react-bootstrap';
import NavBar from '../Components/Navbar.js';
import Footer from '../Components/Footer.js';
import axios from 'axios';
import * as Cfg from '../Utils/ConfigSettings';
import * as MD from '../Utils/ModelData';
import * as MyLib from '../Utils/MyLib';
import { GMap } from 'primereact/gmap';


// Google maps icons: http://kml4earth.appspot.com/icons.html

class UserDetailsPage extends Component {
    constructor(props) {
        super(props);
        this.state = {
            errorMessage: '',
            userData: {},
            username: '',
            nickname: '',
            registeredAt: '',
            lastLoginAt: '',
            trails: [],
            constructedTrail: [],
            totalUserLikes: 0,
            likedTrails: []
        };
        this.requestGetUserData = this.requestGetUserData.bind(this);
        this.getMyTrails = this.getMyTrails.bind(this);
        this.loadTrails = this.loadTrails.bind(this);
        this.clearTrails = this.clearTrails.bind(this);
        this.onOverlayClick = this.onOverlayClick.bind(this);
        this.updateUser = this.updateUser.bind(this);
    }//end constructor

    componentDidMount() {
        //this.requestGetUserData();
        if (!MD.user.isLogged) {
            this.props.history.push('/');
        } else {
            this.requestGetUserData();
        }
    }

    updateUser() {
        if (this.state.username.trim() === '' || !MyLib.validateEmail(this.state.username)) {
            this.setState({ errorMessage: 'Email format is required!' });
            return;
        }
        if (this.state.nickname.trim() === '') {
            this.setState({ errorMessage: 'Nickname is required!' });
            return;
        }

        let json = {
            userId: MD.user.getUserData().userId,
            nickname: this.state.nickname,
            username: this.state.username
        };
        var self = this;
        axios.post(Cfg.SERVER_URL + "user/update", json)
            .then(function (response) {
                console.log("API server response: " + JSON.stringify(response.data));
                if (response.data.responseCode < 0) {
                    self.setState({ errorMessage: response.data.message });
                } else {
                    //window.location.reload();
                    //TODO: display success
                    self.setState({ errorMessage: 'Details successfully updated!' });
                }
            })
            .catch(function (error) {
                console.log(error);
            });
    }

    requestGetUserData() {
        var self = this;
        axios.get(Cfg.SERVER_URL + "user/details")
            .then(function (response) {
                console.log("API server response: " + JSON.stringify(response.data));
                if (response.data.responseCode < 0) {
                    self.setState({ errorMessage: response.data.message });
                } else {
                    MD.user.setUserData(response.data);
                    self.setState({ userData: response.data });
                    self.setState({ username: response.data.username });
                    self.setState({ nickname: response.data.nickname });
                    let date = response.data.registeredAt.split(" ");
                    self.setState({ registeredAt: date[0] });
                    self.setState({ lastLoginAt: response.data.lastLoginAt });
                    self.setState({ likedTrails: response.data.likedTrails});
                    console.log("likedTrails: ", response.data.likedTrails);
                    self.getMyTrails();
                }
            })
            .catch(function (error) {
                console.log(error);
            });
    }//end requestGetUserData

    async getMyTrails() {
        var self = this;
        global.allTrailsResponse = {};
        axios.get(Cfg.SERVER_URL + "trail/user-trails" + "?userId=" + MD.user.getUserData().userId)
            .then(function (response) {
                console.log("API server response getMyTrails: " + JSON.stringify(response.data));
                if (response.data.responseCode < 0) {
                    self.setState({ errorMessage: response.data.message });
                } else {
                    global.allTrailsResponse = response;
                    self.loadTrails(self, response);
                }
            })
            .catch(function (error) {
                console.log(error);
            });
    }

    loadTrails(self, response) {
        console.log("loadTrails" + " " + JSON.stringify(response));
        this.setState({ trails: [] });
        const google = window.google;
        var imageStart = {
            url: 'http://maps.google.com/mapfiles/kml/paddle/go.png',
            scaledSize: new google.maps.Size(40, 40),
        };
        var imageEnd = {
            url: 'http://maps.google.com/mapfiles/kml/paddle/red-circle.png',
            scaledSize: new google.maps.Size(40, 40),
        };

        let trailGPS = [];
        let trailName = '', trailDifficulty = '', trailLength = '', trailType = '', trailLikes = 0, trailId = '';
        let tmpTrails = [];
        //console.log("\n" + response.data.trails);
        for (let i = 0; i < response.data.length; i++) {
            trailGPS = response.data[i].coordinates;
            //console.log("\n coord: " + trailGPS);
            trailName = response.data[i].trailName;
            trailLength = response.data[i].trailLength;
            trailDifficulty = response.data[i].trailDifficulty;
            trailType = response.data[i].trailType;
            trailId = response.data[i].trailId;
            trailLikes = self.state.totalUserLikes;
            trailLikes = trailLikes + response.data[i].trailNoOfLikes;
            self.setState({ totalUserLikes: trailLikes });
            //console.log("trailGPS.length for "+ i + ": " + trailGPS.length + "       " +  JSON.stringify(trailGPS[0]));
            tmpTrails.push(new google.maps.Polyline({ path: trailGPS, geodesic: true, strokeColor: '#0000FF', strokeOpacity: 0.5, strokeWeight: 5 }));
            var startMarker = new google.maps.Marker({ position: trailGPS[0], title: "Start Trail, " + trailName + ", " + trailDifficulty + ", " + trailLength + ", " + trailType + ", " + trailId, icon: imageStart });
            tmpTrails.push(startMarker);
            var endMarker = new google.maps.Marker({ position: trailGPS[trailGPS.length - 1], title: "End Trail, " + trailName + ", " + trailDifficulty + ", " + trailLength + ", " + trailType + ", " + trailId, icon: imageEnd });
            tmpTrails.push(endMarker);
        }
        self.setState({ trails: tmpTrails });
        //console.log("coord: " + JSON.stringify(trailGPS));
    }

    onOverlayClick(event) {
        const google = window.google;
        let isMarker = event.overlay.getTitle !== undefined;

        if (isMarker) {
            let title = event.overlay.getTitle();
            var partsOfStr = title.split(',');
            let trailId = partsOfStr[5].trim();
            console.log("#trailId: #" + trailId + "#");
            this.infoWindow = this.infoWindow || new google.maps.InfoWindow();
            this.infoWindow.setContent('<div>' + '<h5>' + partsOfStr[1] + '</h5><h6> Location: ' + partsOfStr[0] + '</h6>'
                + '<h6> Difficulty: ' + partsOfStr[2] + '</h6>'
                + '<h6> Length: ' + partsOfStr[3] + ' m</h6>'
                + '<h6> Type: ' + partsOfStr[4] + '</h6>'
                + '<button onClick={this.test}>' + '<a href="./?trailId=' + trailId + '">' +
                'View Trail</a>' + '</button>'
                + '</div>');
            this.infoWindow.open(event.map, event.overlay);
            event.map.setCenter(event.overlay.getPosition());

            //this.growl.show({severity:'info', summary:'Marker Selected', detail: title});
        }
        else {
            //this.growl.show({severity:'info', summary:'Shape Selected', detail: ''});
        }
    }

    clearTrails() {
        global.newTrail = [];
        this.loadTrails(this, global.allTrailsResponse);
    }

    render() {
        let errorAlert = <span></span>;
        if (this.state.errorMessage !== '') {
            errorAlert = <Alert bsStyle="warning"><strong>{this.state.errorMessage}</strong></Alert>;
        }
        const google = window.google;
        const options = {
            //center: { lat: this.state.lat, lng: this.state.lng },
            center: { lat: 53.467458, lng: -2.234257 },
            zoom: 15
        };

        let updateButton = <span></span>;
        updateButton = <Button bsStyle="success" type="button" onClick={this.updateUser}>Update</Button>

        let likedTrailsArray = [];
        let allTrails = MD.user.getAppData();
        //console.log("allTr: ", allTrails.allTrails);
        for(let i = 0; i < this.state.likedTrails.length; i++){
            let link = "./?trailId=" + this.state.likedTrails[i];
            for(let j = 0; j < allTrails.allTrails.length; j++)
            if(allTrails.allTrails[j].trailId === this.state.likedTrails[i]){
                likedTrailsArray.push(<a href= {link}>{allTrails.allTrails[j].trailName} | </a> );
            }
        }
        return (
            <div>
                <ButtonToolbar>
                    <NavBar />
                </ButtonToolbar>
                <h3> User {this.state.nickname}'s Details Page </h3>
                <div style={{ textAlign: 'center' }}>
                    <Panel style={{ width: '400px', display: 'inline-block' }}>
                        <Panel.Heading>
                            {errorAlert}
                        </Panel.Heading>
                        <Panel.Body>
                            <Form horizontal >
                                <FormGroup controlId="formHorizontalUserName">
                                    <Col componentClass={ControlLabel} sm={4}>Username:</Col>
                                    <Col sm={7}>
                                        <FormControl type="email" value={this.state.username} onChange={(event) => this.setState({ username: event.target.value })} />
                                    </Col>
                                </FormGroup>
                                <FormGroup controlId="formHorizontalNickname">
                                    <Col componentClass={ControlLabel} sm={4}>Nickname:</Col>
                                    <Col sm={7}>
                                        <FormControl type="text" value={this.state.nickname} onChange={(event) => this.setState({ nickname: event.target.value })} />
                                    </Col>
                                </FormGroup>
                                <FormGroup controlId="formHorizontalRegisteredDate">
                                    <Col componentClass={ControlLabel} sm={4}>Registered On:</Col>
                                    <Col sm={7}>
                                        <FormControl type="date" value={this.state.registeredAt} disabled={true} onChange={(event) => this.setState({ registeredAt: event.target.value })} />
                                    </Col>
                                </FormGroup>
                                <FormGroup controlId="formHorizontalTotalLikes">
                                    <Col componentClass={ControlLabel} sm={4}>Total trail likes received:</Col>
                                    <Col sm={7}>
                                        <FormControl type="number" value={this.state.totalUserLikes} disabled={true} onChange={(event) => this.setState({ registeredAt: event.target.value })} />
                                    </Col>
                                </FormGroup>
                                <FormGroup controlId="formHorizontalTrailsLiked">
                                    <Col componentClass={ControlLabel} sm={4}>Liked trails:</Col>
                                    <Col sm={7}>
                                        <span>{likedTrailsArray}</span>
                                    </Col>
                                </FormGroup>
                                <FormGroup>
                                    <Col sm={10}>
                                        {updateButton}
                                    </Col>
                                </FormGroup>
                            </Form>

                        </Panel.Body>
                    </Panel>
                </div>
                <hr />
                <h3> {this.state.nickname}'s own trails: </h3>
                <div id="map">
                    <GMap id="map" overlays={this.state.trails} options={options} style={{ width: '100%', minHeight: '320px', height: '700px' }} onMapClick={this.onMapClick} onOverlayClick={this.onOverlayClick} />
                </div>
                <Footer />
            </div>
        );
    }//end render
}
export default withRouter(UserDetailsPage);