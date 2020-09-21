
import * as Cfg from './ConfigSettings';
import axios from 'axios';


class User {
  constructor() {
    console.log('Create User model class');
    this.userData = {}; // info about user
    this.userAuth = {}; // authentication key and datetime
    this.appData = { allTrails: [], trailUserId: 0, trailUserNickname: "", currentCoord: { lat: 0, lng: 0 } }; // info about current page and app settings
    this.isLogged = false;
    this.cTagId = 0; // Current field Tag Id - for work with html forms
    this.loadFromLocalStorage();
  }

  /**
   * loadFromLocalStorage - load userData form browser's local storage if exist
   * if not exist in local storage then init userData with undefined
   */
  loadFromLocalStorage() {
    if (localStorage.getItem('trail.appData') != null) {
      this.appData = JSON.parse(localStorage.getItem('trail.appData'));
      global.gpsLatitude = this.appData.currentCoord.lat;
      global.gpsLongitude = this.appData.currentCoord.lng;
    }
    if (localStorage.getItem('trail.userData') != null) {
      this.userData = JSON.parse(localStorage.getItem('trail.userData'));
      console.log("OK loaded userData from localStorage");
      if (localStorage.getItem('trail.userAuth') != null) {
        this.userAuth = JSON.parse(localStorage.getItem('trail.userAuth'));
        let dt = Date.now() - new Date(this.userAuth.jsLastAccessDate).getTime();
        // we accept max. 20 min of inactivity to validate the current session at reload / refresh browser:
        if (dt < Cfg.MAX_INACTIVITY_MS) {
          this.isLogged = true;
          axios.defaults.headers.common['Authorization'] = this.userAuth.authorization;
        }
      }
    }
  }//end loadFromLocalStorage

  /**
   * get user's data info from global memory/localStorage
   */
  getUserData() {
    return this.userData;
  }

  /**
   * save user's data info in global memory & localStorage
   * @param {*} userData 
   */
  setUserData(userData) {
    this.userData = userData;
    //save userData to browser's local storage:
    localStorage.setItem('trail.userData', JSON.stringify(this.userData));
  }

  /**
   * save user's auth key in global memory & localStorage
   * @param {*} auth 
   */
  setUserAuth(auth) {
    if (this.userAuth === undefined) {
      this.userAuth = {};
    }
    this.userAuth.authorization = auth;
    this.userAuth.jsLastAccessDate = Date.now();
    this.isLogged = true;
    //save userAuth to browser's local storage:
    localStorage.setItem('trail.userAuth', JSON.stringify(this.userAuth));
  }

  /**
   * save app info in global memory & localStorage
   * @param {*} appData 
   */
  setAppData(appData) {
    this.appData = appData;
    //save appData to browser's local storage:
    localStorage.setItem('trail.appData', JSON.stringify(this.appData));
  }

  /**
   * get app info from global memory/localStorage
   */
  getAppData() {
    return this.appData;
  }

  getCTagId() {
    return this.cTagId;
  }

  setCTagId(value) {
    //console.log('~~~ setCTagId: ' + value);
    this.cTagId = value;
  }

  doLogout() {
    this.isLogged = false;
    this.userData = {}; // info about user
    this.userAuth = {}; // authentication key and datetime
    //remove all user' data and auth from  browser's local storage:
    localStorage.removeItem('trail.userAuth');
    localStorage.removeItem('trail.userData');
    //localStorage.removeItem('trail.appData'); // keep appData ?
  }

  isLogged() {
    return this.isLogged;
  }
} //end class User

// export User class as a singleton:
export const user = new User();
