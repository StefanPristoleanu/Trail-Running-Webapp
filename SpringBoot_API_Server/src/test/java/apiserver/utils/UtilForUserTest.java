package apiserver.utils;

import static org.junit.Assert.*;

import org.junit.Test;

/** @author stefan */
public class UtilForUserTest {

  @Test
  public void testHashEncryption() throws Exception {
    System.out.println("hashEncryption");
    String input = "123";
    String expectedResult = "A665A45920422F9D417E4867EFDC4FB8A04A1F3FFF1FA07E998E86F7F7A27AE3";
    String result = UtilForUser.hashEncryption(input);
    System.out.println("result: " + result + " leng " + result.length());
    assertEquals(result, expectedResult);
  }
}
