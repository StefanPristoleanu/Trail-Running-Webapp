package apiserver.postgresql_rest.entities;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import java.io.Serializable;
import java.util.Date;
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

/** @author */

/*
SQL:
CREATE TABLE DB_TABLE_1 (
    ID SERIAL,
    DATA_JSON JSONB,
    CONSTRAINT db_table_pk PRIMARY KEY (ID));
CREATE INDEX IDX_GIN_DATA ON DB_TABLE_1 USING GIN (DATA_JSON);
 */
/*
CREATE TABLE DB_TABLE_2 (
    ID SERIAL,
    NAME varchar(100) NOT NULL,
    DATA_JSON JSONB,
    CREATED_AT TIMESTAMP,
    LAST_UPDATED_AT TIMESTAMP,
    updates
    CONSTRAINT db_table_pk2 PRIMARY KEY (ID));
CREATE INDEX IDX_GIN_DATA2 ON DB_TABLE_2 USING GIN (DATA_JSON);
CREATE INDEX IDX_NAME2 ON DB_TABLE_2(NAME, LAST_UPDATED_AT desc);
*/
@Entity
@Table(name = "DB_TABLE_2")
@XmlRootElement
public class MyTableModel implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(columnDefinition = "serial")
  @Basic(optional = false)
  private Long id;

  @Column(name = "NAME")
  private String name;

  @Column(name = "created_at")
  @Temporal(TemporalType.TIMESTAMP)
  private Date createdAt;

  @Column(name = "last_updated_at")
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastUpdatedAt;

  @Column(name = "TRAIL_CREATED_BY")
  @Expose
  private Long trailCreatedBy; // is the userid from USERS table

  private String dataJson; // a JSONObject in format:

  /*
  {
  "id": "1",
  "name": "n10",
  "dataList": [10, 20, 30, 41],
  "createdAt": "Tue Nov 06 23:11:10 EET 2018",
  "lastUpdatedAt": "Tue Nov 06 23:15:32 EET 2018",
  "counterUpdates": 0
   }
   */
  protected MyTableModel() {}

  public MyTableModel(JsonObject jsonObj) {
    // String strDate = new java.sql.Timestamp(System.currentTimeMillis()) + "";
    // jsonObj.addProperty("createdAt", strDate);
    // jsonObj.addProperty("lastUpdatedAt", strDate);
    createdAt = new java.sql.Timestamp(System.currentTimeMillis());
    lastUpdatedAt = createdAt;
    name = jsonObj.get("name").getAsString();
    jsonObj.addProperty("counterUpdates", 0);
    dataJson = jsonObj.toString();
    trailCreatedBy = jsonObj.get("ownerId").getAsLong();
  }

  public void update(Gson gson, JsonObject jsonRequest) {
    // JsonObject dataJsonObj = (new JsonParser()).parse(this.dataJson).getAsJsonObject();
    JsonObject dataJsonObj = gson.fromJson(this.dataJson, JsonObject.class);
    int counter = dataJsonObj.get("counterUpdates").getAsInt();
    dataJsonObj.addProperty("counterUpdates", counter + 1);
    /*dataJsonObj.addProperty(
    "lastUpdatedAt", new java.sql.Timestamp(System.currentTimeMillis()) + "");*/
    lastUpdatedAt = new java.sql.Timestamp(System.currentTimeMillis());
    // dataJsonObj.addProperty("name", jsonRequest.get("name").getAsString());
    name = jsonRequest.get("name").getAsString();
    dataJsonObj.add("dataList", jsonRequest.getAsJsonArray("dataList"));
    dataJson = dataJsonObj.toString();
  }

  public JsonObject toJSONObject() {
    JsonObject jsonObj = new JsonObject();
    jsonObj.addProperty("id", this.id);
    jsonObj.addProperty("name", this.name);
    jsonObj.add("dataJson", (new JsonParser()).parse(this.dataJson).getAsJsonObject());
    jsonObj.addProperty("lastUpdatedAt", this.lastUpdatedAt + "");
    return jsonObj;
  }

  @Override
  public String toString() {
    return String.format("DbTable1[id=%s, data_json='%s']", id, dataJson);
  }

  /** @return the id */
  public Long getId() {
    return id;
  }

  /** @param id the id to set */
  public void setId(Long id) {
    this.id = id;
  }

  /** @return the dataJson */
  public String getDataJson() {
    return dataJson;
  }

  /** @param dataJson the dataJson to set */
  public void setDataJson(String dataJson) {
    this.dataJson = dataJson;
  }

  /** @return the createdAt */
  public Date getCreatedAt() {
    return createdAt;
  }

  /** @param createdAt the createdAt to set */
  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  /** @return the lastUpdatedAt */
  public Date getLastUpdatedAt() {
    return lastUpdatedAt;
  }

  /** @param lastUpdate the lastUpdatedAt to set */
  /** @param lastUpdatedAt the lastUpdate to set */
  public void setLastUpdatedAt(Date lastUpdatedAt) {
    this.lastUpdatedAt = lastUpdatedAt;
  }

  /** @return the name */
  public String getName() {
    return name;
  }

  /** @param name the name to set */
  public void setName(String name) {
    this.name = name;
  }

  public Long getTrailCreatedBy() {
    return trailCreatedBy;
  }

  public void setTrailCreatedBy(Long trailCreatedBy) {
    this.trailCreatedBy = trailCreatedBy;
  }
}
