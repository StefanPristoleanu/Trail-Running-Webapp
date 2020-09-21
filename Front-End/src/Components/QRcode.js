import React from 'react';
import logo from './QR.png'; // Tell Webpack this JS file uses this image
console.log(logo); // /logo.84287d09.png
function QR() {
  // Import result is the URL of your image
  let styles = {
    margin: '10px',
    width: '250px',
    height: '250px',
  };
  return <img style={styles} src={logo} alt="QR" />;
}
export default QR;