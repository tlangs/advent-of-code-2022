package org.tlangs.question;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Question14 implements Question {

  @Override
  public void answer(Stream<String> lines) {
    var list = lines.map(Path::new).toList();
    vizualize(list);
  }


  private void vizualize(List<Path> paths) {
    var minX = paths.stream().mapToInt(Path::minX).min().orElseThrow();
    var maxX = paths.stream().mapToInt(Path::maxX).max().orElseThrow();
    var maxY = paths.stream().mapToInt(Path::maxY).max().orElseThrow();
    var array = new Character[maxY + 1][maxX - minX + 1];

    for (int y = 0; y <= maxY; y++) {
      var row = array[y];
      for (int x = minX; x <= maxX; x++) {
        array[y][x - minX] = '.';
      }
    }

    paths.forEach(p -> p.draw(array, minX));

    array[0][500 - minX] = '+';
    printGrid(array, minX, maxX);
  }

  private void printGrid(Character[][] grid, int minX, int maxX) {

    var offset = Integer.toString(grid.length).length() + 1;
    // Labels must be 3-digit integers
    for (int y = 0; y < 3; y++) {
      for (int x = 0; x < grid[0].length + offset; x++) {
        var printedLabel = false;
        for (int label : List.of(minX, maxX, 500)) {
          if (x - offset == label - minX) {
            var labelString = Integer.toString(label);
            System.out.printf(labelString.substring(y, y + 1));
            printedLabel = true;
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
      var yLabel = Integer.toString(y);
      while (yLabel.length() < offset) {
        yLabel = yLabel + " ";
      }
      System.out.printf("%s", yLabel);
      for (int x = 0; x < row.length; x++) {
        System.out.printf(row[x].toString());
      }
      System.out.println();
    }
  }


  class Path {

    private List<Position> positions;
    public Path(String line) {
      this.positions = getPositionsFromVertices(parseVertices(line));
    }

    public void draw(Character[][] array, int gridMinX) {
      for (var position : positions) {
        var y = position.y;
        var x = position.x - gridMinX;
        array[y][x] = '#';
      }
    }

    public int minX() {
      return positions.stream()
          .min(Comparator.comparing(Position::x))
          .map(Position::x)
          .orElseThrow();
    }

    public int maxX() {
      return positions.stream()
          .max(Comparator.comparing(Position::x))
          .map(Position::x)
          .orElseThrow();
    }

    public int maxY() {
      return positions.stream()
          .max(Comparator.comparing(Position::y))
          .map(Position::y)
          .orElseThrow();
    }

    private List<Integer[]> parseVertices(String line) {
      return Arrays.stream(line.split(" -> "))
          .map(s -> s.split(","))
          .map(a -> new Integer[] {Integer.parseInt(a[0]), Integer.parseInt(a[1])})
          .toList();
    }

    private List<Position> getPositionsFromVertices(List<Integer[]> vertices) {
      Stack<Position> positions = new Stack<>();
      for (Integer[] vertex : vertices) {
        var thisPosition = new Position(vertex[0], vertex[1]);
        if (positions.isEmpty()) {
          positions.push(thisPosition);
        } else {
          var previousPosition = positions.peek();
          var line = pointsBetween(previousPosition, thisPosition);
          line.forEach(positions::push);
          positions.push(thisPosition);
        }
      }
      return positions.stream().toList();
    }

    private List<Position> pointsBetween(Position from, Position to) {
      var deltaX = to.x - from.x;
      var deltaY = to.y - from.y;
      List<Position> result = new ArrayList<>();

      if (deltaX != 0 && deltaY != 0) {
        throw new RuntimeException("Cannot draw line between two points not in same row or column");
      }
      if (deltaX != 0) {
        int start = deltaX < 0 ? from.x - 1 : from.x + 1;
        IntUnaryOperator iter = deltaX < 0 ? this::sub1 : this::add1;
        IntStream.iterate(start, iter)
            .limit(Math.abs(deltaX) - 1)
            .mapToObj(i -> new Position(i, from.y))
            .forEach(result::add);
      } else {
        int start = deltaY < 0 ? from.y - 1 : from.y + 1;
        IntUnaryOperator iter = deltaY < 0 ? this::sub1 : this::add1;
        IntStream.iterate(start, iter)
            .mapToObj(i -> new Position(from.x, i))
            .limit(Math.abs(deltaY) - 1)
            .forEach(result::add);
      }
      return result;
    }

    int add1(int x) {
      return x + 1;
    }

    int sub1(int x) {
      return x - 1;
    }
  }



  record Position(int x, int y) {}
}
