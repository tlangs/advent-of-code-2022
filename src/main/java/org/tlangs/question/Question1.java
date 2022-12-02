package org.tlangs.question;

import org.tlangs.question.Question;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Question1 implements Question {

  public void answer(Stream<String> lines) {
    var top3 = sumAndSortCalories(lines).limit(3).toArray(Integer[]::new);
    var sum = Arrays.stream(top3).mapToInt(Integer::intValue).sum();
    System.out.printf("The elf carrying the most calories is carrying [%d] calories.%n", top3[0]);
    System.out.printf("The 3 elves carrying the most calories are carrying a combined [%d] calories.%n", sum);
  }

  public static Stream<Integer> sumAndSortCalories(Stream<String> lines) {
   return Arrays.stream(lines.collect(Collectors.joining(",")).split(",,"))
        .map(commaSeparated -> Arrays.stream(commaSeparated.split(",")).mapToInt(Integer::parseInt).sum()).sorted(Comparator.reverseOrder());
  }
}
