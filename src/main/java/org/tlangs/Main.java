package org.tlangs;

import org.tlangs.question.Question;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class Main {

  private static final Pattern QUESTION_NUM_REGEX = Pattern.compile("([0-9]+)[A-Za-z]*");
  private static final Pattern INT_REGEX = Pattern.compile("[0-9]+$");

  public static void main(String[] args) throws IOException {
    var day = args[0];
    var questionPath = String.format("inputs/question%s", day);
    final Supplier<InputStream> inputStreamSupplier;
    if (INT_REGEX.matcher(day).matches()) {
      inputStreamSupplier = () -> {
        try {
          return InputDownloader.downloadInput(day);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      };
    } else {
      inputStreamSupplier = () -> ClassLoader.getSystemClassLoader().getResourceAsStream(questionPath);
    }

    try (var resourceInputStream = inputStreamSupplier.get()) {
      var inputLines = new BufferedReader(new InputStreamReader(Objects.requireNonNull(resourceInputStream))).lines();
      var question = getQuestion(day);
      question.answer(inputLines);
    }
  }

  private static Question getQuestion(String day) {
    var matcher = QUESTION_NUM_REGEX.matcher(day);
    matcher.find();
    var questionNum = matcher.group(1);
    try {
      Class<Question> aQuestion = (Class<Question>) Class.forName(String.format("org.tlangs.question.Question%s", questionNum));
      return aQuestion.getDeclaredConstructor().newInstance();
    } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
             IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(String.format("Invalid question: [%s]", questionNum), e);
    }
  }
}
