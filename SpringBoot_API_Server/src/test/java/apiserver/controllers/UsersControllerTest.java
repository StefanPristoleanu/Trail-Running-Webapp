package apiserver.controllers;

import static org.junit.Assert.*;

import apiserver.dto.LoginDTO;
import apiserver.dto.RegisterDTO;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

/** @author stefan */
@RunWith(SpringRunner.class)
@SpringBootTest
public class UsersControllerTest {

  @Autowired private UsersController instance;

  @BeforeClass
  public static void setUpClass() {}

  @AfterClass
  public static void tearDownClass() {}

  @Before
  public void setUp() {}

  @After
  public void tearDown() {}

  /** Test of register method, of class UsersController. */
  @Test
  // @WithMockUser(username = "test_999", password = "123", roles = "USER")
  public void testUsersController() {
    String username = "test_999";
    System.out.println("testUsersController");
    RegisterDTO request = new RegisterDTO(username, "123", "nick");
    ResponseEntity result = instance.register(request);
    assertEquals(HttpStatus.OK, result.getStatusCode());
    System.out.println("result register: " + result);

    LoginDTO requestLogin = new LoginDTO(username, "123");
    result = instance.login(requestLogin);
    assertEquals(HttpStatus.OK, result.getStatusCode());
    System.out.println("result login: " + result);

    result = instance.extractDetails(username, "USER", 1L);
    assertEquals(HttpStatus.OK, result.getStatusCode());
    System.out.println("result user details: " + result);

    result = instance.doRemove(username);
    assertEquals(HttpStatus.OK, result.getStatusCode());
    System.out.println("result unsubscribe user: " + result);
  }
}
