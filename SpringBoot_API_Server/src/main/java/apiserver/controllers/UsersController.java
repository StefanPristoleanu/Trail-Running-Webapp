package apiserver.controllers;

import apiserver.dto.LoginDTO;
import apiserver.dto.RegisterDTO;
import apiserver.dto.UpdateUserDTO;
import apiserver.entities.UserEntity;
import apiserver.entities.UsersRepository;
import apiserver.security.JWTAuthService;
import apiserver.utils.UtilForUser;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** @author Stefan */
@Api(tags = {"User management"})
@RestController
@RequestMapping("/api-server/user")
public class UsersController {

  private static final Logger LOG = Logger.getLogger(UsersController.class.getName());
  // https://google.github.io/gson/apidocs/com/google/gson/Gson.html
  // Gson instances are Thread-safe so you can reuse them freely across multiple threads:
  private Gson gson;

  @Autowired private UsersRepository usersRep;

  // ToDo: add here @Autowired private UserRepository userRepository;
  @PostConstruct
  public void init() {
    gson = new Gson(); // we will pass this instance for all api method
  }

  @ExceptionHandler
  void handleIllegalArgumentException(IllegalArgumentException e, HttpServletResponse response)
      throws IOException {
    response.sendError(HttpStatus.BAD_REQUEST.value());
  }

  // alias curlj='curl --header "Content-Type: application/json" -w "\n" -v'
  // curlj http://localhost:9090/api-server/user/register -d  '{"username":"s1", "password":"123",
  // "nickname":"test"}'
  @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> register(@RequestBody RegisterDTO request) {
    try {
      UserEntity newRec = new UserEntity(request);
      usersRep.save(newRec);
      JsonObject jsonResponse = new JsonObject();
      jsonResponse.addProperty("responseCode", 0);
      jsonResponse.addProperty("message", "OK register: " + request.getUsername());
      return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
    } catch (JsonSyntaxException ex) {
      LOG.severe(ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end register

  @ApiOperation(
      value = "Update an existing user",
      authorizations = {@Authorization(value = "Bearer")})
  @PostMapping(value = "/update", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> updateUser(@RequestBody UpdateUserDTO userDTO) {
    try {
      Long userId = 0L;
      try {
        Authentication auth =
            SecurityContextHolder.getContext().getAuthentication(); // TODO make a method
        String username = auth.getPrincipal().toString();
        String userRole = auth.getAuthorities().toArray()[0].toString();
        userId = Long.parseLong(auth.getAuthorities().toArray()[1].toString()); // method ends here
      } catch (Exception e) {
        LOG.severe(e.toString());
      }
      Optional<UserEntity> myUser = usersRep.findById(userDTO.getUserId()); // findByUserId(userId);
      if (!myUser.isPresent()) {
        return new ResponseEntity("User not found", HttpStatus.NOT_FOUND);
      }
      myUser.get().setUsername(userDTO.getUsername());
      myUser.get().setNickname(userDTO.getNickname());
      usersRep.save(myUser.get());
      JsonObject jsonResponse = new JsonObject();
      jsonResponse.addProperty("responseCode", 0);
      jsonResponse.addProperty("message", "OK updated user: " + myUser.get().getUsername());
      return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
    } catch (JsonSyntaxException ex) {
      LOG.severe(ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end updateUser

  // curlj http://localhost:9090/api-server/user/login -d  '{"username":"s1", "password":"123"}'
  /*
  < HTTP/1.1 200
  < Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuMSIsImV4cCI6MTU0Mjc0MTQwN30.aZVfYmCdNWu07UFoBN5hFt_q7r_b4vYYwFmmg1l2YHLRuwwS8LwLfot073g_ag7TI6uVq9z1jYhk3LbI5rhoTg
  < X-Content-Type-Options: nosniff
  < X-XSS-Protection: 1; mode=block
  < Cache-Control: no-cache, no-store, max-age=0, must-revalidate
  < Pragma: no-cache
  < Expires: 0
  < X-Frame-Options: DENY
  < Content-Type: application/json;charset=UTF-8
  < Content-Length: 43
  < Date: Tue, 20 Nov 2018 19:11:47 GMT
  <
  * Connection #0 to host localhost left intact
  {"responseCode":0,"message":"OK login: n1"}
     */
  // Note: the auth JWT is added in the header's reponse
  @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> login(@RequestBody LoginDTO request) {
    try {
      UserEntity user = getUser(request.getUsername());
      if (user == null) {
        return new ResponseEntity("incorrect username or password", HttpStatus.BAD_REQUEST);
      }
      String passMD5 = UtilForUser.hashEncryption(request.getPassword());
      if (!user.getPassword().equals(passMD5)) {
        return new ResponseEntity("incorrect username or password", HttpStatus.BAD_REQUEST);
      }
      JsonObject jsonResponse = new JsonObject();
      jsonResponse.addProperty("responseCode", 0);
      jsonResponse.addProperty("message", "OK login: " + user.getNickname());
      HttpHeaders headers = new HttpHeaders();
      headers.add(
          HttpHeaders.AUTHORIZATION,
          JWTAuthService.generateAuthentication(
              user.getUsername(), user.getUserRole(), user.getUserId()));
      return new ResponseEntity(jsonResponse.toString(), headers, HttpStatus.OK);
    } catch (JsonSyntaxException ex) {
      LOG.severe(ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end login

  /*
  curlj http://localhost:9090/api-server/user/details
  curl -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzMSIsInJvbGUiOiJVU0VSIiwidXNlcklkIjoiMTAiLCJleHAiOjE1NTI1ODgxODJ9.Cas-aFImJrAUEutHD-YecEhmiBptkNwyem694vn-DjjYgV6-_tPMg02o908tQeW7-JdPX7CuMvTfpY_MkIyNQQ" http://localhost:9090/api-server/user/details -v
     */
  @ApiOperation(
      value = "User Details info",
      authorizations = {@Authorization(value = "Bearer")})
  @GetMapping(value = "/details", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> getDetails() {
    try {
      // The request was autenticated via JWTAuthService. We need to extract user info from JWT
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      String username = auth.getPrincipal().toString();
      String userRole = auth.getAuthorities().toArray()[0].toString();
      Long userId = Long.parseLong(auth.getAuthorities().toArray()[1].toString());
      // LOG.info("userId: " + userId);
      return extractDetails(username, userRole, userId);
    } catch (JsonSyntaxException ex) {
      LOG.severe(ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end getDetails

  protected ResponseEntity<?> extractDetails(String username, String role, Long userId) {
    UserEntity user = getUser(username);
    JsonObject jsonResponse = new JsonObject();
    jsonResponse.addProperty("responseCode", 0);
    jsonResponse.addProperty("message", "OK getUserDetails ... ");
    jsonResponse.addProperty("username", username);
    jsonResponse.addProperty("role", role);
    jsonResponse.addProperty("nickname", user.getNickname());
    jsonResponse.addProperty("userId", userId);
    jsonResponse.addProperty("registeredAt", user.getRegistratedAt() + "");
    jsonResponse.addProperty("lastLoginAt", user.getLastLoginAt() + "");
    jsonResponse.add("likedTrails", gson.fromJson(user.getLikedTrails(), JsonArray.class));
    return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
  }

  private UserEntity getUser(String username) {
    List<UserEntity> uList = usersRep.findByUsername(username);
    if (uList.isEmpty()) {
      return null;
    }
    return uList.get(0);
  }

  /*
  curlj http://localhost:9090/api-server/user/unsubscribe
  curl -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuMSIsInJvbGUiOiJVU0VSIiwiZXhwIjoxNTQyNzUyODE1fQ.rAPrnpRzBNxNbpnLDM3QybNcT4chTH_HEbiM-IgotHq6lnOoa4aZX1eemh0gwn7XTuPDeeI9R1O0IuBJq97Nrw" http://localhost:8060/api-server/user-details -v
     */
  @ApiOperation(
      value = "Unsubscribe user",
      authorizations = {@Authorization(value = "Bearer")})
  @GetMapping(value = "/unsubscribe", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> unsubscribeUser() {
    try {
      // The request was autenticated via JWTAuthService. We need to extract user info from JWT
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      String username = auth.getPrincipal().toString();
      return doRemove(username);
    } catch (JsonSyntaxException ex) {
      LOG.severe(ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end unsubscribeUser

  protected ResponseEntity<?> doRemove(String username) {
    UserEntity user = getUser(username);
    usersRep.delete(user);
    JsonObject jsonResponse = new JsonObject();
    jsonResponse.addProperty("responseCode", 0);
    jsonResponse.addProperty("message", "OK user has been unsubscribed");
    jsonResponse.addProperty("username", username);
    jsonResponse.addProperty("nickname", user.getNickname());
    return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
  }
}
