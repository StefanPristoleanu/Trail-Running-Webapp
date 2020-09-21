package apiserver.dto;

/** @author stefan */
public class UpdateTrailDTO {

  private Long trailId;

  private String trailName;

  private String trailType;

  private Integer trailDifficulty;

  private String trailDescription;

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

  public String getTrailDescription() {
    return trailDescription;
  }

  public void setTrailDescription(String trailDescription) {
    this.trailDescription = trailDescription;
  }
}
