package apiserver.dto;

import com.google.gson.annotations.Expose;
import java.io.Serializable;

/** @author stefan */
public class GpsCoord implements Serializable {

  // X axis coordinate for google maps
  @Expose public double lat;

  // Y axis coordinate for google maps
  @Expose public double lng;
}
