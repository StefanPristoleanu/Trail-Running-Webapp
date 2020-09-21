package apiserver.speed_test;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/** @author */
public class SpeedTestApp {

  private static final String API_URL = // "http://sa306.saturn.fastwebserver.de:8090/rest-mongodb";
      "http://sa306.saturn.fastwebserver.de:8080/rest-postgresql";
  // http://sa306.saturn.fastwebserver.de:8090/rest-mongodb
  private static final int NO_THREADS = 1;
  private static final int NO_REQ_1TH = 1;

  private static String apiURL = API_URL;
  private static int noThreads = NO_THREADS;
  private static int noReq1Th = NO_REQ_1TH;
  public static final AtomicLong REQUEST_TEST_COUNTER = new AtomicLong(0);
  public static final AtomicLong RESPONSE_OK_COUNTER = new AtomicLong(0);

  private static void loadParams(String[] args) {
    if (args.length < 3) {
      System.out.println("\nWarning: missing parameters in the application call!");
      System.out.println("The application requires 3 parameters in the format below:");
      System.out.println(
          "java -jar speed_test-0.0.1.jar <api_server_address> <number_of_threads> <requests_per_thread>");
    }
    if (args.length > 0) {
      apiURL = args[0];
    } else {
      System.out.println(
          "The <api_server_address> parameter was not found and the test will use the default value: "
              + API_URL);
    }
    if (args.length > 1) {
      noThreads = Integer.valueOf(args[1]);
    } else {
      System.out.println(
          "The <number_of_threads> parameter was not found and the test will use the default value: "
              + NO_THREADS);
    }
    if (args.length > 2) {
      noReq1Th = Integer.valueOf(args[2]);
    } else {
      System.out.println(
          "The <requests_per_thread> parameter was not found and the test will use the default value: "
              + NO_REQ_1TH);
    }
  }

  private static void startTesting() {
    long t1 = System.currentTimeMillis();
    if (noThreads == 1) {
      System.out.println("\nPlease wait for 1 test thread ...\n");
    } else {
      System.out.println("\nPlease wait for " + noThreads + " test threads ...\n");
    }
    ExecutorService executor = Executors.newFixedThreadPool(noThreads);
    for (int i = 0; i < noThreads; i++) {
      Runnable worker = new MyRunnableThread((i + 1), apiURL, noReq1Th);
      executor.execute(worker);
    }
    executor.shutdown();
    // Wait until all threads are finish
    while (!executor.isTerminated()) {
      // just wait ... :)
    }
    long difTime = System.currentTimeMillis() - t1;
    // MyRunnableThread.updateProgress(1);
    System.out.println("\nAPI Server Speed Test Stats: ");
    System.out.println("api_server_address: " + apiURL);
    System.out.println(
        "Total test requests:    "
            + noThreads
            + " * "
            + noReq1Th
            + " = "
            + REQUEST_TEST_COUNTER.get());
    System.out.println("Total OK API responses: " + RESPONSE_OK_COUNTER.get());
    System.out.println(
        "Total Errors responses: " + (REQUEST_TEST_COUNTER.get() - RESPONSE_OK_COUNTER.get()));
    String strDiffTime = String.format("%.3f", (double) (difTime) / 1000);
    System.out.println("Total execution time: " + strDiffTime + " sec ");
    String strSpeed =
        String.format("%.2f", (double) REQUEST_TEST_COUNTER.get() / ((double) difTime / 1000));
    System.out.println("Total requests per second: " + strSpeed + "\n");
  }

  public static void main(String[] args) {
    System.out.println("\nSpeed test for API Server");
    loadParams(args);
    System.out.println("\napi_server_address: " + apiURL);
    System.out.println("number_of_threads: " + noThreads);
    System.out.println("requests_per_thread: " + noReq1Th);
    if (noThreads == 1 && noReq1Th == 1) {
      System.out.println("Start testing with 1 request:");
      startTesting();
      System.exit(0);
    }
    System.out.println("Start testing with " + noThreads * noReq1Th + " total requests? Y/N");
    Scanner in = new Scanner(System.in);
    String option = in.nextLine().trim().toUpperCase();
    if (option.startsWith("Y")) {
      startTesting();
    } else {
      System.out.println("Exit!");
    }
  }
}
