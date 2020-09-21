package apiserver.dto;

import java.io.Serializable;
import java.util.List;

/** @author stefan */
public class TrailDTO implements Serializable {

  private String trailName;

  private String trailType;

  private Integer trailDifficulty;

  private Integer trailLength;

  private String trailDescription;

  private Integer trailNoOfLikes;

  private Double deltaElevation;

  private Double trailSlope;

  private List<GpsCoord> coordinates;

  public String getTrailName() {
    return trailName;
  }

  public void setTrailName(String trailName) {
    this.trailName = trailName;
  }

  public String getTrailType() {
    return trailType;
  }

  public void setTrailType(String trailType) {
    this.trailType = trailType;
  }

  public Integer getTrailDifficulty() {
    return trailDifficulty;
  }

  public void setTrailDifficulty(Integer trailDifficulty) {
    this.trailDifficulty = trailDifficulty;
  }

  public Integer getTrailLength() {
    return trailLength;
  }

  public void setTrailLength(Integer trailLength) {
    this.trailLength = trailLength;
  }

  public String getTrailDescription() {
    return trailDescription;
  }

  public void setTrailDescription(String trailDescription) {
    this.trailDescription = trailDescription;
  }

  public Integer getTrailNoOfLikes() {
    return trailNoOfLikes;
  }

  public void setTrailNoOfLikes(Integer trailNoOfLikes) {
    this.trailNoOfLikes = trailNoOfLikes;
  }

  public Double getDeltaElevation() {
    return deltaElevation;
  }

  public void setDeltaElevation(Double deltaElevation) {
    this.deltaElevation = deltaElevation;
  }

  public Double getTrailSlope() {
    return trailSlope;
  }

  public void setTrailSlope(Double trailSlope) {
    this.trailSlope = trailSlope;
  }

  public List<GpsCoord> getCoordinates() {
    return coordinates;
  }

  public void setCoordinates(List<GpsCoord> coordinates) {
    this.coordinates = coordinates;
  }
}
