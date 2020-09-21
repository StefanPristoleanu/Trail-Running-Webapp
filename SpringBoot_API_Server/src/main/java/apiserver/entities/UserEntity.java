package apiserver.entities;

import apiserver.dto.RegisterDTO;
import apiserver.utils.UtilForUser;
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

/** @author stefan */

/*
CREATE TABLE USERS (
    --USER_ID VARCHAR(36) NOT NULL, --type UUID - PK
    USER_ID SERIAL,--an auto incremented field for primary key
    USERNAME VARCHAR(50) NOT NULL UNIQUE, -- user's email address
    PASSWORD VARCHAR(64) NOT NULL, -- PASSWORD: min 8 and max password chars: 24 and it will be encryped one-way SHA in 64 chars
    NICKNAME VARCHAR(50) NOT NULL,
    USER_ROLE VARCHAR(20) NOT NULL, --default all registration users are with role USER
    REGISTRATED_AT TIMESTAMP NOT NULL,
    LAST_LOGIN_AT TIMESTAMP NOT NULL,
    LIKED_TRAILS JSON,  --Using JSON instead of JSONB in order to keep the chronological order of likes
    CONSTRAINT users_pk PRIMARY KEY (USER_ID));
 */
@Entity
@Table(name = "USERS")
@XmlRootElement
public class UserEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(columnDefinition = "serial")
  @Basic(optional = false)
  private Long userId;

  @Column(name = "USERNAME")
  private String username;

  @Column(name = "PASSWORD")
  private String password;

  @Column(name = "NICKNAME")
  private String nickname;

  @Column(name = "USER_ROLE")
  private String userRole;

  @Column(name = "REGISTRATED_AT")
  @Temporal(TemporalType.TIMESTAMP)
  private Date registratedAt;

  @Column(name = "LAST_LOGIN_AT")
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastLoginAt;

  @Column(name = "LIKED_TRAILS")
  private String likedTrails;

  protected UserEntity() {}

  public UserEntity(RegisterDTO request) {
    registratedAt = new java.sql.Timestamp(System.currentTimeMillis());
    lastLoginAt = registratedAt;
    username = request.getUsername();
    password = UtilForUser.hashEncryption(request.getPassword());
    nickname = request.getNickname();
    userRole = "USER";
    likedTrails = "[]";
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public String getUserRole() {
    return userRole;
  }

  public void setUserRole(String userRole) {
    this.userRole = userRole;
  }

  public Date getRegistratedAt() {
    return registratedAt;
  }

  public void setRegistratedAt(Date registratedAt) {
    this.registratedAt = registratedAt;
  }

  public Date getLastLoginAt() {
    return lastLoginAt;
  }

  public void setLastLoginAt(Date lastLoginAt) {
    this.lastLoginAt = lastLoginAt;
  }

  public String getLikedTrails() {
    return likedTrails;
  }

  public void setLikedTrails(String likedTrails) {
    this.likedTrails = likedTrails;
  }
}
