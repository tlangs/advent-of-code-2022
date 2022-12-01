package org.tlangs;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Main {
  public static void main(String[] args) throws URISyntaxException, IOException {
    var day = args[0];
    var question = getQuestion(day);
    question.answer(getInputLines(day));
  }

  private static Question getQuestion(String day) {
    return switch (day) {
      case "1" -> new Question1();
      default -> throw new RuntimeException(String.format("Invalid day: [%s]", day));
    };
  }

  private static Stream<String> getInputLines(String day) throws URISyntaxException, IOException {
    var classLoader = ClassLoader.getSystemClassLoader();
    return Files.lines(Paths.get(classLoader.getResource("inputs/question" + day).toURI()));
  }
}