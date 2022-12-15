package org.tlangs.utils.grid;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class TwoDimensionalArrayUtils {

  @FunctionalInterface
  public interface Combiner<T, U, V> {
    V apply(T thisOne, U fromLeft, U fromRight, U fromTop, U fromBottom);
  }

  public static <T, U, V> ArrayList<ArrayList<V>> runComputation(T[][] lines,
                                                            BiFunction<T, List<T>, U> directionReducer,
                                                            Combiner<T, U, V> combiner,
                                                            Optional<Integer> localizedLimit) {
    var maxX = lines[0].length;
    var maxY = lines.length;
    var result = new ArrayList<ArrayList<V>>();
    for (int y = 0; y < maxY; y++) {
      var row = new ArrayList<V>();
      for (int x = 0; x < maxX; x++) {
        var thisOne = lines[y][x];
        var toTheLeft =  directionReducer.apply(thisOne, lookLeft(lines, x, y, localizedLimit));
        var toTheRight = directionReducer.apply(thisOne, lookRight(lines, x, y, localizedLimit));
        var toTheTop = directionReducer.apply(thisOne, lookUp(lines, x, y, localizedLimit));
        var toTheBottom = directionReducer.apply(thisOne, lookDown(lines, x, y, localizedLimit));
        var combined = combiner.apply(thisOne, toTheLeft, toTheRight, toTheTop, toTheBottom);
        row.add(combined);
      }
      result.add(row);
    }
    return result;
  }

  private static <T> List<T> lookLeft(T[][] lines, int x, int y, Optional<Integer> localizedLimit) {
    var line = lines[y];
    var result = new ArrayList<T>();
    for (int i = x - 1; i >= localizedLimit.map(l -> Math.max((x - 1) - l, 0)).orElse(0); i--) {
      result.add(line[i]);
    }
    return result;
  }

  private static <T> List<T> lookRight(T[][] lines, int x, int y, Optional<Integer> localizedLimit) {
    var line = lines[y];
    var result = new ArrayList<T>();
    for (int i = x + 1; i < localizedLimit.map(l -> Math.min((x + 1 + l), line.length)).orElse(line.length); i++) {
      result.add(line[i]);
    }
    return result;
  }

  private static <T> List<T> lookUp(T[][] lines, int x, int y, Optional<Integer> localizedLimit) {
    var result = new ArrayList<T>();
    for (int i = y - 1; i >= localizedLimit.map(l -> Math.max(((y - 1) - l), 0)).orElse(0); i--) {
      result.add(lines[i][x]);
    }
    return result;
  }

  private static <T> List<T> lookDown(T[][] lines, int x, int y, Optional<Integer> localizedLimit) {
    var result = new ArrayList<T>();
    for (int i = y + 1; i < localizedLimit.map(l -> Math.min((y + 1 + l), lines.length)).orElse(lines.length); i++) {
      result.add(lines[i][x]);
    }
    return result;
  }

  public static Character[][] makeGrid(int minX, int maxX, int minY, int maxY, char fillChar) {
    var deltaY = maxY - minY;
    var deltaX = maxX - minX;
    var array = new Character[deltaY + 1][deltaX + 1];
    for (int y = minY; y <= maxY; y++) {
      var row = array[y - minY];
      for (int x = minX; x <= maxX; x++) {
        array[y-minY][x - minX] = fillChar;
      }
    }
    return array;
  }

  public static void printGrid(Character[][] grid, int minX, int minY, List<Integer> xAxisLabels) {
    var xOffset = Integer.toString(grid.length).length() + 1;
    var labelHeight = xAxisLabels.stream().mapToInt(l -> l.toString().length()).max().orElse(0);
    // Labels must be 3-digit integers
    for (int y = 0; y < labelHeight; y++) {
      for (int x = 0; x < grid[0].length + xOffset; x++) {
        var printedLabel = false;
        for (int label : xAxisLabels) {
          if (x - xOffset == label - minX) {
            var labelString = Integer.toString(label);
            if (labelString.length() > y) {
              System.out.printf(labelString.substring(y, y + 1));
              printedLabel = true;
            }
          }
        }
        if (!printedLabel) {
          System.out.printf(" ");
        }
      }
      System.out.println();
    }


    for (int y = 0; y < grid.length; y++) {
      var row = grid[y];
      var yLabel = Integer.toString(y + minY);
      while (yLabel.length() < xOffset) {
        yLabel = yLabel + " ";
      }
      System.out.printf("%s", yLabel);
      for (int x = 0; x < row.length; x++) {
        System.out.printf(row[x].toString());
      }
      System.out.println();
    }
  }
}
