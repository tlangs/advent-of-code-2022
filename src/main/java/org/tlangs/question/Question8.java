package org.tlangs.question;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Question8 implements Question {
  public Question8() {
  }

  public void answer(Stream<String> lines) {

    var linesList = lines.toList();
    var xResult = calculate(linesList);
    var transposed = transpose(linesList);
    var yResult = calculate(transposed);

    xResult.forEach(System.out::println);
    System.out.println("");
    yResult.forEach(System.out::println);
    System.out.println("");
    List<String> transposedBack = this.transpose(this.transpose(this.transpose(yResult)));
    transposedBack.forEach(System.out::println);

    System.out.println("");
    System.out.println("--------------------");

    List<String> result = this.combine(xResult, transposedBack);
    int count = result.stream()
        .mapToInt((line) -> Arrays.stream(line.split(""))
            .mapToInt((s) -> s.isBlank() ? 0 : 1)
            .sum())
        .sum();

    result.forEach(System.out::println);
    System.out.println("");
    System.out.println(count);
    System.out.println("");


    var maxScenicScore = scenicScores(linesList).stream().mapToInt(l -> l.stream().mapToInt(i -> i).max().orElse(0)).max().orElse(0);
    System.out.println(maxScenicScore);
  }

  private ArrayList<ArrayList<Integer>> scenicScores(List<String> lines) {
    var maxX = lines.get(0).length();
    var maxY = lines.size();
    var result = new ArrayList<ArrayList<Integer>>();
    for (int y = 0; y < maxY; y++) {
      var row = new ArrayList<Integer>();
      for (int x = 0; x < maxX; x++) {
        var leftScore =  lookLeft(lines, x, y);
        var rightScore = lookRight(lines, x, y);
        var downScore = lookDown(lines, x, y);
        var upScore = lookUp(lines, x, y);
        row.add(leftScore * rightScore * downScore * upScore);
      }
      result.add(row);
    }
    return result;
  }

  private int lookRight(List<String> lines, int x, int y) {
    var line = lines.get(y);
    var maxX = line.length();
    var thisTreesHeight = Integer.parseInt(line.substring(x, x + 1));
    var count = 0;
    for (int i = x + 1; i < maxX; i++) {
      var nextTreesHeight = Integer.parseInt(line.substring(i, i+1));
      count++;
      if (thisTreesHeight <= nextTreesHeight) {
        return count;
      }
    }
    return count;
  }

  private int lookLeft(List<String> lines, int x, int y) {
    var line = lines.get(y);
    var thisTreesHeight = Integer.parseInt(line.substring(x, x + 1));
    var count = 0;
    for (int i = x - 1; i >= 0; i--) {
      var nextTreesHeight = Integer.parseInt(line.substring(i, i+1));
      count++;
      if (thisTreesHeight <= nextTreesHeight) {
        return count;
      }
    }
    return count;
  }

  private int lookDown(List<String> lines, int x, int y) {
    var line = lines.get(y);
    var thisTreesHeight = Integer.parseInt(line.substring(x, x + 1));
    var maxY = lines.size();
    var count = 0;
    for (int i = y + 1; i < maxY; i++) {
      var nextTreesHeight = Integer.parseInt(lines.get(i).substring(x, x+1));
      count++;
      if (thisTreesHeight <= nextTreesHeight) {
        return count;
      }
    }
    return count;
  }

  private int lookUp(List<String> lines, int x, int y) {
    var line = lines.get(y);
    var thisTreesHeight = Integer.parseInt(line.substring(x, x + 1));
    var count = 0;
    for (int i = y - 1; i >= 0; i--) {
      var nextTreesHeight = Integer.parseInt(lines.get(i).substring(x, x+1));
      count++;
      if (thisTreesHeight <= nextTreesHeight) {
        return count;
      }
    }
    return count;
  }

  private List<String> combine(List<String> linesA, List<String> linesB) {
    int numLines = linesA.size();
    int numCols = linesA.get(0).length();
    var result = new ArrayList<String>();

    for(int i = 0; i < numLines; ++i) {
      var line = new ArrayList<String>();

      for(int j = 0; j < numCols; ++j) {
        var a = linesA.get(i).substring(j, j + 1);
        var b = linesB.get(i).substring(j, j + 1);
        if (a.isBlank() && b.isBlank()) {
          line.add(" ");
        } else if (a.isBlank() && !b.isBlank()) {
          line.add(b);
        } else if (!a.isBlank() && b.isBlank()) {
          line.add(a);
        } else {
          line.add(a);
        }
      }
      result.add(String.join("", line));
    }

    return result;
  }

  private List<String> calculate(List<String> linesList) {
    var result = new ArrayList<String>();
    result.add(linesList.get(0));
    result.addAll(linesList.subList(1, linesList.size() - 1)
        .stream()
        .map((list) -> this.keepOnlyVisible(list, (l, r) -> l > r))
        .toList());
    result.add(linesList.get(linesList.size() - 1));
    return result;
  }

  private List<String> transpose(List<String> lines) {
    int maxX = lines.size();
    int maxY = lines.get(0).length();
    var strings = IntStream.range(0, maxY)
        .mapToObj((ix) -> new ArrayList<String>()).toList();

    for(int i = 0; i < maxY; ++i) {
      String line = lines.get(i);
      for(int j = 0; j < maxX; ++j) {
        strings.get(j).add(line.substring(j, j + 1));
      }
    }

    return strings.stream().map((line) -> String.join("", line)).toList();
  }

  private String keepOnlyVisible(String line, BiPredicate<Integer, Integer> pred) {
    var array = line.split("");

    var resultFromLeft = new ArrayList<String>();
    var largestLeft = Integer.parseInt(array[0]);
    resultFromLeft.add(array[0]);

    var resultFromRight = new ArrayList<String>();
    var largestRight = Integer.parseInt(array[array.length - 1]);
    resultFromRight.add(array[array.length - 1]);

    for(int i = 1,j = array.length - 2; i < array.length; i++, --j) {
      var iTree = array[i];
      var jTree = array[j];

      if (iTree.isBlank() && jTree.isBlank()) {
        resultFromLeft.add(" ");
        resultFromRight.add(" ");
      } else if (iTree.isBlank() && !jTree.isBlank()) {
        resultFromLeft.add(" ");
        resultFromRight.add(jTree);
        largestRight = Integer.parseInt(jTree);
      } else if (!iTree.isBlank() && jTree.isBlank()) {
        resultFromLeft.add(iTree);
        resultFromRight.add(" ");
        largestLeft = Integer.parseInt(iTree);
      } else {
        var iTreeInt = Integer.parseInt(iTree);
        var jTreeInt = Integer.parseInt(jTree);
        if (i == j) {
          if (pred.test(iTreeInt, largestLeft) && pred.test(iTreeInt, largestRight)) {
            resultFromLeft.add(iTree);
            resultFromRight.add(iTree);
            largestLeft = iTreeInt;
            largestRight = iTreeInt;
          } else if (pred.test(iTreeInt, largestLeft)) {
            resultFromLeft.add(iTree);
            largestLeft = iTreeInt;
            resultFromRight.add(" ");
          } else if (pred.test(iTreeInt, largestRight)) {
            resultFromRight.add(iTree);
            largestRight = iTreeInt;
            resultFromLeft.add(" ");
          } else {
            resultFromLeft.add(" ");
            resultFromRight.add(" ");
          }
        } else {
          if (pred.test(iTreeInt, largestLeft)) {
            resultFromLeft.add(iTree);
            largestLeft = iTreeInt;
          } else {
            resultFromLeft.add(" ");
          }
          if (pred.test(jTreeInt, largestRight)) {
            resultFromRight.add(jTree);
            largestRight = jTreeInt;
          } else {
            resultFromRight.add(" ");
          }
        }
      }
    }

    Collections.reverse(resultFromRight);
    var result = new ArrayList<String>();

    for(int i = 0; i < array.length; i++) {
      var leftTree = resultFromLeft.get(i);
      var rightTree = resultFromRight.get(i);
      if (leftTree.equals(rightTree)) {
        result.add(leftTree);
      } else if (leftTree.isBlank() && rightTree.isBlank()) {
        result.add(" ");
      } else if (leftTree.isBlank() && !rightTree.isBlank()) {
        result.add(rightTree);
      } else if (!leftTree.isBlank() && rightTree.isBlank()) {
        result.add(leftTree);
      } else {
        var leftTreeInt = Integer.parseInt(leftTree);
        var rightTreeInt = Integer.parseInt(rightTree);
        if (pred.test(leftTreeInt, rightTreeInt)) {
          result.add(Integer.toString(leftTreeInt));
        } else {
          result.add(Integer.toString(rightTreeInt));
        }
      }
    }

    return String.join("", result);
  }
}
