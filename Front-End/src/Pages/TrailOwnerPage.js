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

class TrailOwnerPage extends Component {
    constructor(props) {
        super(props);
        this.state = {
            errorMessage: '',
            nickname: '',
            trails: [],
            constructedTrail: [],
            totalUserLikes: 0
        };
        this.getOwnerTrails = this.getOwnerTrails.bind(this);
        this.loadTrails = this.loadTrails.bind(this);
        this.onOverlayClick = this.onOverlayClick.bind(this);
    }//end constructor

    componentDidMount() {
        if (!MD.user.isLogged) {
            this.props.history.push('/');
        } else {
            this.getOwnerTrails();
        }
    }
    async getOwnerTrails() {
        let trailUserId = MD.user.getAppData().trailUserId;
        var self = this;
        global.allTrailsResponse = {};
        axios.get(Cfg.SERVER_URL + "trail/user-trails" + "?userId=" + trailUserId)
            .then(function (response) {
                console.log("API server response Trail Owner Trails: " + JSON.stringify(response.data));
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
        /*for(let i = 0; i < this.state.likedTrails.length; i++){
            let link = "./?trailId=" + this.state.likedTrails[i];
            for(let j = 0; j < allTrails.allTrails.length; j++)
            if(allTrails.allTrails[j].trailId === this.state.likedTrails[i]){
                likedTrailsArray.push(<a href= {link}>{allTrails.allTrails[j].trailName} | </a> );
            }
        }*/
        let trailUserNickname = MD.user.getAppData().trailUserNickname;
        return (
            <div>
                <ButtonToolbar>
                    <NavBar />
                </ButtonToolbar>
                <h3> User {trailUserNickname}'s Overview Page </h3>
                <div style={{ textAlign: 'center' }}>
                    <Panel style={{ width: '400px', display: 'inline-block' }}>
                        <Panel.Heading>
                            {errorAlert}
                        </Panel.Heading>
                        <Panel.Body>
                            <Form horizontal >
                                <FormGroup controlId="formHorizontalNickname">
                                    <Col componentClass={ControlLabel} sm={4}>Nickname:</Col>
                                    <Col sm={7}>
                                        <FormControl type="text" value={trailUserNickname} disabled={true} />
                                    </Col>
                                </FormGroup>
                                <FormGroup controlId="formHorizontalTotalLikes">
                                    <Col componentClass={ControlLabel} sm={4}>Total trail likes received:</Col>
                                    <Col sm={7}>
                                        <FormControl type="number" value={this.state.totalUserLikes} disabled={true} />
                                    </Col>
                                </FormGroup>
                                {/*<FormGroup controlId="formHorizontalTrailsLiked">
                                    <Col componentClass={ControlLabel} sm={4}>Liked trails:</Col>
                                    <Col sm={7}>
                                        <span>{likedTrailsArray}</span>
                                    </Col>
                                </FormGroup>*/}
                            </Form>

                        </Panel.Body>
                    </Panel>
                </div>
                <hr />
                <h3> {trailUserNickname}'s own trails: </h3>
                <div id="map">
                    <GMap id="map" overlays={this.state.trails} options={options} style={{ width: '100%', minHeight: '320px', height: '700px' }} onMapClick={this.onMapClick} onOverlayClick={this.onOverlayClick} />
                </div>
                <Footer />
            </div>
        );
    }//end render
}
export default withRouter(TrailOwnerPage);