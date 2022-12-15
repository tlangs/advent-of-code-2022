package org.tlangs.question;

import org.tlangs.utils.grid.TwoDimensionalArrayUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Question8 implements Question {
  public void answer(Stream<String> lines) {
    var linesList = lines.map(line -> line.split("")).toArray(String[][]::new);

    var visibleFromEdge = TwoDimensionalArrayUtils.runComputation(linesList,
        this::isVisibleFromEdge,
        this::combineVisibleFromEdge,
        Optional.empty());

    int numVisible = visibleFromEdge.stream()
        .mapToInt(line -> line.stream()
            .mapToInt((s) -> s.isBlank() ? 0 : 1)
            .sum())
        .sum();

    var scenicScores = TwoDimensionalArrayUtils.runComputation(linesList,
        this::scenicScoreToEdge,
        this::combineScenicScores,
        Optional.empty());

    var maxScenicScore = scenicScores.stream().mapToInt(l -> l.stream().mapToInt(Integer::parseInt).max().orElse(0)).max().orElse(0);

    System.out.printf("The number of trees visible from outside the grid is [%s]%n", numVisible);
    System.out.printf("[%s] is the max scenic score%n", maxScenicScore);
  }

  private String isVisibleFromEdge(String thisOne, List<String> toTheEdge) {
    var thisInt = Integer.parseInt(thisOne);
    if (toTheEdge.stream().mapToInt(Integer::parseInt).anyMatch(i -> i >= thisInt)) {
      return " ";
    } else {
      return thisOne;
    }
  }

  private String combineVisibleFromEdge(String thisOne, String fromLeft, String fromRight, String fromTop, String fromBottom) {
    return Stream.of(fromLeft, fromRight, fromTop, fromBottom)
        .filter(s -> !s.isBlank())
        .findFirst()
        .orElse(" ");
  }

  private String scenicScoreToEdge(String thisOne, List<String> toTheEdge) {
    if (toTheEdge.isEmpty()) {
      return "0";
    } else {
      var count = 0;
      for (var thatOne : toTheEdge) {
        var thisInt = Integer.parseInt(thisOne);
        var thatInt = Integer.parseInt(thatOne);
        count++;
        if (thisInt <= thatInt) {
          return Integer.toString(count);
        }
      }
      return Integer.toString(count);
    }
  }

  private String combineScenicScores(String thisOne, String fromLeft, String fromRight, String fromTop, String fromBottom) {
    return Stream.of(fromLeft, fromRight, fromTop, fromBottom)
        .mapToInt(Integer::parseInt)
        .reduce(Math::multiplyExact)
        .stream().mapToObj(Integer::toString)
        .findFirst()
        .orElseThrow();
  }

}
