package apiserver.mongodb_rest.controllers;

import apiserver.mongodb_rest.entities.DbTable1;
import apiserver.mongodb_rest.entities.DbTable1Repository;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** @author */
@RestController
@RequestMapping("/rest-mongodb")
public class DbTable1Controller {

  @Autowired private DbTable1Repository repository;

  // curlj http://localhost:8080/rest-mongodb/addNew -d  '{"name":"n1", "dataList":[1,2,3,4]}'
  @PostMapping(value = "/addNew", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> addNew(@RequestBody String request) {
    try {
      JSONObject jsonRequest = new JSONObject(request);
      DbTable1 newRec =
          new DbTable1(
              jsonRequest.getString("name"),
              jsonRequest.getJSONArray("dataList"),
              jsonRequest.getLong("ownerId"));
      repository.save(newRec);
      JSONObject jsonResponse = new JSONObject();
      jsonResponse.put("responseCode", 0);
      jsonResponse.put("message", "OK add new record in DbTable1 for name: " + newRec.getName());
      jsonResponse.put("id", newRec.getId());
      // System.out.println("newRec: " + newRec.toString());
      return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
    } catch (JSONException ex) {
      Logger.getLogger(DbTable1Controller.class.getName()).log(Level.SEVERE, null, ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end addNew

  // curlj http://localhost:8080/rest-mongodb/update -d  '{"id":"5be19c85873c3073650ba312",
  // "name":"n1", "dataList":[1,2,3,4]}'
  @PostMapping(value = "/update", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> update(@RequestBody String request) {
    try {
      JSONObject jsonRequest = new JSONObject(request);
      if (!jsonRequest.has("id")) {
        return new ResponseEntity("id tag not found in json request", HttpStatus.BAD_REQUEST);
      }
      String id = jsonRequest.getString("id");
      DbTable1 myRec = repository.findById(id).orElse(null);
      if (myRec == null) {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("responseCode", -1);
        jsonResponse.put("message", "Not found a record with id: " + id);
        return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
      }
      myRec.update(jsonRequest.getString("name"), jsonRequest.getJSONArray("dataList"));
      repository.save(myRec);
      JSONObject jsonResponse = new JSONObject();
      jsonResponse.put("responseCode", 0);
      jsonResponse.put("message", "OK updated record in DbTable1 for id: " + id);
      return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
    } catch (JSONException ex) {
      Logger.getLogger(DbTable1Controller.class.getName()).log(Level.SEVERE, null, ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end update

  // curlj http://localhost:8080/rest-mongodb/delete -d  '{"id":"5be1976a873c30733c9da283"}'
  @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> delete(@RequestBody String request) {
    try {
      JSONObject jsonRequest = new JSONObject(request);
      if (!jsonRequest.has("id")) {
        return new ResponseEntity("id tag not found in json request", HttpStatus.BAD_REQUEST);
      }
      String id = jsonRequest.getString("id");
      DbTable1 myRec = repository.findById(id).orElse(null);
      if (myRec == null) {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("responseCode", -1);
        jsonResponse.put("message", "Not found a record with id: " + id);
        return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
      }
      repository.delete(myRec);
      JSONObject jsonResponse = new JSONObject();
      jsonResponse.put("responseCode", 0);
      jsonResponse.put("message", "OK deleted record in DbTable1 with id: " + id);
      return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
    } catch (JSONException ex) {
      Logger.getLogger(DbTable1Controller.class.getName()).log(Level.SEVERE, null, ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end delete

  // curlj http://localhost:8080/rest-mongodb/find-name -d  '{"ownerId":"100"}'
  @PostMapping(value = "/find-ownerId", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> findByOwnerId(@RequestBody String request) {
    try {
      JSONObject jsonRequest = new JSONObject(request);
      if (!jsonRequest.has("ownerId")) {
        return new ResponseEntity("ownerId not found in json request", HttpStatus.BAD_REQUEST);
      }
      long ownerId = jsonRequest.getLong("ownerId");
      List<DbTable1> recList = repository.findByTrailCreatedBy(ownerId);
      if (recList == null || recList.isEmpty()) {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("responseCode", -1);
        jsonResponse.put("message", "Not found a record with ownerId: " + ownerId);
        return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
      }
      JSONObject jsonResponse = new JSONObject();
      jsonResponse.put("responseCode", 0);
      jsonResponse.put("message", "OK query records from DbTable1 with ownerId: " + ownerId);
      jsonResponse.put("resultList", new JSONArray(recList));
      return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
    } catch (JSONException ex) {
      Logger.getLogger(DbTable1Controller.class.getName()).log(Level.SEVERE, null, ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end findByName

  // curlj http://localhost:8080/rest-mongodb/query -d  '{"minCounterUpdates": 2}'
  @PostMapping(value = "/query", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> query(@RequestBody String request) {
    try {
      JSONObject jsonRequest = new JSONObject(request);
      if (!jsonRequest.has("minCounterUpdates")) {
        return new ResponseEntity(
            "minCounterUpdates tag not found in json request", HttpStatus.BAD_REQUEST);
      }
      long minCounterUpdates = jsonRequest.getLong("minCounterUpdates");
      List<DbTable1> recList = repository.findTop10ByCounterUpdatesGreaterThan(minCounterUpdates);
      if (recList == null || recList.isEmpty()) {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("responseCode", -1);
        jsonResponse.put(
            "message", "Not found a record with counterUpdates > " + minCounterUpdates);
        return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
      }
      JSONObject jsonResponse = new JSONObject();
      jsonResponse.put("responseCode", 0);
      jsonResponse.put(
          "message", "OK query records from DbTable1 with counterUpdates > " + minCounterUpdates);
      jsonResponse.put("resultList", new JSONArray(recList));
      return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
    } catch (JSONException ex) {
      Logger.getLogger(DbTable1Controller.class.getName()).log(Level.SEVERE, null, ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end findByName
}
