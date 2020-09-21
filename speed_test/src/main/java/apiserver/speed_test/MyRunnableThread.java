package apiserver.speed_test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;

/** @author */
public class MyRunnableThread implements Runnable {

  private static final String INSERT_TEMPLATE =
      "{\"name\":\"thread_#THREAD#\", \"dataList\":[1,2,3, #TS#], \"ownerId\":#OWNERID#}";
  private static final String FIND_OWNERID_TEMPLATE = "{\"ownerId\":#OWNERID#}";
  private static final String UPDATE_TEMPLATE =
      "{\"id\":\"#MY_ID#\", \"name\":\"thread_#THREAD#\", \"dataList\":[1,2,3, #TS#]}";
  private final int threadId;
  private final String apiURL;
  private final String httpMethod;
  private final int totalRequests4Thread;
  private String myID = "";

  MyRunnableThread(int threadId, String apiURL, int totalRequests4Thread) {
    this.threadId = threadId;
    this.apiURL = apiURL;
    this.totalRequests4Thread = totalRequests4Thread;
    httpMethod = "POST";
  }

  /**
   * postToAPIServer
   *
   * @param apiURL : https://onesignal.com/api/v1/notifications
   * @param httpMethod
   * @param strRequest - String - can be in JSON format
   * @return String with the API server response or throw an exception
   * @throws Exception - String in format: Error code: <http_response_code> - <error_message>
   */
  public static String postToAPIServer(String apiURL, String httpMethod, String strRequest)
      throws Exception {
    String jsonResponse;
    URL url = new URL(apiURL);
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setUseCaches(false);
    con.setDoOutput(true);
    con.setDoInput(true);
    con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
    // con.setRequestProperty("Authorization", authorization);
    con.setRequestMethod(httpMethod);
    byte[] sendBytes = strRequest.getBytes("UTF-8");
    con.setFixedLengthStreamingMode(sendBytes.length);
    OutputStream outputStream = con.getOutputStream();
    outputStream.write(sendBytes);
    Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
    jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
    scanner.close();
    int httpResponseCode = con.getResponseCode();
    if (httpResponseCode != HttpURLConnection.HTTP_OK) {
      throw new IOException("Error code: " + httpResponseCode + " - " + jsonResponse);
    }
    return jsonResponse;
  } // postToAPIServer

  /**
   * updateProgress
   *
   * @param progressPercentage double from 0 to 1
   */
  static void updateProgress(double progressPercentage) {
    final int width = 50; // progress bar width in chars
    System.out.print("\r[");
    int i = 0;
    for (; i <= (int) (progressPercentage * width); i++) {
      System.out.print(".");
    }
    for (; i < width; i++) {
      System.out.print(" ");
    }
    System.out.print("]");
    if (progressPercentage >= 1) {
      System.out.print("\n");
    }
  }

  private void doTestInsert(long ownerId) {
    String serverResponse = "";
    myID = ""; // reset the previous ID value
    try {
      String jsonRequest = INSERT_TEMPLATE;
      jsonRequest = jsonRequest.replaceAll("#THREAD#", threadId + "");
      jsonRequest = jsonRequest.replaceAll("#TS#", System.currentTimeMillis() + "");
      jsonRequest = jsonRequest.replaceAll("#OWNERID#", ownerId + "");
      serverResponse = postToAPIServer(apiURL + "/addNew", httpMethod, jsonRequest);
      if (totalRequests4Thread == 1) {
        System.out.println("Thread " + threadId + " /addNew - request: " + jsonRequest);
        System.out.println("Thread " + threadId + " - serverResponse: " + serverResponse);
      }
      if (serverResponse == null || serverResponse.trim().equals("")) {
        throw new Exception("no response from server");
      }
      JSONObject jsonResponse = new JSONObject(serverResponse);
      if (jsonResponse.has("responseCode") && jsonResponse.getInt("responseCode") == 0) {
        // // SpeedTestApp.RESPONSE_OK_COUNTER.incrementAndGet();
        // we will increment RESPONSE_OK_COUNTER after /update request
        if (jsonResponse.has("id")) {
          myID = jsonResponse.getString("id");
        }
      }
    } catch (Exception ex) {
      if (totalRequests4Thread == 1) {
        System.out.println("error Thread " + threadId + " : " + ex.toString());
      }
    }
  } // end doTestInsert

  private void doTestFindByOwnerId(long ownerId) {
    if (myID.equals("")) {
      return;
    }
    String serverResponse = "";
    try {
      String jsonRequest = FIND_OWNERID_TEMPLATE;
      jsonRequest = jsonRequest.replaceAll("#OWNERID#", ownerId + "");
      serverResponse = postToAPIServer(apiURL + "/find-ownerId", httpMethod, jsonRequest);
      if (totalRequests4Thread == 1) {
        System.out.println("Thread " + threadId + " /find-ownerId - request: " + jsonRequest);
        System.out.println("Thread " + threadId + " - serverResponse: " + serverResponse);
      }
      if (serverResponse == null || serverResponse.trim().equals("")) {
        throw new Exception("no response from server");
      }
    } catch (Exception ex) {
      if (totalRequests4Thread == 1) {
        System.out.println("error Thread " + threadId + " : " + ex.toString());
      }
    }
  } // end doTestInsert

  private void doTestUpdate() {
    if (myID.equals("")) {
      return;
    }
    String serverResponse = "";
    try {
      String jsonRequest = UPDATE_TEMPLATE;
      jsonRequest = jsonRequest.replaceAll("#MY_ID#", myID);
      jsonRequest = jsonRequest.replaceAll("#THREAD#", threadId + "");
      jsonRequest = jsonRequest.replaceAll("#TS#", System.currentTimeMillis() + "");
      serverResponse = postToAPIServer(apiURL + "/update", httpMethod, jsonRequest);
      if (totalRequests4Thread == 1) {
        System.out.println("Thread " + threadId + " /update - request: " + jsonRequest);
        System.out.println("Thread " + threadId + " - serverResponse: " + serverResponse);
      }
      if (serverResponse == null || serverResponse.trim().equals("")) {
        throw new Exception("no response from server");
      }
      JSONObject jsonResponse = new JSONObject(serverResponse);
      if (jsonResponse.has("responseCode") && jsonResponse.getInt("responseCode") == 0) {
        SpeedTestApp.RESPONSE_OK_COUNTER.incrementAndGet();
      }
    } catch (Exception ex) {
      if (totalRequests4Thread == 1) {
        System.out.println("error Thread " + threadId + " : " + ex.toString());
      }
    }
  } // end doTestInsert

  @Override
  public void run() {
    for (int i = 0; i < totalRequests4Thread; i++) {
      long ownerId = (threadId + 1) * 1000 + i % 100;
      SpeedTestApp.REQUEST_TEST_COUNTER.incrementAndGet();
      doTestInsert(ownerId);
      doTestUpdate();
      doTestFindByOwnerId(ownerId);
      if (threadId == 1 && i % 100 == 0) {
        updateProgress(((double) i / totalRequests4Thread));
      }
    } // end for i
  } // end run
}
