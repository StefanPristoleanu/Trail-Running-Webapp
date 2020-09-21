import React from 'react';
import image1 from './image1.png';
import image2 from './image2.png';
import image3 from './image3.png';

function imageSet() {
  // Import result is the URL of your image
  let styles = {
    margin: '10px',
    width: '120px',
    height: '120px',
  };
  return <div><img style={styles} src={image1} alt="Image" /><img style={styles} src={image2} alt="Image" /><img style={styles} src={image3} alt="Image" /></div>;
}
export default imageSet;