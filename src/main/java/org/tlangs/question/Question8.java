package org.tlangs.question;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class Question8 implements Question {
  public void answer(Stream<String> lines) {
    var linesList = lines.toList();

    var visibleFromEdge = runComputation(linesList,
        this::isVisibleFromEdge,
        this::combineVisibleFromEdge);

    int numVisible = visibleFromEdge.stream()
        .mapToInt(line -> line.stream()
            .mapToInt((s) -> s.isBlank() ? 0 : 1)
            .sum())
        .sum();

    var scenicScores = runComputation(linesList,
        this::scenicScoreToEdge,
        this::combineScenicScores);

    var maxScenicScore = scenicScores.stream().mapToInt(l -> l.stream().mapToInt(Integer::parseInt).max().orElse(0)).max().orElse(0);

    System.out.printf("The number of trees visible from outside the grid is [%s]%n", numVisible);
    System.out.printf("[%s] is the max scenic score%n", maxScenicScore);
  }


  @FunctionalInterface
  interface Combiner {
    String apply(String fromLeft, String fromRight, String fromTop, String fromBottom);
  }

  private String isVisibleFromEdge(String thisOne, List<String> toTheEdge) {
    var thisInt = Integer.parseInt(thisOne);
    if (toTheEdge.stream().mapToInt(Integer::parseInt).anyMatch(i -> i >= thisInt)) {
      return " ";
    } else {
      return thisOne;
    }
  }

  private String combineVisibleFromEdge(String fromLeft, String fromRight, String fromTop, String fromBottom) {
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

  private String combineScenicScores(String fromLeft, String fromRight, String fromTop, String fromBottom) {
    return Stream.of(fromLeft, fromRight, fromTop, fromBottom)
        .mapToInt(Integer::parseInt)
        .reduce(Math::multiplyExact)
        .stream().mapToObj(Integer::toString)
        .findFirst()
        .orElseThrow();
  }


  private ArrayList<ArrayList<String>> runComputation(List<String> lines,
                                                      BiFunction<String, List<String>, String> compute,
                                                      Combiner combiner) {
    var maxX = lines.get(0).length();
    var maxY = lines.size();
    var result = new ArrayList<ArrayList<String>>();
    for (int y = 0; y < maxY; y++) {
      var row = new ArrayList<String>();
      for (int x = 0; x < maxX; x++) {
        var thisOne = lines.get(y).substring(x, x + 1);
        var toTheLeft =  compute.apply(thisOne, lookLeft(lines, x, y));
        var toTheRight = compute.apply(thisOne, lookRight(lines, x, y));
        var toTheTop = compute.apply(thisOne, lookUp(lines, x, y));
        var toTheBottom = compute.apply(thisOne, lookDown(lines, x, y));
        var combined = combiner.apply(toTheLeft, toTheRight, toTheTop, toTheBottom);
        row.add(combined);
      }
      result.add(row);
    }
    return result;
  }

  private List<String> lookLeft(List<String> lines, int x, int y) {
    var line = lines.get(y);
    var result = new ArrayList<String>();
    for (int i = x - 1; i >= 0; i--) {
      result.add(line.substring(i, i+1));
    }
    return result;
  }

  private List<String> lookRight(List<String> lines, int x, int y) {
    var line = lines.get(y);
    var result = new ArrayList<String>();
    for (int i = x + 1; i < line.length(); i++) {
      result.add(line.substring(i, i+1));
    }
    return result;
  }

  private List<String> lookUp(List<String> lines, int x, int y) {
    var result = new ArrayList<String>();
    for (int i = y - 1; i >= 0; i--) {
      result.add(lines.get(i).substring(x, x+1));
    }
    return result;
  }

  private List<String> lookDown(List<String> lines, int x, int y) {
    var result = new ArrayList<String>();
    for (int i = y + 1; i < lines.size(); i++) {
      result.add(lines.get(i).substring(x, x+1));
    }
    return result;
  }
}
