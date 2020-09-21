package apiserver.controllers;

import static apiserver.controllers.TrailsController.gson;
import static org.junit.Assert.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/** @author stefan */
public class TrailsControllerTest {

  public TrailsControllerTest() {}

  @BeforeClass
  public static void setUpClass() {}

  @AfterClass
  public static void tearDownClass() {}

  @Before
  public void setUp() {}

  @After
  public void tearDown() {}

  /** Test of init method, of class TrailsController. */
  @Ignore
  @Test
  public void testInit() {
    System.out.println("init");
    TrailsController instance = new TrailsController();
    instance.init();
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of addTrail method, of class TrailsController. [{"x":53.2734, "y":-7.77832031},
   * {"x":53.3456, "y":-7.74215143}, {"x":53.4567, "y":-7.7712112}]
   */
  @Ignore
  @Test
  public void testAddTrail() {
    System.out.println("addTrail");
    String stringArray =
        "[{\"x\":53.2734, \"y\":-7.77832031}, {\"x\":53.3456, \"y\":-7.74215143}, {\"x\":53.4567, \"y\":-7.7712112}]";
    Gson gson = new Gson();

    JsonArray jsonArray = gson.fromJson(stringArray, JsonArray.class);
    // TrailEntity newTrail = new TrailEntity("trail1", 1, "easy", 1, 10, jsonArray.toString());

    // System.out.println("new trail: " + gson.toJson(newTrail));
    //        TrailsController instance = new TrailsController();
    //        ResponseEntity expResult = null;
    //        ResponseEntity result = instance.addTrail(newTrail);
    //        assertEquals(expResult, result);
  }

  public static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

  @Test
  public void testLikeTrail() {
    String existedLikes = "[]"; // myUser.get().getLikedTrails()
    JsonArray jsonLikes = new JsonParser().parse(existedLikes).getAsJsonArray();
    Long trailId = 1L;
    JsonElement trailIdElem = gson.fromJson(trailId.toString(), JsonElement.class);
    assertNotNull(trailIdElem);
  }
}
