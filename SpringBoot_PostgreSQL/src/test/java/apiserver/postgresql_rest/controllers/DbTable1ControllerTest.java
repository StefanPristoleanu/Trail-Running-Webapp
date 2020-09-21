package apiserver.postgresql_rest.controllers;

import static org.junit.Assert.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

/** @author */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DbTable1ControllerTest {

  @Autowired private DbTable2Controller controller;

  public DbTable1ControllerTest() {}

  @BeforeClass
  public static void setUpClass() {}

  @AfterClass
  public static void tearDownClass() {}

  @Before
  public void setUp() {}

  @After
  public void tearDown() {}

  /** Test of addNew method, of class DbTable1Controller. */
  @Test
  public void testAddNewUpdateDelete() {
    try {
      System.out.println("addNew:");
      String request = "{\"name\":\"test_1001\", \"ownerId\":100, \"dataList\":[1,2,3,4]}";
      ResponseEntity result = controller.addNew(request);
      assertEquals(HttpStatus.OK, result.getStatusCode());
      JSONObject jsonResponse = new JSONObject(result.getBody().toString());
      System.out.println("response: " + jsonResponse.toString());
      int myId = jsonResponse.getInt("id");
      assertEquals(0, jsonResponse.getInt("responseCode"));

      System.out.println("update:");
      request = "{\"id\":" + myId + ", \"name\":\"test_1001\", \"dataList\":[1001,1002,1003,1004]}";
      result = controller.update(request);
      assertEquals(HttpStatus.OK, result.getStatusCode());
      jsonResponse = new JSONObject(result.getBody().toString());
      System.out.println("response: " + jsonResponse.toString());
      assertEquals(0, jsonResponse.getInt("responseCode"));

      System.out.println("delete:");
      request = "{\"id\":" + myId + "}";
      result = controller.delete(request);
      assertEquals(HttpStatus.OK, result.getStatusCode());
      jsonResponse = new JSONObject(result.getBody().toString());
      System.out.println("response: " + jsonResponse.toString());
      assertEquals(0, jsonResponse.getInt("responseCode"));
    } catch (JSONException ex) {
      Logger.getLogger(DbTable1ControllerTest.class.getName()).log(Level.SEVERE, null, ex);
    }
  } // end testAddNewUpdateDelete
}
