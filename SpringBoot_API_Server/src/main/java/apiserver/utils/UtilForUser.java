package apiserver.utils;

import java.io.UnsupportedEncodingException;

/** @author stefan */
public class UtilForUser {

  public static String hashEncryption(String mytext) {
    String cryptText;
    try {
      // java.security.MessageDigest encryptionAlgo =
      // java.security.MessageDigest.getInstance("MD5");
      java.security.MessageDigest encryptionAlgo =
          java.security.MessageDigest.getInstance("SHA-256");
      encryptionAlgo.reset();
      encryptionAlgo.update(mytext.getBytes("UTF-8"));
      byte[] sha_digest = encryptionAlgo.digest();
      StringBuilder hexString = new StringBuilder();
      for (int i = 0; i < sha_digest.length; i++) {
        if ((0xFF & sha_digest[i]) < 16) {
          hexString.append("0");
        }
        hexString.append(Integer.toHexString(0xFF & sha_digest[i]));
      }
      cryptText = hexString.toString().toUpperCase();
    } catch (java.security.NoSuchAlgorithmException | UnsupportedEncodingException ex) {
      System.out.println("SHA algorithm was not found ! Error: " + ex.toString());
      return mytext;
    }
    return cryptText;
  } // end encryptPassword(String mytext)
}
