package apiserver.controllers;

import apiserver.dto.GpsCoord;
import apiserver.dto.TrailDTO;
import apiserver.dto.UpdateTrailDTO;
import apiserver.entities.TrailEntity;
import apiserver.entities.TrailsRepository;
import apiserver.entities.UserEntity;
import apiserver.entities.UsersRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** @author Stefan */
@Api(tags = {"Trail management"})
@RestController
@RequestMapping("/api-server/trail")
public class TrailsController {

  private static final Logger LOG = Logger.getLogger(TrailsController.class.getName());
  private static final String dateForPoliceAPI = "2019-01";

  // https://google.github.io/gson/apidocs/com/google/gson/Gson.html
  // Gson instances are Thread-safe so you can reuse them freely across multiple threads:
  public static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

  @Autowired private TrailsRepository trailsRep;
  @Autowired private UsersRepository usersRep;

  // ToDo: add here @Autowired private UserRepository userRepository;
  @PostConstruct
  public void init() {}

  @ExceptionHandler
  void handleIllegalArgumentException(IllegalArgumentException e, HttpServletResponse response)
      throws IOException {
    response.sendError(HttpStatus.BAD_REQUEST.value());
  }

  // alias curlj='curl --header "Content-Type: application/json" -w "\n" -v'
  // curlj http://localhost:9090/api-server/trail/add -d
  // '{"trailName":"trail1","trailCreatedBy":1,"trailCreatedAt":"Feb 16, 2019 10:13:54
  // PM","trailType":"easy","trailDifficulty":1,"trailLength":10,"dataJSON":[{\"x\":53.2734,
  // \"y\":-7.77832031}, {\"x\":53.3456, \"y\":-7.74215143}, {\"x\":53.4567, \"y\":-7.7712112}]}'
  @ApiOperation(
      value = "Add a specific trail",
      notes =
          "<b>coordinates: </b>Array of GPS latitude and logitude</br>"
              + "<b>trailDifficulty: </b>Numeric value from 1 to 5</br>"
              + "<b>trailLength: </b>Numeric value in meters</br>"
              + "<b>trailName: </b>Chosen name by the user</br>"
              + "<b>trailType: </b>Possible values - walking, running, cycling</br>"
              + "<b>deltaElevation: </b>Double value for the maximum difference elevation in meters</br>"
              + "<b>trailSlope: </b>Overall slope of the trail in meters</br> </br> "
              + "<b>This method automatically calls the police API and computes the number of crimes for the start of the trail</b>",
      authorizations = {@Authorization(value = "Bearer")})
  @PostMapping(value = "/add", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> addTrail(@RequestBody TrailDTO trailDTO) {
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
      TrailEntity newTrail = new TrailEntity(trailDTO, userId);

      // Compute the security rating for a trail
      newTrail.setTrailIncidents(composeTrailIncidents(trailDTO.getCoordinates().get(0)));
      trailsRep.save(newTrail);
      JsonObject jsonResponse = new JsonObject();
      jsonResponse.addProperty("responseCode", 0);
      jsonResponse.addProperty("message", "OK added new trail: " + newTrail.getTrailName());
      return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
    } catch (JsonSyntaxException ex) {
      LOG.severe(ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end addTrail

  // Connects to an URL and returns the http response in JsonNode format
  // Example URL:
  // https://data.police.uk/api/crimes-at-location?date=2017-02&lat=52.629729&lng=-1.131592
  // https://data.police.uk/api/crimes-at-location?date=2019-01&lat=53.467458&lng=-2.234257
  public static Integer composeTrailIncidents(GpsCoord currentPoint) {
    try {
      // ?date=2017-02&lat=52.629729&lng=-1.131592
      String policeAPI = "https://data.police.uk/api/crimes-at-location?date=" + dateForPoliceAPI;
      String lat = "&lat=" + currentPoint.lat;
      String lng = "&lng=" + currentPoint.lng;
      policeAPI = policeAPI + lat + lng;
      // System.out.println("#### " + policeAPI);
      HttpResponse<JsonNode> jsonResponse;
      jsonResponse = Unirest.get(policeAPI).header("accept", "application/json").asJson();
      // System.out.println("Response: " + jsonResponse.getBody().toString());

      return jsonResponse.getBody().getArray().length();
    } catch (UnirestException ex) {
      Logger.getLogger(TrailsController.class.getName()).log(Level.SEVERE, null, ex);
      return 0;
    }
  }

  @ApiOperation(
      value = "Update an existing trail by the trail owner",
      notes =
          "<b>trailDifficulty: </b>Numeric value from 1 to 5</br>"
              + "<b>trailLength: </b>Numeric value in meters</br>"
              + "<b>trailName: </b>Chosen name by the user</br>"
              + "<b>trailType: </b>Possible values - walking, running, cycling</br>",
      authorizations = {@Authorization(value = "Bearer")})
  @PostMapping(value = "/update", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> updateTrail(@RequestBody UpdateTrailDTO trailDTO) {
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
      Optional<TrailEntity> myTrail =
          trailsRep.findById(trailDTO.getTrailId()); // findByUserId(userId);
      if (!myTrail.isPresent()) {
        return new ResponseEntity("Trail not found", HttpStatus.NOT_FOUND);
      }
      myTrail.get().setTrailName(trailDTO.getTrailName());
      if (trailDTO.getTrailDifficulty() != null) {
        myTrail.get().setTrailDifficulty(trailDTO.getTrailDifficulty());
      }
      if (trailDTO.getTrailDescription() != null) {
        myTrail.get().setTrailDescription(trailDTO.getTrailDescription());
      }
      if (trailDTO.getTrailType() != null) {
        myTrail.get().setTrailType(trailDTO.getTrailType());
      }
      trailsRep.save(myTrail.get());
      JsonObject jsonResponse = new JsonObject();
      jsonResponse.addProperty("responseCode", 0);
      jsonResponse.addProperty("message", "OK updated trail: " + myTrail.get().getTrailName());
      return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
    } catch (JsonSyntaxException ex) {
      LOG.severe(ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end updateTrail

  @ApiOperation(
      value = "Like an existing trail",
      notes =
          "The requested parameter is a json with the following format: " + "{\"trailId\": value}",
      authorizations = {@Authorization(value = "Bearer")})
  @PostMapping(value = "/like", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> likeTrail(@RequestBody String strTrailId) {
    try {
      Long userId = 0L;
      try {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        userId = Long.parseLong(auth.getAuthorities().toArray()[1].toString());
      } catch (Exception e) {
        LOG.severe(e.toString());
      }
      JsonObject jsonResponse = new JsonObject();
      JsonObject json = new JsonParser().parse(strTrailId).getAsJsonObject();
      Long trailId = json.get("trailId").getAsLong();
      Optional<TrailEntity> myTrail = trailsRep.findById(trailId);
      if (!myTrail.isPresent()) {
        return new ResponseEntity("Trail not found", HttpStatus.NOT_FOUND);
      }
      Optional<UserEntity> myUser = usersRep.findById(userId);
      if (!myUser.isPresent()) {
        return new ResponseEntity("User not found", HttpStatus.NOT_FOUND);
      }
      JsonArray jsonLikes = new JsonParser().parse(myUser.get().getLikedTrails()).getAsJsonArray();
      JsonElement trailIdElem = gson.fromJson(trailId.toString(), JsonElement.class);
      if (!jsonLikes.contains(trailIdElem)) {
        jsonLikes.add(trailId);
        myUser.get().setLikedTrails(jsonLikes.toString());
        usersRep.save(myUser.get());
        myTrail.get().setTrailNoOfLikes(myTrail.get().getTrailNoOfLikes() + 1);
        trailsRep.save(myTrail.get());
        return new ResponseEntity(jsonLikes.toString(), HttpStatus.OK);
      } else {
        return new ResponseEntity("Already liked trail " + trailId, HttpStatus.NOT_ACCEPTABLE);
      }
    } catch (JsonSyntaxException ex) {
      LOG.severe(ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end likeTrail

  @ApiOperation(value = "Retrieve all trails. It is public function displayed on the home page.")
  @GetMapping(value = "/find-all", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<List<TrailEntity>> findAllTrails() {
    try {
      Iterator<TrailEntity> trailsList = trailsRep.findAll().iterator();
      JsonArray jsonArray = new JsonArray();
      while (trailsList.hasNext()) {
        jsonArray.add(trailsList.next().toJsonObject());
      }
      return new ResponseEntity(jsonArray.toString(), HttpStatus.OK);
    } catch (JsonSyntaxException ex) {
      LOG.severe(ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end addTrail

  @ApiOperation(
      value = "Find a specific user's trails",
      notes =
          "The requested parameter is a json with the following format: " + "{\"userId\": value}",
      authorizations = {@Authorization(value = "Bearer")})
  @GetMapping(value = "/user-trails", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> findUserTrails(
      @RequestParam(name = "userId", required = true) Long userId) {
    try {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      // Long userId = Long.parseLong(auth.getAuthorities().toArray()[1].toString());
      List<TrailEntity> trailsList = trailsRep.findByUserId(userId);
      JsonArray jsonArray = new JsonArray();
      Iterator<TrailEntity> iterator = trailsList.iterator();
      while (iterator.hasNext()) {
        jsonArray.add(iterator.next().toJsonObject());
      }
      return new ResponseEntity(jsonArray.toString(), HttpStatus.OK);
    } catch (JsonSyntaxException ex) {
      LOG.severe(ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end findAllTrailsOfUser

  @ApiOperation(
      value = "Find a specific user's certain trail",
      authorizations = {@Authorization(value = "Bearer")})
  @GetMapping(value = "/trail-details", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> findOneTrail(
      @RequestParam(name = "trailId", required = true) Long trailId) {
    try {
      System.out.println("findOneTrail trailId: " + trailId);
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      Long userId = Long.parseLong(auth.getAuthorities().toArray()[1].toString());
      Optional<TrailEntity> myTrail = trailsRep.findById(trailId);
      if (!myTrail.isPresent()) {
        return new ResponseEntity("Trail not found", HttpStatus.NOT_FOUND);
      }
      JsonObject jsonResponse = myTrail.get().toJsonObject();
      Optional<UserEntity> myUser = usersRep.findById(myTrail.get().getTrailCreatedBy());
      if (!myUser.isPresent()) {
        return new ResponseEntity("Trail not found", HttpStatus.NOT_FOUND);
      }
      jsonResponse.addProperty("nickname", myUser.get().getNickname());
      return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
    } catch (JsonSyntaxException ex) {
      LOG.severe(ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end findAllTrailsOfUser

  @ApiOperation(
      value = "Delete a specific trail",
      notes =
          "The requested parameter is a json with the following format: " + "{\"trailId\": value}",
      authorizations = {@Authorization(value = "Bearer")})
  @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> deleteTrail(@RequestBody String strTrailId) {
    try {
      // JsonObject jsonRequest = gson.fromJson(request, JsonObject.class);
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      Long userId = Long.parseLong(auth.getAuthorities().toArray()[1].toString());
      List<TrailEntity> myTrails = trailsRep.findByUserId(userId);
      Iterator<TrailEntity> iter = myTrails.iterator();
      JsonObject jsonResponse = new JsonObject();
      JsonObject json = new JsonParser().parse(strTrailId).getAsJsonObject();
      Long trailId = json.get("trailId").getAsLong();

      while (iter.hasNext()) {
        TrailEntity currentTrail = iter.next();
        if (currentTrail.getTrailId().equals(trailId)) {
          trailsRep.delete(currentTrail);
          jsonResponse.addProperty("responseCode", 0);
          jsonResponse.addProperty(
              "message", "OK deleted the trail: " + currentTrail.getTrailName());
          return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
        }
      }
      jsonResponse.addProperty("responseCode", 0);
      jsonResponse.addProperty("message", "No trail found with trailId: " + trailId);
      return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);

    } catch (JsonSyntaxException ex) {
      LOG.severe(ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end deleteTrail
}
