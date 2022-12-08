package org.tlangs;

import org.tlangs.question.Question;
import org.tlangs.question.Question1;
import org.tlangs.question.Question2;
import org.tlangs.question.Question3;
import org.tlangs.question.Question4;
import org.tlangs.question.Question5;
import org.tlangs.question.Question6;
import org.tlangs.question.Question7;
import org.tlangs.question.Question8;

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
    return switch (day.substring(0, 1)) {
      case "1" -> new Question1();
      case "2" -> new Question2();
      case "3" -> new Question3();
      case "4" -> new Question4();
      case "5" -> new Question5();
      case "6" -> new Question6();
      case "7" -> new Question7();
      case "8" -> new Question8();
      default -> throw new RuntimeException(String.format("Invalid day: [%s]", day));
    };
  }
}
