package apiserver.postgresql_rest.entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import org.json.JSONObject;

/** @author Stefan */

/*
SQL:
CREATE TABLE DB_TABLE_1 (
    ID SERIAL,
    DATA_JSON JSONB,
    CONSTRAINT db_table_pk PRIMARY KEY (ID));
CREATE INDEX IDX_GIN_DATA ON DB_TABLE_1 USING GIN (DATA_JSON);
 */
@Entity
@Table(name = "DB_TABLE_1")
@XmlRootElement
public class DbTable1 implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(columnDefinition = "serial")
  @Basic(optional = false)
  private Long id;

  private String dataJson; // a JSONObject in format:

  /*
       {
  "id": "1",
  "name": "n10",
  "dataList": [10, 20, 30, 41],
  "createdAt": "Tue Nov 06 23:11:10 EET 2018",
  "lastUpdate": "Tue Nov 06 23:15:32 EET 2018",
  "counterUpdates": 0
      }
      */
  protected DbTable1() {}

  public DbTable1(JSONObject jsonObj) {
    Date d = new java.sql.Timestamp(System.currentTimeMillis());
    jsonObj.put("createdAt", d);
    jsonObj.put("lastUpdatedAt", d);
    jsonObj.put("counterUpdates", 0);
    dataJson = jsonObj.toString();
  }

  public void update(JSONObject jsonRequest) {
    JSONObject dataJsonObj = new JSONObject(this.dataJson);
    int counter = dataJsonObj.getInt("counterUpdates");
    dataJsonObj.put("counterUpdates", counter + 1);
    dataJsonObj.put("lastUpdatedAt", new java.sql.Timestamp(System.currentTimeMillis()));
    dataJsonObj.put("name", jsonRequest.get("name"));
    dataJsonObj.put("dataList", jsonRequest.getJSONArray("dataList"));
    dataJson = dataJsonObj.toString();
  }

  /** @return the id */
  public Long getId() {
    return id;
  }

  /** @param id the id to set */
  public void setId(Long id) {
    this.id = id;
  }

  public JSONObject toJSONObject() {
    JSONObject jsonObj = new JSONObject();
    jsonObj.put("id", this.id);
    jsonObj.put("dataJson", new JSONObject(this.dataJson));
    return jsonObj;
  }

  // [...]
  @Override
  public String toString() {
    return String.format("DbTable1[id=%s, data_json='%s']", id, dataJson);
  }

  /** @return the dataJson */
  public String getDataJson() {
    return dataJson;
  }

  /** @param dataJson the dataJson to set */
  public void setDataJson(String dataJson) {
    this.dataJson = dataJson;
  }
}
