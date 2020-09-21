import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import RegisterDialog from '../Components/RegisterDialog.js';
import LoginDialog from '../Components/LoginDialog.js';
import NavBar from '../Components/Navbar.js';
import Footer from '../Components/Footer.js';
import { ButtonToolbar, Jumbotron, Button, Modal, Panel, Form, FormControl, FormGroup, Col, ControlLabel, Alert } from 'react-bootstrap';
import axios from 'axios';
import * as Cfg from '../Utils/ConfigSettings';
import * as MD from '../Utils/ModelData';
import { GMap } from 'primereact/gmap';

// Google maps icons: http://kml4earth.appspot.com/icons.html

class TrailDetailsPage extends Component {
  constructor(props) {
    super(props);
    this.state = {
      errorMessage: '',
      lat: 45,
      lng: 0,
      trails: [],
      trailId: 0,
      trailName: '', trailDifficulty: '', trailLength: 0, trailType: '', trailDescription: '', trailLikes: 0, trailCreatedAt: '', trailCreatedBy: 0,
      trailIncidents: 0, deltaElevation: 0, trailSlope: 0,
      ownerOfTrail: false,
      showPopup: false,
      center: { lat: 53.467458, lng: -2.234257 },
      likedTrails: [],
      //enableLike: true
    };
    this.getTrailDetails = this.getTrailDetails.bind(this);
    this.displayTrail = this.displayTrail.bind(this);
    this.requestGetUserData = this.requestGetUserData.bind(this);
    this.generateActions = this.generateActions.bind(this);
    this.openPopup = this.openPopup.bind(this);
    this.closePopup = this.closePopup.bind(this);
    this.deleteTrail = this.deleteTrail.bind(this);
    this.updateTrail = this.updateTrail.bind(this);
    this.updateTrail = this.updateTrail.bind(this);
    this.likeTrail = this.likeTrail.bind(this);
    this.goToUser = this.goToUser.bind(this);
  }//end constructor

  componentDidMount() {
    global.trailId = this.props.match.params.id;
    if (!MD.user.isLogged) {
      this.props.history.push('/');
    } else {
      this.getTrailDetails();
    }
  }

  openPopup() { this.setState({ showPopup: true }); }

  closePopup() { this.setState({ showPopup: false }); }

  getTrailDetails() {
    var self = this;
    axios.get(Cfg.SERVER_URL + "trail/trail-details" + "?trailId=" + this.props.match.params.id)
      .then(function (response) {
        console.log("API server response: " + JSON.stringify(response.data));
        if (response.data.responseCode < 0) {
          self.setState({ errorMessage: response.data.message });
        } else {
          console.log("Trail details: " + response);
          self.displayTrail(self, response);
          self.generateActions();
        }
      })
      .catch(function (error) {
        console.log(error);
      });
  }

  async generateActions() {
    //if (MD.user.getUserData === undefined) {
    await this.requestGetUserData();
    //}
    if (MD.user.getUserData().userId === this.state.trailCreatedBy) {
      this.setState({ ownerOfTrail: true });
    }
    console.log("userID: " + MD.user.getUserData().userId + " ? " + this.state.trailCreatedBy);
  }

  displayTrail(self, response) {
    console.log("displayTrail" + " " + JSON.stringify(response));
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
    trailGPS = response.data.coordinates;
    console.log("\n coord: " + trailGPS);
    trailName = response.data.trailName;
    trailLength = response.data.trailLength;
    trailDifficulty = response.data.trailDifficulty;
    trailType = response.data.trailType;
    trailId = response.data.trailId;
    let appData = MD.user.getAppData();
    appData.trailUserId = response.data.trailCreatedBy;
    appData.trailUserNickname = response.data.nickname;
    MD.user.setAppData(appData);
    this.setState({
      trailName: trailName,
      trailCreatedBy: response.data.trailCreatedBy,
      trailLength: trailLength,
      trailDifficulty: trailDescription,
      trailType: trailType,
      trailDescription: response.data.trailDescription,
      trailLikes: response.data.trailNoOfLikes,
      trailCreatedAt: response.data.trailCreatedAt,
      trailIncidents: response.data.trailIncidents,
      deltaElevation: response.data.deltaElevation,
      trailSlope: response.data.trailSlope
    });
    tmpTrails.push(new google.maps.Polyline({ path: trailGPS, geodesic: true, strokeColor: '#0000FF', strokeOpacity: 0.5, strokeWeight: 5 }));
    var startMarker = new google.maps.Marker({ position: trailGPS[0], title: "Start Trail, " + trailName + ", " + trailDifficulty + ", " + trailLength + ", " + trailType + ", " + trailId, icon: imageStart });
    tmpTrails.push(startMarker);
    var endMarker = new google.maps.Marker({ position: trailGPS[trailGPS.length - 1], title: "End Trail, " + trailName + ", " + trailDifficulty + ", " + trailLength + ", " + trailType + ", " + trailId, icon: imageEnd });
    tmpTrails.push(endMarker);
    var latLng = new google.maps.LatLng(trailGPS[0].lat, trailGPS[0].lng);
    this.gmap.getMap().panTo(latLng);
    let trailCenter = { lat: trailGPS[0].lat, lng: trailGPS[0].lng };
    self.setState({ trails: tmpTrails, center: trailCenter });
    console.log("Final trails len: " + tmpTrails.length);
  }

  async deleteTrail() {
    var self = this;
    let json = { trailId: global.trailId };
    axios.post(Cfg.SERVER_URL + "trail/delete", json)
      .then(function (response) {
        console.log("API server response: " + JSON.stringify(response.data));
        if (response.data.responseCode < 0) {
          self.setState({ errorMessage: response.data.message });
        } else {
          self.setState({ showPopup: false });
          //self.closePopup();
          self.props.history.push('/user-details');
        }
      })
      .catch(function (error) {
        console.log(error);
      });
  }

  updateTrail() {
    if (this.state.trailName.trim() === '') {
      this.setState({ errorMessage: 'A trail name is required!' });
      return;
    }

    let json = {
      trailId: global.trailId,
      trailName: this.state.trailName,
      trailDifficulty: 1,//this.state.trailDifficulty,
      trailType: this.state.trailType,
      trailDescription: this.state.trailDescription,
      trailLikes: this.state.trailLikes
    };
    var self = this;
    axios.post(Cfg.SERVER_URL + "trail/update", json)
      .then(function (response) {
        console.log("API server response: " + JSON.stringify(response.data));
        if (response.data.responseCode < 0) {
          self.setState({ errorMessage: response.data.message });
        } else {
          //window.location.reload();
          self.setState({ errorMessage: 'Successfully updated trail!' });
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
          self.setState({ likedTrails: response.data.likedTrails });
          /*if(response.data.likedTrails.includes(global.trailId)){
            self.setState({ enableLike: false });
          }*/
          console.log("likedTrails: " + self.state.likedTrails);
        }
      })
      .catch(function (error) {
        console.log(error);
      });
  }//end requestGetUserData

  likeTrail() {
    if (this.state.likedTrails.includes(Number(global.trailId))) {
      this.setState({ errorMessage: "You have already liked this trail!" });
      return;
    }
    var self = this;
    let json = { trailId: global.trailId };
    axios.post(Cfg.SERVER_URL + "trail/like", json)
      .then(function (response) {
        console.log("API server response: " + JSON.stringify(response.data));
        if (response.data.responseCode < 0) {
          self.setState({ errorMessage: response.data.message });
        } else {
          let tLikes = self.state.trailLikes + 1;
          self.setState({ errorMessage: "Like Success", trailLikes: tLikes });

          //self.closePopup();
          //self.props.history.push('/user-details');
        }
      })
      .catch(function (error) {
        console.log(error);
      });
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
        + '<button onClick={this.test}>' + '<a href="./trail-details/' + trailId + '">' +
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

  goToUser() {
    this.props.history.push('/view-user');
  }

  render() {
    let errorAlert = <span></span>;
    if (this.state.errorMessage !== '') {
      errorAlert = <Alert bsStyle="warning"><strong>{this.state.errorMessage}</strong></Alert>;
    }

    const google = window.google;
    const options = {
      //center: { lat: this.state.lat, lng: this.state.lng },
      center: this.state.center,
      zoom: 15
    };

    const overlays = [
      //new google.maps.Marker({ position: this.state.coordonates[0], title: "Start Trail", icon: imageStart }),
      //new google.maps.Marker({ position: this.state.coordonates[this.state.coordonates.length - 1], title: "End Trail", icon: imageEnd }),
    ];

    let delButton = <span></span>;
    let updateButton = <span></span>;
    let likeButton = <span></span>;
    let slope = "";
    if (this.state.ownerOfTrail) {
      delButton = <Button bsStyle="danger" onClick={this.openPopup} style={{ width: '93px' }}> Delete Trail </Button>
      updateButton = <Button bsStyle="success" type="button" onClick={this.updateTrail} style={{ width: '93px' }}>Update</Button>
    }
    likeButton = <Button bsStyle="success" onClick={() => this.likeTrail()} style={{ width: '93px' }}> Like Trail </Button>
    if(this.state.trailSlope > 0){
      slope = "Uphill"
    }
    else{
      slope = "Downhill"
    }
    return (
      <div>
        <ButtonToolbar>
          <NavBar />
        </ButtonToolbar>
        <div>
          <div>
            <h2>Trail - {this.state.trailName} Details </h2>
            <div style={{ textAlign: 'center' }}>
              <Panel style={{ width: '400px', display: 'inline-block' }}>
                <Panel.Heading>
                  {errorAlert}
                </Panel.Heading>
                <Panel.Body>

                  <Form horizontal >
                    <FormGroup controlId="formHorizontalTrailName">
                      <Col componentClass={ControlLabel} sm={4}>Trail Name:</Col>
                      <Col sm={7}>
                        <FormControl type="text" value={this.state.trailName} disabled={!this.state.ownerOfTrail} onChange={(event) => this.setState({ trailName: event.target.value })} />
                      </Col>
                    </FormGroup>
                    <FormGroup controlId="formHorizontalTrailDifficulty">
                      <Col componentClass={ControlLabel} sm={4}>Difficulty:</Col>
                      <Col sm={7}>
                        <FormControl componentClass="select" disabled={!this.state.ownerOfTrail} value={this.state.trailDifficulty} onChange={(event) => this.setState({ trailDifficulty: event.target.value })}>
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
                        <FormControl componentClass="select" disabled={!this.state.ownerOfTrail} value={this.state.trailType} onChange={(event) => this.setState({ trailType: event.target.value })}>
                          <option value="walking">Walking</option>
                          <option value="running">Running</option>
                          <option value="cycling">Cycling</option>
                        </FormControl>
                      </Col>
                    </FormGroup>
                    <FormGroup controlId="formHorizontalTrailDescription">
                      <Col componentClass={ControlLabel} sm={4}>Description:</Col>
                      <Col sm={7}>
                        <FormControl componentClass="textarea" type="text" disabled={!this.state.ownerOfTrail} value={this.state.trailDescription} onChange={(event) => this.setState({ trailDescription: event.target.value })} />
                      </Col>
                    </FormGroup>
                    <FormGroup controlId="formHorizontalTrailLikes">
                      <Col componentClass={ControlLabel} sm={4}>Likes:</Col>
                      <Col sm={7}>
                        <b><FormControl disabled={true} value={this.state.trailLikes} /></b>
                      </Col>
                    </FormGroup>
                    <FormGroup controlId="formHorizontalTrailOwner">
                      <Col componentClass={ControlLabel} sm={4}>Creator:</Col>
                      <Col sm={4}>
                        <b><FormControl disabled={true} value={MD.user.getAppData().trailUserNickname} /></b>
                      </Col>
                      <Col sm={2}>
                        <Button bsStyle="primary" type="button" onClick={this.goToUser} style={{ width: '93px' }}>View User</Button>
                      </Col>
                    </FormGroup>
                    <FormGroup controlId="formHorizontalTrailIncidents">
                      <Col componentClass={ControlLabel} sm={4}>Number of incidents:</Col>
                      <Col sm={4}>
                        <b><FormControl disabled={true} value={this.state.trailIncidents} /></b>
                      </Col>
                      <Col sm={2}>
                        <Button bsStyle="warning" type="button" style={{ width: '93px' }}><a href="https://data.police.uk/docs/method/crimes-at-location/" target="police">View Details</a></Button>
                      </Col>
                    </FormGroup>
                    <FormGroup controlId="formHorizontalTrailSlope">
                      <Col componentClass={ControlLabel} sm={4}>TrailSlope:</Col>
                      <Col sm={7}>
                        <b><FormControl disabled={true} value={this.state.trailSlope} /></b>
                      </Col>
                    </FormGroup>
                    <FormGroup controlId="formHorizontalTrailElevation">
                    <Col componentClass={ControlLabel} sm={4}>Maximum elevation:</Col>
                      <Col sm={4}>
                      <b><FormControl disabled={true} value={this.state.deltaElevation} /></b>
                      </Col>
                      <Col sm={3}>
                      <b><FormControl disabled={true} value={slope} /></b>
                      </Col>
                    </FormGroup>
                    <FormGroup>
                      <Col sm={4}>
                        {likeButton}
                      </Col>

                      <Col sm={4}>
                        {delButton}
                      </Col>
                      <Col sm={4}>
                        {updateButton}
                      </Col>
                    </FormGroup>
                  </Form>

                </Panel.Body>
              </Panel>
              <ButtonToolbar>

              </ButtonToolbar>
              <Modal show={this.state.showPopup} onHide={this.closePopup}>
                <Modal.Header closeButton>
                  <Modal.Title>Delete</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                  <div>Please confirm deleting trail: {this.state.trailName}</div>
                </Modal.Body>
                <Modal.Footer>
                  <Button bsStyle="danger" type="button" onClick={this.deleteTrail}>Delete</Button>
                  <Button bsStyle="primary" type="button" onClick={this.closePopup}>Cancel</Button>
                </Modal.Footer>
              </Modal>
            </div>
          </div>
        </div>
        <div>
          <GMap ref={(el) => this.gmap = el} overlays={this.state.trails} options={options} style={{ width: '100%', minHeight: '320px', height: '500px' }} onOverlayClick={this.onOverlayClick} />
        </div>
        <Footer />
      </div>

    );
  }//end render
}
export default withRouter(TrailDetailsPage);
