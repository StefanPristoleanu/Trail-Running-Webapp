import * as MD from '../Utils/ModelData';
import axios from 'axios';

function successGeolocation(position) {
  console.log('~~~ init App current position: ' + position.coords.latitude + ', ' + position.coords.longitude);
  global.gpsLatitude = position.coords.latitude;
  global.gpsLongitude = position.coords.longitude;
  let appData = MD.user.getAppData();
  appData.currentCoord.lat = global.gpsLatitude;
  appData.currentCoord.lng = global.gpsLongitude;
  MD.user.setAppData(appData);
}

function errorGeolocation(err) {
  console.warn(`ERROR(${err.code}): ${err.message}`);
}

export function getGeolocation() {
  var options = {
    enableHighAccuracy: true,
    timeout: 5000,
    maximumAge: 0
  };
  // Try HTML5 geolocation:
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(successGeolocation,
      errorGeolocation, options);
  } else {
    // Browser doesn't support Geolocation
    //handleLocationError(false, infoWindow, map.getCenter());
    console.log('Browser doesn\'t support Geolocation!');
  }
}

function degreesToRadians(degrees) {
  return degrees * Math.PI / 180;
}

export function distanceBetweenCoordinates(lat1, lon1, lat2, lon2) {
  console.log("distanceBetweenCoordinates: " + lat1 + ", " + lon1 + " and " + lat2 + ", " + lon2);
  var earthRadius = 6371000;

  var dLat = degreesToRadians(lat2 - lat1);
  var dLon = degreesToRadians(lon2 - lon1);

  lat1 = degreesToRadians(lat1);
  lat2 = degreesToRadians(lat2);

  var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
  var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return earthRadius * c;
}

export function validateEmail(email) {
  var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
  return re.test(String(email).toLowerCase());
}

export function validatePassword(inputPassword) {
  //var password = /^[A-Za-z0-9]\w{7,15}$/;
  //return password.test(String(inputPassword).toLowerCase());
  var re = /(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{6,}/;
  return re.test(inputPassword);
}


// Snap a user-created polyline to roads and draw the snapped path
//https://roads.googleapis.com/v1/snapToRoads?path=-35.27801,149.12958|-35.28032,149.12907|-35.28099,149.12929|-35.28144,149.12984|-35.28194,149.13003|-35.28282,149.12956|-35.28302,149.12881|-35.28473,149.12836&interpolate=true&key=YOUR_API_KEY
export async function runSnapToRoad(path) {
  const google = window.google;
  let pathValues = "";
  for (let i = 0; i < path.length; i++) {
    pathValues += path[i].lat + "," + path[i].lng + "|";
  }
  pathValues = pathValues.substring(0, pathValues.length - 1);
  pathValues += "&interpolate=true&key=AIzaSyCdfU7kT27TmWiEAYDmalLIcebbeBe5M_E";
 // console.log("New path: " +  pathValues);
  await axios.get("https://roads.googleapis.com/v1/snapToRoads?path=" + pathValues)
  .then(await function (response) {
    //console.log("New path " + JSON.stringify(response));
    let point;
    global.newTrailSnapToRoad = [];
    for(let i = 0; i < response.data.snappedPoints.length; i++) {
      //console.log(JSON.stringify(response.data.snappedPoints[i]) + "/n");
      point = { lat: response.data.snappedPoints[i].location.latitude, lng: response.data.snappedPoints[i].location.longitude };
      global.newTrailSnapToRoad.push(point);
    }
    //console.log("snapped: " + JSON.stringify(global.newTrailSnapToRoad));
  })
  .catch(function (error) {
    console.log(error);
  });
}
