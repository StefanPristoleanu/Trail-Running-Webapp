package springrest.controllers;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author stefan  - 2018.10.11
 */
@RestController
@RequestMapping("/rest")
public class TestRestController {

    /*
    http://localhost:8080/rest/test1
    */
    @RequestMapping(value = "/test1", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> test1() {
        try {
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("responseCode", 0);
            jsonResponse.put("message", "OK Test1 - GET!");
            jsonResponse.put("timestamp", new Date() + "");
            return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
        } catch (JSONException ex) {
            Logger.getLogger(TestRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
        }
    }// test1

    /*
    curl -H "Content-Type: application/json http://localhost:8080/rest/test2 -d '{"q":1}' 
    or
    alias curlj='curl --header "Content-Type: application/json" -w "\n" -v'
    curlj http://localhost:8080/rest/test2 -d '{"accessToken":1, "request":"test-api"}'
    - for json pretty print:
    curlj http://localhost:8080/rest/test2 -d '{"accessToken":1, "request":"test-api"}' | json_pp
    
     */
    @RequestMapping(value = "/test2", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> test2(@RequestBody String request) {
        try {
            JSONObject jsonRequest = new JSONObject(request);
            System.out.println("~~~ request: " + jsonRequest.toString());
            if (jsonRequest.has("accessToken")) {
                System.out.println("~~~ accessToken: " + jsonRequest.getString("accessToken"));
            }
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("responseCode", 0);
            jsonResponse.put("message", "OK Test 2 - POST!");
            jsonResponse.put("receivedRequest", jsonRequest);
            jsonResponse.put("timestamp", new Date() + "");
            return new ResponseEntity(jsonResponse.toString(), HttpStatus.OK);
        } catch (JSONException ex) {
            Logger.getLogger(TestRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new ResponseEntity(ex.toString(), HttpStatus.BAD_REQUEST);
        }
    }// test2

}
