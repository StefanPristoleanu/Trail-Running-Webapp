package apiserver.mongodb_rest.entities;

import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.springframework.data.annotation.Id;

/** @author */
public class DbTable1 {

  @Id private String id; // UUID automatically updated by MongoDB - we will create only getId method
  private String name;
  private Date createdAt;
  private Date lastUpdatedAt;
  private long counterUpdates;
  private long trailCreatedBy; // is the userid from USERS table
  private List<Object> dataList; // a JSONArray with any data

  protected DbTable1() {}

  public DbTable1(String name, JSONArray jsonArray, long ownerId) {
    this.name = name;
    this.createdAt = new java.sql.Timestamp(System.currentTimeMillis());
    this.lastUpdatedAt = this.createdAt;
    this.counterUpdates = 0;
    this.dataList = jsonArray.toList();
    this.trailCreatedBy = ownerId;
  }

  public void update(String name, JSONArray jsonArray) {
    this.name = name;
    this.lastUpdatedAt = new java.sql.Timestamp(System.currentTimeMillis());
    this.counterUpdates += 1;
    this.dataList = jsonArray.toList();
  }

  // [...]

  @Override
  public String toString() {
    return String.format(
        "DbTable1[id=%s, name='%s',lastUpdatedAt='%s', counterUpdates='%s' ]",
        id, name, lastUpdatedAt, counterUpdates);
  }

  /** @return the id */
  public String getId() {
    return id;
  }

  /** @return the name */
  public String getName() {
    return name;
  }

  /** @param name the name to set */
  public void setName(String name) {
    this.name = name;
  }

  /** @return the lastUpdateAt */
  public Date getLastUpdate() {
    return lastUpdatedAt;
  }

  /** @param lastUpdate the lastUpdateAt to set */
  public void setLastUpdate(Date lastUpdate) {
    this.lastUpdatedAt = lastUpdate;
  }

  /** @return the counterUpdates */
  public long getUpdateCounter() {
    return counterUpdates;
  }

  /** @param updateCounter the counterUpdates to set */
  public void setUpdateCounter(long updateCounter) {
    this.counterUpdates = updateCounter;
  }

  /** @return the dataList */
  public List<Object> getDataList() {
    return dataList;
  }

  /** @param dataList the dataList to set */
  public void setDataList(List<Object> dataList) {
    this.dataList = dataList;
  }

  /** @return the createdAt */
  public Date getCreatedAt() {
    return createdAt;
  }

  /** @param createdAt the createdAt to set */
  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public long getTrailCreatedBy() {
    return trailCreatedBy;
  }

  public void setTrailCreatedBy(long trailCreatedBy) {
    this.trailCreatedBy = trailCreatedBy;
  }
}
