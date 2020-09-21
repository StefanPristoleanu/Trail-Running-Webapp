package apiserver.postgresql_rest.controllers;

import apiserver.postgresql_rest.entities.MyTableModel;
import apiserver.postgresql_rest.entities.MyTableRepository;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** @author Stefan */
@RestController
@RequestMapping("/rest-postgresql-v1")
public class DbTable1Controller {
  private static final Logger LOG = Logger.getLogger(DbTable1Controller.class.getName());

  private Gson gson;

  @Autowired private MyTableRepository repository;

  @PostConstruct
  public void init() {
    gson = new Gson();
    LOG.info("DbTable1Controller - OK init gson");
  }

  // curlj http://localhost:8080/rest-postgresql/addNew -d  '{"name":"n1", "dataList":[1,2,3,4]}'
  @PostMapping(value = "/addNew", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> addNew(@RequestBody String request) {
    try {
      JsonObject jsonRequest = gson.fromJson(request, JsonObject.class);
      // JsonObject jsonRequest = (new JsonParser()).parse(request).getAsJsonObject();
      if (!jsonRequest.has("name") || !jsonRequest.has("dataList")) {
        return new ResponseEntity("incorrect json request", HttpStatus.BAD_REQUEST);
      }
      MyTableModel newRec = new MyTableModel(jsonRequest);
      repository.save(newRec);
      JsonObject jsonResponse = new JsonObject();
      jsonResponse.addProperty("responseCode", 0);
      jsonResponse.addProperty(
          "message",
          "OK add new record in DbTable1 with name: " + jsonRequest.get("name").getAsString());
      jsonResponse.addProperty("id", newRec.getId() + "");
      // System.out.println("newRec: " + newRec.toString());
      return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
    } catch (JsonSyntaxException ex) {
      LOG.severe(ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end addNew

  // curlj http://localhost:8080/rest-postgresql/update -d
  // '{"id":"7","name":"n7","dataList":[71,72,77]}'
  @PostMapping(value = "/update", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> update(@RequestBody String request) {
    try {
      JsonObject jsonRequest = gson.fromJson(request, JsonObject.class);
      // JsonObject jsonRequest = (new JsonParser()).parse(request).getAsJsonObject();
      if (!jsonRequest.has("id")) {
        return new ResponseEntity("id tag not found in json request", HttpStatus.BAD_REQUEST);
      }
      Long id = Long.valueOf(jsonRequest.get("id").getAsString()); // jsonRequest.getLong("id");
      MyTableModel myRec = repository.findById(id).orElse(null);
      if (myRec == null) {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("responseCode", -1);
        jsonResponse.addProperty("message", "Not found a record with id: " + id);
        return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
      }
      myRec.update(gson, jsonRequest);
      repository.save(myRec);
      JsonObject jsonResponse = new JsonObject();
      jsonResponse.addProperty("responseCode", 0);
      jsonResponse.addProperty("message", "OK updated record in DbTable1 for id: " + id);
      return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
    } catch (JsonSyntaxException ex) {
      LOG.severe(ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end update

  // curlj http://localhost:8080/rest-postgresql/delete -d  '{"id":"9"}'
  @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> delete(@RequestBody String request) {
    try {
      JsonObject jsonRequest = gson.fromJson(request, JsonObject.class);
      if (!jsonRequest.has("id")) {
        return new ResponseEntity("id tag not found in json request", HttpStatus.BAD_REQUEST);
      }
      Long id = Long.valueOf(jsonRequest.get("id").getAsString()); // jsonRequest.getLong("id");
      MyTableModel myRec = repository.findById(id).orElse(null);
      if (myRec == null) {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("responseCode", -1);
        jsonResponse.addProperty("message", "Not found a record with id: " + id);
        return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
      }
      repository.delete(myRec);
      JsonObject jsonResponse = new JsonObject();
      jsonResponse.addProperty("responseCode", 0);
      jsonResponse.addProperty("message", "OK deleted record in DbTable1 with id: " + id);
      return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
    } catch (JsonSyntaxException ex) {
      LOG.severe(ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end delete

  // curlj http://localhost:8080/rest-postgresql/find-name -d  '{"name":"n2"}'
  // curlj http://localhost:8080/rest-postgresql/find-name -d  '{"name":"thread_1"}'
  @PostMapping(value = "/find-name", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> findByName(@RequestBody String request) {
    try {
      JsonObject jsonRequest = gson.fromJson(request, JsonObject.class);
      if (!jsonRequest.has("name")) {
        return new ResponseEntity("name tag not found in json request", HttpStatus.BAD_REQUEST);
      }
      String name = jsonRequest.get("name").getAsString();
      List<MyTableModel> recList = repository.findFirst10ByName(name);
      if (recList == null || recList.isEmpty()) {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("responseCode", -1);
        jsonResponse.addProperty("message", "Not found a record with name: " + name);
        return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
      }
      JsonObject jsonResponse = new JsonObject();
      jsonResponse.addProperty("responseCode", 0);
      jsonResponse.addProperty("message", "OK query records from DbTable1 with name: " + name);
      jsonResponse.add("resultList", genJSONArrayFromList(recList));
      return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
    } catch (JsonSyntaxException ex) {
      LOG.severe(ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end findByName

  private JsonArray genJSONArrayFromList(List<MyTableModel> recList) {
    JsonArray jsonArray = new JsonArray();
    recList.forEach(
        (rec) -> {
          jsonArray.add(rec.toJSONObject());
        });
    return jsonArray;
  }

  // curlj http://localhost:8080/rest-postgresql/query -d  '{"minCounterUpdates": 2}'
  @PostMapping(value = "/query", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> query(@RequestBody String request) {
    try {
      JsonObject jsonRequest = (new JsonParser()).parse(request).getAsJsonObject();
      if (!jsonRequest.has("minCounterUpdates")) {
        return new ResponseEntity(
            "minCounterUpdates tag not found in json request", HttpStatus.BAD_REQUEST);
      }
      long minCounterUpdates = jsonRequest.get("minCounterUpdates").getAsLong();
      List<MyTableModel> recList =
          repository.findTop10ByCounterUpdatesGreaterThan(minCounterUpdates);
      if (recList == null || recList.isEmpty()) {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("responseCode", -1);
        jsonResponse.addProperty(
            "message", "Not found a record with counterUpdates > " + minCounterUpdates);
        return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
      }
      JsonObject jsonResponse = new JsonObject();
      jsonResponse.addProperty("responseCode", 0);
      jsonResponse.addProperty(
          "message", "OK query records from DbTable1 with counterUpdates > " + minCounterUpdates);
      jsonResponse.add("resultList", genJSONArrayFromList(recList));
      return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
    } catch (JsonSyntaxException ex) {
      LOG.severe(ex.toString());
      return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
    }
  } // end query
}
