import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import RegisterDialog from '../Components/RegisterDialog.js';
import LoginDialog from '../Components/LoginDialog.js';
import NavBar from '../Components/Navbar.js';
import Footer from '../Components/Footer.js';
import { ButtonToolbar, Jumbotron } from 'react-bootstrap';
import axios from 'axios';
import * as MD from '../Utils/ModelData';
import * as Cfg from '../Utils/ConfigSettings';
import { GMap } from 'primereact/gmap';

// Google maps icons: http://kml4earth.appspot.com/icons.html

class PublicHomePage extends Component {
  constructor(props) {
    super(props);
    this.state = {
      lat: 45,
      lng: 0,
      trails: [],
    };
    this.getAllTrails = this.getAllTrails.bind(this);
    this.loadTrails = this.loadTrails.bind(this);
    this.onMapReady = this.onMapReady.bind(this);
    this.onOverlayClick = this.onOverlayClick.bind(this);
  }//end constructor

  async getAllTrails() {
    var self = this;
    axios.get(Cfg.SERVER_URL + "trail/find-all")
      .then(function (response) {
        //console.log("API server response: " + JSON.stringify(response.data));
        if (response.data.responseCode < 0) {
          self.setState({ errorMessage: response.data.message });
        } else {
          self.loadTrails(self, response);
        }
      })
      .catch(function (error) {
        console.log(error);
      });
  }
  componentDidMount() {
    //console.log(window.location.search);
    global.trailId = new URLSearchParams(window.location.search).get('trailId');
    console.log('global.trailId: ' + global.trailId);
    if(MD.user.isLogged && global.trailId !== undefined && global.trailId !== null){
      this.props.history.push('/trail-details/' + global.trailId);
    }
    //global.trailId = this.props.match.params.id;
    this.getAllTrails();
  }

  loadTrails(self, response) {
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
    let mapTrails = [];
    let trailName = '', trailDifficulty = '', trailLength = '', trailType = '', trailDescription = '', trailId = '';
    let appData = MD.user.getAppData();
    appData.allTrails = response.data;
    MD.user.setAppData(appData);
    for (let i = 0; i < response.data.length; i++) {
      trailGPS = response.data[i].coordinates;
      trailName = response.data[i].trailName;
      trailLength = response.data[i].trailLength;
      trailDifficulty = response.data[i].trailDifficulty;
      trailType = response.data[i].trailType;
      trailId = response.data[i].trailId;
      //console.log("trailGPS.length for "+ i + ": " + trailGPS.length + "       " +  JSON.stringify(trailGPS[0]));
      mapTrails.push(new google.maps.Polyline({ path: trailGPS, geodesic: true, strokeColor: '#FF0000', strokeOpacity: 0.5, strokeWeight: 5 }));
      mapTrails.push(new google.maps.Marker({ position: trailGPS[0], title: "Start Trail, " + trailName + ", " + trailDifficulty + ", " + trailLength + ", " + trailType + ", " + trailId, icon: imageStart }));
      mapTrails.push(new google.maps.Marker({ position: trailGPS[trailGPS.length - 1], title: "End Trail, " + trailName + ", " + trailDifficulty + ", " + trailLength + ", " + trailType + ", " + trailId, icon: imageEnd }));
    }
    self.setState({ trails: mapTrails });
    //console.log("coord: " + JSON.stringify(trailGPS));
  }

  onMapReady(map) {
    //map: Map instance
    console.log("pass through MapReady");
  }

  test() {
    console.log("test");
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
        //+ '<button onClick={this.test}>' + '<a href="./?trailId=' + trailId + '">' +
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

  findCurrentLocation() {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(function (position) {
        this.setState({
          lat: position.coords.latitude,
          lng: position.coords.longitude
        });
      });
    } else {
      console.log("Browser doesn't support Geolocation");
    }
  }

  render() {
    console.log("trails length: " + this.state.trails.length);
    const google = window.google;
    const options = {
      //center: { lat: this.state.lat, lng: this.state.lng },
      center: { lat: 53.467458, lng: -2.234257 },
      zoom: 15
    };

    const overlays = [
      //new google.maps.Marker({ position: this.state.coordonates[0], title: "Start Trail", icon: imageStart }),
      //new google.maps.Marker({ position: this.state.coordonates[this.state.coordonates.length - 1], title: "End Trail", icon: imageEnd }),
    ];

    return (
      <div>
        <ButtonToolbar>
          <NavBar />
        </ButtonToolbar>
        <div>
          <Jumbotron>
            <h2>Hello, running enthusiast!</h2>
            <div>
              Here you can view all of the existing trails on the website.
              <br></br>Login for more features.
            </div>
            <div>
              <LoginDialog />
            </div>
          </Jumbotron>
        </div>
        <div>
          <GMap overlays={this.state.trails} options={options} style={{ width: '100%', minHeight: '320px', height: '700px' }} onOverlayClick={this.onOverlayClick} />
        </div>
        <Footer />
      </div>

    );
  }//end render
}
export default withRouter(PublicHomePage);
