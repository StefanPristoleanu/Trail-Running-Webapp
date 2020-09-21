package apiserver.entities;

import apiserver.controllers.TrailsController;
import apiserver.dto.GpsCoord;
import apiserver.dto.TrailDTO;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author stefan
 */
/*
 CREATE TABLE TRAILS (
     --an auto incremented field for primary key
     TRAIL_ID SERIAL,
     TRAIL_NAME VARCHAR(50) NOT NULL,
     TRAIL_CREATED_BY INTEGER, -- = USER_ID from TABLE_USERS
     TRAIL_CREATED_AT TIMESTAMP NOT NULL,
     TRAIL_TYPE VARCHAR(20), --bicycle, by foot
     TRAIL_DIFICULTY INTEGER,
     TRAIL_LENGTH INTEGER,
     TRAIL_DESCRIPTION VARCHAR(200),
     TRAIL_NO_OF_LIKES INTEGER,
     TRAIL_INCIDENTS INTEGER,
     DELTA_ELEVATION DOUBLE PRECISION,
     TRAIL_SLOPE DOUBLE PRECISION,
     DATA_JSON JSONB,
     CONSTRAINT trails_pk PRIMARY KEY (TRAIL_ID));
 CREATE INDEX TRAILS_GIN_DATA ON TRAILS USING GIN (DATA_JSON);

 ALTER TABLE trails 
 ADD CONSTRAINT owner_id_fk FOREIGN KEY (trail_created_by) REFERENCES users (user_id);
 */
@Entity
@Table(name = "TRAILS")
@XmlRootElement
public class TrailEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(columnDefinition = "serial")
  @Basic(optional = false)
  @Expose
  private Long trailId;

  @Column(name = "TRAIL_NAME")
  @Expose
  private String trailName;

  @Column(name = "TRAIL_CREATED_BY")
  @Expose
  private Long trailCreatedBy; // is the userid from USERS table

  @Column(name = "TRAIL_CREATED_AT")
  @Temporal(TemporalType.TIMESTAMP)
  @Expose
  private Date trailCreatedAt;

  @Column(name = "TRAIL_TYPE")
  @Expose
  private String trailType;

  @Column(name = "TRAIL_DIFICULTY")
  @Expose
  private Integer trailDifficulty;

  @Column(name = "TRAIL_LENGTH")
  @Expose
  private Integer trailLength;

  @Column(name = "TRAIL_DESCRIPTION")
  @Expose
  private String trailDescription;

  @Column(name = "TRAIL_NO_OF_LIKES")
  @Expose
  private Integer trailNoOfLikes;

  @Column(name = "TRAIL_INCIDENTS")
  @Expose
  private Integer trailIncidents;

  @Column(name = "DELTA_ELEVATION")
  @Expose
  private Double deltaElevation;

  @Column(name = "TRAIL_SLOPE")
  @Expose
  private Double trailSlope;

  @Column(name = "DATA_JSON")
  private String dataJSON;

  protected TrailEntity() {
  }

  public TrailEntity(TrailDTO trailDTO, Long userId) {
    this.trailName = trailDTO.getTrailName();
    this.trailCreatedBy = userId;
    this.trailCreatedAt = new java.sql.Timestamp(System.currentTimeMillis());
    this.trailType = trailDTO.getTrailType();
    if (trailDTO.getTrailDifficulty() != null) {
      this.trailDifficulty = trailDTO.getTrailDifficulty();
    } else {
      this.trailDifficulty = 1;
    }
    this.trailLength = trailDTO.getTrailLength();
    this.trailDescription = trailDTO.getTrailDescription();
    this.trailNoOfLikes = 0;
    this.deltaElevation = trailDTO.getDeltaElevation();
    this.trailSlope = trailDTO.getTrailSlope();
    this.dataJSON = TrailsController.gson.toJsonTree(trailDTO.getCoordinates()).toString();
    System.out.println("get coord: " + trailDTO.getCoordinates());
  }

  public JsonObject toJsonObject() {
    JsonObject jsonObj
            = TrailsController.gson.fromJson(TrailsController.gson.toJson(this), JsonObject.class);
    List<GpsCoord> coordList = TrailsController.gson.fromJson(this.dataJSON, List.class);
    jsonObj.add("coordinates", TrailsController.gson.toJsonTree(coordList));
    return jsonObj;
  }

  public Long getTrailId() {
    return trailId;
  }

  public void setTrailId(Long trailId) {
    this.trailId = trailId;
  }

  public String getTrailName() {
    return trailName;
  }

  public void setTrailName(String trailName) {
    this.trailName = trailName;
  }

  public Long getTrailCreatedBy() {
    return trailCreatedBy;
  }

  public void setTrailCreatedBy(Long trailCreatedBy) {
    this.trailCreatedBy = trailCreatedBy;
  }

  public Date getTrailCreatedAt() {
    return trailCreatedAt;
  }

  public void setTrailCreatedAt(Date trailCreatedAt) {
    this.trailCreatedAt = trailCreatedAt;
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

  public void setTrailDescription(String trailDescription) {
    this.trailDescription = trailDescription;
  }

  public String getTrailDescription() {
    return trailDescription;
  }

  public Integer getTrailNoOfLikes() {
    return trailNoOfLikes;
  }

  public void setTrailNoOfLikes(Integer trailNoOfLikes) {
    this.trailNoOfLikes = trailNoOfLikes;
  }

  public Integer getTrailIncidents() {
    return trailIncidents;
  }

  public void setTrailIncidents(Integer trailIncidents) {
    this.trailIncidents = trailIncidents;
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

  public String getDataJSON() {
    return dataJSON;
  }

  public void setDataJSON(String dataJSON) {
    this.dataJSON = dataJSON;
  }
}
