import React, { Component } from 'react';
import './App.css';
import 'bootstrap/dist/css/bootstrap.css';
import 'bootstrap/dist/css/bootstrap-theme.css';
import 'font-awesome/css/font-awesome.min.css';
import 'primereact/resources/themes/nova-light/theme.css';
import 'primereact/resources/primereact.min.css';
import 'primeicons/primeicons.css';
import { BrowserRouter as Router, Route, Switch, HashRouter, HashHistory } from 'react-router-dom';
// add HashRouter, HashHistory for Corodva Hybrid Mobile App
import PublicHome from './Pages/PublicHomePage.js';
import UserHome from './Pages/UserHomePage.js';
import UserDetails from './Pages/UserDetailsPage.js';
import TrailDetails from './Pages/TrailDetailsPage.js';
import TrailRecording from './Pages/TrailRecordingPage.js';
import * as MyLib from './Utils/MyLib';
import TrailOwnerPage from './Pages/TrailOwnerPage';


class App extends Component {
    constructor(props) {
        super(props);
        console.log('App start ...');
    }

    componentDidMount() {
        MyLib.getGeolocation();
    }

    myRouterSwitch() {
        return (
            <Switch>
                <Route exact path="/" component={PublicHome} />
                <Route path="/home" component={UserHome} />
                <Route path="/user-details" component={UserDetails} />
                <Route path="/recording" component={TrailRecording} />
                <Route path="/trail-details/:id" component={TrailDetails} />
                <Route path="/view-user" component={TrailOwnerPage} />
            </Switch>
        );
    }//end myRouterSwitch

    render() {

        let routerSwitch = this.myRouterSwitch();
        /*return (
            <div className="App">
                <Router history={Router.HistoryLocation} >
                    {routerSwitch}
                </Router>
            </div>
        );*/
        return (
            <div className="App">
                <HashRouter>
                    {routerSwitch}
                </HashRouter>
                }
            </div>
        );
    }//end render
}

export default App;
