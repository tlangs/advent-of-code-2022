package org.tlangs;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class InputDownloader {

  private static final String USER_AGENT = "Java-http-client/" + System.getProperty("java.version");

  static InputStream downloadInput(String day) throws IOException {
    System.out.printf("Downloading input for Day %s%n", day);
    var sessionCookie = System.getenv("SESSION_COOKIE");
    if (sessionCookie == null) {
      throw new RuntimeException("Cannot download input without SESSION_COOKIE env var set!");
    }
    URL endpoint = new URL(String.format("https://adventofcode.com/2022/day/%s/input", day));
    HttpURLConnection httpURLConnection = (HttpURLConnection) endpoint.openConnection();
    httpURLConnection.setRequestMethod("GET");
    httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);
    httpURLConnection.setRequestProperty("Cookie", String.format("session=%s", sessionCookie));

    int responseCode = httpURLConnection.getResponseCode();

    if (responseCode == HttpURLConnection.HTTP_OK) { // success
      System.out.println("Successfully downloaded input\n");
      return httpURLConnection.getInputStream();
    } else {
      throw new RuntimeException(String.format("Encountered %d", responseCode));
    }
  }
}
