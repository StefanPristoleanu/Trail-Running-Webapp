import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import { Button, ButtonToolbar, Alert, Panel } from 'react-bootstrap';
import AddTrailDialog from '../Components/AddTrailDialog.js';
import NavBar from '../Components/Navbar.js';
import Footer from '../Components/Footer.js';
import axios from 'axios';
import * as Cfg from '../Utils/ConfigSettings';
import * as MD from '../Utils/ModelData';
import { GMap } from 'primereact/gmap';
import { Growl } from 'primereact/growl';
import { InputText } from 'primereact/inputtext';
import { ToggleButton } from 'primereact/togglebutton';


// Google maps icons: http://kml4earth.appspot.com/icons.html

class UserHomePage extends Component {
    constructor(props) {
        super(props);
        this.state = {
            errorMessage: '',
            trails: [],
            constructedTrail: [],
            isEditMode: false,
            isSnapToRoad: false
        };
        this.requestGetUserData = this.requestGetUserData.bind(this);
        //this.getMyTrails = this.getMyTrails.bind(this);
        this.onMapClick = this.onMapClick.bind(this);
        this.loadTrails = this.loadTrails.bind(this);
        this.clearTrails = this.clearTrails.bind(this);
        this.addTrails = this.addTrails.bind(this);
        this.onOverlayClick = this.onOverlayClick.bind(this);
        this.goToRecordTrail = this.goToRecordTrail.bind(this);
    }//end constructor

    componentDidMount() {
        global.newTrail = [];
        this.getAllTrails();
        if (!MD.user.isLogged) {
            this.props.history.push('/');
        } else {
            this.requestGetUserData();
            global.isSnapToRoad = false;
        }
    }

    async getAllTrails() {
        var self = this;
        global.allTrailsResponse = {};
        axios.get(Cfg.SERVER_URL + "trail/find-all")
            .then(function (response) {
                console.log("API server response: " + JSON.stringify(response.data));
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
                    //self.getMyTrails();//TODO user auth token like user-details
                }
            })
            .catch(function (error) {
                console.log(error);
            });
    }//end requestGetUserData

    /*async getMyTrails() {
        var self = this;
        axios.get(Cfg.SERVER_URL + "trail/my-trails")
            .then(function (response) {
                console.log("API server response: " + JSON.stringify(response.data));
                if (response.data.responseCode < 0) {
                    self.setState({ errorMessage: response.data.message });
                } else {
                    //...
                }
            })
            .catch(function (error) {
                console.log(error);
            });
    }*/

    loadTrails(self, response) {
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
        let trailName = '', trailDifficulty = '', trailLength = '', trailType = '', trailDescription = '', trailId = '';
        let tmpTrails = [];
        for (let i = 0; i < response.data.length; i++) {
            trailGPS = response.data[i].coordinates;
            trailName = response.data[i].trailName;
            trailLength = response.data[i].trailLength;
            trailDifficulty = response.data[i].trailDifficulty;
            trailType = response.data[i].trailType;
            trailId = response.data[i].trailId;
            //console.log("trailGPS.length for "+ i + ": " + trailGPS.length + "       " +  JSON.stringify(trailGPS[0]));
            tmpTrails.push(new google.maps.Polyline({ path: trailGPS, geodesic: true, strokeColor: '#FF0000', strokeOpacity: 0.5, strokeWeight: 5 }));
            var startMarker = new google.maps.Marker({ position: trailGPS[0], title: "Start Trail, " + trailName + ", " + trailDifficulty + ", " + trailLength + ", " + trailType + ", " + trailId, icon: imageStart });
            tmpTrails.push(startMarker);
            var endMarker = new google.maps.Marker({ position: trailGPS[trailGPS.length - 1], title: "End Trail, " + trailName + ", " + trailDifficulty + ", " + trailLength + ", " + trailType + ", " + trailId, icon: imageEnd });
            tmpTrails.push(endMarker);
        }
        //Add marker for current position
        var imageMe = {
            url: 'http://maps.google.com/mapfiles/kml/paddle/blu-circle.png',
            scaledSize: new google.maps.Size(40, 40),
        };
        var myMarker = new google.maps.Marker({ position: { lat: global.gpsLatitude, lng: global.gpsLongitude }, title: "Current Position", icon: imageMe, animation: google.maps.Animation.DROP });
        tmpTrails.push(myMarker);

        self.setState({ trails: tmpTrails });
        //console.log("coord: " + JSON.stringify(trailGPS));
    }

    //this method adds new points for a trail on the map
    onMapClick(event) {
        if (!this.state.isEditMode) {
            return;
        }
        let tmpTrails = this.state.trails;
        this.setState({ trails: [] });
        const google = window.google;
        var imagePoint = {
            url: 'http://maps.google.com/mapfiles/kml/paddle/ylw-circle.png',
            scaledSize: new google.maps.Size(40, 40),
        };
        //event: MouseEvent of Google Maps api
        let gps = JSON.parse(JSON.stringify(event));
        let point = { lat: gps.latLng.lat, lng: gps.latLng.lng };
        global.newTrail.push(point);
        console.log("elem: " + tmpTrails.length + " coord: " + gps.latLng.lat + " " + gps.latLng.lng + "   " + JSON.stringify(point));
        var customMarker = new google.maps.Marker({ position: point, title: "Custom Marker", icon: imagePoint, animation: google.maps.Animation.DROP, });
        //customMarker.addListener('click', toggleBounce);

        tmpTrails.push(customMarker);
        //new google.maps.Circle({center: {lat: gps.latLng.lat, lng: gps.latLng.lng}, fillColor: '#1976D2', fillOpacity: 0.35, strokeWeight: 1, radius: 1500}));
        //let trail = this.state.constructedTrail;
        //trail.push()
        //this.setState({constructedTrail: })
        var lineSymbol = {
            path: 'M 0,-1 0,1',
            strokeOpacity: 1,
            scale: 4
        };

        tmpTrails.push(new google.maps.Polyline({
            path: global.newTrail, geodesic: true, strokeColor: '#0000FF', strokeOpacity: 0, icons: [{
                icon: lineSymbol,
                offset: '0',
                repeat: '20px'
            }],
        }));

        this.setState({ trails: tmpTrails });
    }

    onOverlayClick(event) {
        if (this.state.errorMessage != "") {
            this.state.errorMessage = "";
        }
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

    addTrails(trailJson) {
        console.log("json: " + trailJson);
        if (global.newTrail === undefined) {
            return;
        }
        if (global.newTrail.length < 2) {
            this.state.errorMessage = "Please activate Edit Mode and construct a trail.";
            return;
        }
        console.log("newtrail length: " + global.newTrail.length);
        var self = this;
        axios.post(Cfg.SERVER_URL + "trail/add", trailJson)
            .then(function (response) {
                console.log("API server response: " + JSON.stringify(response.data));
                if (response.data.responseCode < 0) {
                    self.setState({ errorMessage: response.data.message });
                } else {
                    console.log("//todo");
                    //window.location.reload();
                    self.setState({ trails: [] });
                    self.getAllTrails();
                    self.setState({ isEditMode: false });
                }
            })
            .catch(function (error) {
                console.log(error);
            });
    }

    goToRecordTrail() {
        this.props.history.push('/recording');
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

        let mapOptions = {
        };
        if (global.gpsLatitude !== undefined) {
            mapOptions = {
                center: { lat: global.gpsLatitude, lng: global.gpsLongitude },
                zoom: 15
            }
        } else {
            //Browser did not allow location sharing
            mapOptions = {
                center: { lat: 53.467458, lng: -2.234257 },
                zoom: 15
            }
        }
        global.isSnapToRoad = this.state.isSnapToRoad;
        //console.log(global.isSnapToRoad);
        return (
            <div>
                <ButtonToolbar>
                    <NavBar />
                </ButtonToolbar>
                <h3> User Home Page </h3>
                <div style={{ textAlign: 'center' }}>
                    <Panel style={{ width: '400px', display: 'inline-block' }}>
                        <Panel.Heading>
                            {errorAlert}
                        </Panel.Heading>
                        <Panel.Body>
                            <ButtonToolbar>
                                <AddTrailDialog callbackFunctionAddTrails={this.addTrails} callbackFunctionClearTrails={this.clearTrails} />
                                <Button bsStyle="success" onClick={this.goToRecordTrail}>
                                    Record Trail
                                </Button>
                                <ToggleButton style={{ width: '150px' }} onLabel="Edit Mode" offLabel="View Mode" onIcon="pi pi-check" offIcon="pi pi-times"
                                    checked={this.state.isEditMode} onChange={(e) => this.setState({ isEditMode: e.value })} />
                                <ToggleButton style={{ width: '150px' }} onLabel="Snap-To-Road" offLabel="Off-road" onIcon="pi pi-check" offIcon="pi pi-times"
                                    checked={this.state.isSnapToRoad} onChange={(e) =>  this.setState({ isSnapToRoad: e.value })} />
                            </ButtonToolbar>
                        </Panel.Body>
                    </Panel>
                </div>
                <div id="map">
                    <GMap id="map" overlays={this.state.trails} options={mapOptions} style={{ width: '100%', minHeight: '320px', height: '700px' }} onMapClick={this.onMapClick} onOverlayClick={this.onOverlayClick} />
                </div>
                <Footer />
            </div>
        );
    }//end render
}
export default withRouter(UserHomePage);