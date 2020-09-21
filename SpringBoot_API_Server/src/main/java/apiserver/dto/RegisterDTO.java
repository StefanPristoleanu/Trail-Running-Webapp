package apiserver.dto;

import java.io.Serializable;

/** @author stefan */
public class RegisterDTO extends LoginDTO implements Serializable {

  private String nickname;

  public RegisterDTO() {}

  public RegisterDTO(String username, String password, String nickname) {
    super(username, password);
    this.nickname = nickname;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }
}
