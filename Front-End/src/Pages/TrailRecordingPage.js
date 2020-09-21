import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import RegisterDialog from '../Components/RegisterDialog.js';
import LoginDialog from '../Components/LoginDialog.js';
import NavBar from '../Components/Navbar.js';
import Footer from '../Components/Footer.js';
import AddTrailDialog from '../Components/AddTrailDialog.js';
import { ButtonToolbar, Button, FormControl, Col, Panel, Form, FormGroup, ControlLabel, Alert } from 'react-bootstrap';
import { ToggleButton } from 'primereact/togglebutton';
import axios from 'axios';
import * as MyLib from '../Utils/MyLib';
import * as Cfg from '../Utils/ConfigSettings';
import { GMap } from 'primereact/gmap';

const TESTING = 0;
const WALKING = 5;
const RUNNING = 9;
const CYCLING = 12;

class TrailRecordingPage extends Component {
  constructor(props) {
    super(props);
    this.state = {
      lat: global.gpsLatitude,
      lng: global.gpsLongitude,
      distance: 0,
      trails: [],
      isFirstPoint: true,
      deviationRate: WALKING,
      isRecording: false,
      isSnapToRoad: false,
      errorMessage:""
    };
    this.recordCurrentLocation = this.recordCurrentLocation.bind(this);
    this.addPointOnTrail = this.addPointOnTrail.bind(this);
    this.addTrails = this.addTrails.bind(this);
    this.clearTrails = this.clearTrails.bind(this);
    this.startRecording = this.startRecording.bind(this);
    this.stopRecording = this.stopRecording.bind(this);
  }//end constructor

  componentDidMount() {
    let deviation = MyLib.distanceBetweenCoordinates(53.444584878055316, -2.2329281868442195, 53.44657854076952, -2.2195385994418757);
    console.log("~~~test: " + deviation);
  }

  componentWillUnmount() {
    if (global.gpsTimer !== undefined)
      clearInterval(global.gpsTimer);
  }

  startRecording() {
    global.gpsTimer = setInterval(this.recordCurrentLocation, 5000);
    console.log(this.state.deviationRate);
    this.setState({ isRecording: true });
  }

  stopRecording() {
    this.setState({ isRecording: false });
    clearInterval(global.gpsTimer);
  }

  async recordCurrentLocation() {
    if (this.state.isFirstPoint) {

      const google = window.google;
      var imageStart = {
        url: 'http://maps.google.com/mapfiles/kml/paddle/go.png',
        scaledSize: new google.maps.Size(40, 40),
      };
      let tmpTrails = this.state.trails;
      this.setState({ trails: [] });
      console.log("~~~coord: " + global.gpsLatitude + " " + global.gpsLongitude);
      var myMarker = new google.maps.Marker({ position: { lat: global.gpsLatitude, lng: global.gpsLongitude }, title: "Start Position", icon: imageStart, animation: google.maps.Animation.DROP });
      tmpTrails.push(myMarker);
      this.setState({ trails: tmpTrails });
      this.setState({ isFirstPoint: false });
    }
    if (global.prevLat === undefined) {
      global.prevLat = global.gpsLatitude;
      global.prevLng = global.gpsLongitude;
    }

    let diffTrail = [];
    let point1 = { lat: global.prevLat, lng: global.prevLng };
    diffTrail.push(point1);
    await MyLib.getGeolocation();
    let deviation = MyLib.distanceBetweenCoordinates(global.prevLat, global.prevLng, global.gpsLatitude, global.gpsLongitude);
    if (deviation > 0) {
      global.prevLat = global.gpsLatitude;
      global.prevLng = global.gpsLongitude;
    }
    console.log("~~~deviation: " + deviation);


    /*let point2 = { lat: global.gpsLatitude, lng: global.gpsLongitude };
    diffTrail.push(point2);

    const google = window.google;
    let myPoly = new google.maps.Polyline({ path: diffTrail, geodesic: true, strokeColor: '#FF0000', strokeOpacity: 0.5, strokeWeight: 5 });
    let trailLength = (google.maps.geometry.spherical.computeLength(myPoly.getPath()));
    console.log("###Trail leng: " + trailLength);
    */

    //Minimum deviation required to accept a new point to the current trail
    // GPS average deviation is around 3m after testing 
    if (deviation >= this.state.deviationRate) {
      this.addPointOnTrail();
    }
    this.setState({
      lat: global.gpsLatitude,
      lng: global.gpsLongitude,
      distance: deviation
    });

  }

  addPointOnTrail() {
    let tmpTrails = this.state.trails;
    this.setState({ trails: [] });
    const google = window.google;
    var imagePoint = {
      url: 'http://maps.google.com/mapfiles/kml/paddle/ylw-circle.png',
      scaledSize: new google.maps.Size(25, 25),
    };
    //event: MouseEvent of Google Maps api
    let point = { lat: global.gpsLatitude, lng: global.gpsLongitude };
    global.newTrail.push(point);
    var customMarker = new google.maps.Marker({ position: point, title: "Custom Marker", icon: imagePoint, animation: google.maps.Animation.DROP, });
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
          //window.location.reload();
          self.setState({ trails: [] });
          self.props.history.push('/user-details');
        }
      })
      .catch(function (error) {
        console.log(error);
      });
  }

  /*computeTrailIncidents() {
    var self = this;
    //let trail = "?poly=" + global.prevLat+","+global.prevLng+":"+global.gpsLatitude+","+global.gpsLongitude;
    let trail = "&lat=" + global.prevLat + "&lng=" + global.prevLng;
    //axios.get("https://data.police.uk/api/crimes-street/all-crime"+ trail)
    axios.get("https://data.police.uk/api/crimes-at-location?date=2019-01" + trail)
      .then(function (response) {
        console.log("API server response: " + JSON.stringify(response.data));
        if (response.data.responseCode < 0) {
          self.setState({ errorMessage: response.data.message });
        } else {
          //window.location.reload();
          //self.setState({ trails: [] });
          //self.props.history.push('/user-details');
          console.log("Response police: " + JSON.stringify(response));
          console.log("No crimes: " + response.data.length);
        }
      })
      .catch(function (error) {
        console.log(error);
      });
  }*/

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
    return (
      <div>
        <ButtonToolbar>
          <NavBar />
        </ButtonToolbar>
        <h3>Recording Page</h3>
        <br />
        <div style={{ textAlign: 'center' }}>
          <Panel style={{ width: '400px', display: 'inline-block' }}>
            <Panel.Heading>
            </Panel.Heading>
            <Panel.Body>
              <Form horizontal >
                <FormGroup controlId="formHorizontalUserName">
                  <Col componentClass={ControlLabel} sm={4}>Current lat:</Col>
                  <Col sm={7}>
                    <FormControl type="number" value={this.state.lat} />
                  </Col>
                </FormGroup>
                <FormGroup controlId="formHorizontalNickname">
                  <Col componentClass={ControlLabel} sm={4}>Current lng:</Col>
                  <Col sm={7}>
                    <FormControl type="number" value={this.state.lng} />
                  </Col>
                </FormGroup>
                <FormGroup controlId="formHorizontalRegisteredDate">
                  <Col componentClass={ControlLabel} sm={4}>Last sprint distance:</Col>
                  <Col sm={7}>
                    <FormControl type="number" value={this.state.distance} disabled={true} />
                  </Col>
                </FormGroup>
                <FormGroup controlId="formHorizontalRegisteredDate">
                  <Col componentClass={ControlLabel} sm={4}>Select pace:</Col>
                  <Col sm={4}>
                    <FormControl componentClass="select" placeholder="How difficult was the trail" onChange={(event) => this.setState({ deviationRate: event.target.value })}>
                      <option value={WALKING}>Walking</option>
                      <option value={RUNNING}>Running</option>
                      <option value={CYCLING}>Cycling</option>
                    </FormControl>
                  </Col>
                  <Col sm={4}>
                  <ToggleButton style={{ width: '120px' }} onLabel="Snap-To-Road" offLabel="Off-road" onIcon="pi pi-check" offIcon="pi pi-times"
                                    checked={this.state.isSnapToRoad} onChange={(e) =>  this.setState({ isSnapToRoad: e.value })} />
                  </Col>
                  
                </FormGroup>
                <ButtonToolbar>
                  <AddTrailDialog callbackFunctionAddTrails={this.addTrails} callbackFunctionClearTrails={this.clearTrails} />
                  <Button bsStyle="success" disabled={this.state.isRecording} onClick={this.startRecording}>Start Recording</Button>
                  <Button bsStyle="danger" disabled={!this.state.isRecording} onClick={this.stopRecording}>Stop Recording</Button>
                </ButtonToolbar>
              </Form>

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
export default withRouter(TrailRecordingPage);
