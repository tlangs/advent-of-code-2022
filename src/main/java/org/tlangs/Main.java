package org.tlangs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class Main {
  public static void main(String[] args) throws IOException {
    var day = args[0];
    var questionPath = String.format("inputs/question%s", day);
    try (var resourceInputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(questionPath)) {
      var inputLines = new BufferedReader(new InputStreamReader(Objects.requireNonNull(resourceInputStream))).lines();
      var question = getQuestion(day);
      question.answer(inputLines);
    }
  }

  private static Question getQuestion(String day) {
    return switch (day) {
      case "1" -> new Question1();
      default -> throw new RuntimeException(String.format("Invalid day: [%s]", day));
    };
  }
}