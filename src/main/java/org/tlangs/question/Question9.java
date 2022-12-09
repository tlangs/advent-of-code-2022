package org.tlangs.question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Question9 implements Question {

  @Override
  public void answer(Stream<String> lines) {
    var ropeWithTwoKnots = new Rope(2);
    var ropeWithTenKnots = new Rope(10);
    lines.forEach(line -> {
      ropeWithTwoKnots.pullRope(line);
      ropeWithTenKnots.pullRope(line);
    });

    System.out.printf("The the tail for the rope with two knots visits [%s] positions%n", ropeWithTwoKnots.visitedByTail.size());
    System.out.printf("The the tail for the rope with ten knots visits [%s] positions%n", ropeWithTenKnots.visitedByTail.size());
  }

  private void visualizeTails(Rope rope) {

    var minX = rope.visitedByTail.stream().mapToInt(Coordinates::x).min().orElseThrow();
    var maxX = rope.visitedByTail.stream().mapToInt(Coordinates::x).max().orElseThrow();
    var minY = rope.visitedByTail.stream().mapToInt(Coordinates::y).min().orElseThrow();
    var maxY = rope.visitedByTail.stream().mapToInt(Coordinates::y).max().orElseThrow();
    var xOffset = minX >= 0 ? 0 : Math.abs(minX);
    var yOffset = minY >= 0 ? 0 : Math.abs(minY);

    var plotPoints = rope.visitedByTail;

    var grid = IntStream.range(0, maxY + yOffset + 2)
        .mapToObj(y -> new ArrayList<>(IntStream.range(0, maxX + xOffset + 2).mapToObj(x -> "-").toList()))
        .collect(Collectors.toCollection(ArrayList::new));
    for (int y = minY; y < maxY + 1; y++) {
      var row = grid.get(y + yOffset);
      for (int x = minX; x < maxX + 1; x++) {
        var position = new Coordinates(x, y);
        if (plotPoints.contains(position)) {
          row.set(x + xOffset, "#");
        }
      }
    }
    Collections.reverse(grid);
    grid.forEach(System.out::println);
    System.out.println("-----------------------------------");
  }


  static class Rope {

    private Coordinates headPosition;
    private List<Coordinates> tailPositions = new ArrayList<>();

    private Set<Coordinates> visitedByTail = new HashSet<>();

    public Rope(int numKnots) {
      this.headPosition = new Coordinates(0, 0);
      for (int i = 0; i < numKnots - 1; i++) {
        tailPositions.add(new Coordinates(0, 0));
      }
      this.visitedByTail.add(tailPositions.get(tailPositions.size() - 1));
    }

    public void pullRope(String move) {
      var splitted = move.split(" ");
      var direction = splitted[0];
      var amount = Integer.parseInt(splitted[1]);
      for (int i = amount; i > 0; i--) {
        headPosition = switch (direction) {
          case "L":
            yield headPosition.moveHorizontal(-1);
          case "R":
            yield headPosition.moveHorizontal(1);
          case "D":
            yield headPosition.moveVertical(-1);
          case "U":
            yield headPosition.moveVertical(1);
          case default:
            throw new RuntimeException(String.format("No direction found for move [%s]", move));
        };
        updateTails();
      }
    }

    private void updateTails() {
      var parentPosition = headPosition;
      for (int i = 0; i < tailPositions.size(); i++) {
        var thisTail = tailPositions.get(i);
        tailPositions.set(i, thisTail.moveTailToBeAdjacent(parentPosition));
        parentPosition = tailPositions.get(i);
      }
      visitedByTail.add(tailPositions.get(tailPositions.size() - 1));
    }

    public void printGrid() {
      var minX = Stream.concat(Stream.of(headPosition), tailPositions.stream()).mapToInt(Coordinates::x).min().orElseThrow();
      var maxX = Stream.concat(Stream.of(headPosition), tailPositions.stream()).mapToInt(Coordinates::x).max().orElseThrow();
      var minY = Stream.concat(Stream.of(headPosition), tailPositions.stream()).mapToInt(Coordinates::y).min().orElseThrow();
      var maxY = Stream.concat(Stream.of(headPosition), tailPositions.stream()).mapToInt(Coordinates::y).max().orElseThrow();
      var xOffset = minX >= 0 ? 0 : Math.abs(minX);
      var yOffset = minY >= 0 ? 0 : Math.abs(minY);

      var grid = IntStream.range(0, maxY + yOffset + 1)
          .mapToObj(y -> new ArrayList<>(IntStream.range(0, maxX + xOffset + 1).mapToObj(x -> "-").toList()))
          .collect(Collectors.toCollection(ArrayList::new));

      for (int i = tailPositions.size() - 1; i >= 0; i--) {
        var thisTailPosition = tailPositions.get(i);
        grid.get(thisTailPosition.y() + yOffset).set(thisTailPosition.x() + xOffset, Integer.toString(i + 1));
      }
      grid.get(headPosition.y() + yOffset).set(headPosition.x() + xOffset, "H");

      Collections.reverse(grid);
      grid.forEach(System.out::println);
      System.out.println("--------------------------------------------");
    }
  }

  record Coordinates(int x, int y) {

    public Coordinates moveHorizontal(int units) {
      return new Coordinates(x + units, y);
    }

    public Coordinates moveVertical(int units) {
      return new Coordinates(x, y + units);
    }

    public boolean isAdjacentTo(Coordinates otherPosition) {
      var horizontal = Math.abs(x - otherPosition.x());
      var vertical = Math.abs(y - otherPosition.y());
      return horizontal <= 1 && vertical <= 1;
    }

    private Coordinates moveTailToBeAdjacent(Coordinates otherPosition) {
      if (isAdjacentTo(otherPosition)) {
        return new Coordinates(x, y);
      } else {
        var deltaX = otherPosition.x - x;
        var horizontalUnits = Math.abs(deltaX);
        var deltaY = otherPosition.y - y;
        var verticalUnits = Math.abs(deltaY);
        if (horizontalUnits > 1 && verticalUnits == 0) {
          // Need to move horizontally
          return deltaX < 0 ? this.moveHorizontal(-1) : this.moveHorizontal(1);
        } else if (horizontalUnits == 0 && verticalUnits > 1) {
          // Need to move vertically
          return deltaY < 0 ? this.moveVertical(-1) : this.moveVertical(1);
        } else {
          // Need to move diagonally
          var newX = x + (deltaX < 0 ? -1 : 1);
          var newY = y + (deltaY < 0 ? -1 : 1);
          return new Coordinates(newX, newY);
        }
      }
    }
  }
}
