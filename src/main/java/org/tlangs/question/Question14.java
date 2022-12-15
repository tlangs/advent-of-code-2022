package org.tlangs.question;

import org.tlangs.utils.grid.TwoDimensionalArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Question14 implements Question {

  @Override
  public void answer(Stream<String> lines) {
    var rockPaths = lines.map(Path::new).toList();
    var cave = new Cave(rockPaths);
    var grainsOfSand = cave.fill();
//    cave.visualize();
    System.out.printf("[%d] grains of sand come to rest before sand starts flowing into the abyss below%n", grainsOfSand);

    var rockPathsWithFloor = addFloor(rockPaths);
    var caveWithFloor = new Cave(rockPathsWithFloor);
    var grainsOfSandWithFloor = caveWithFloor.fill();
//    caveWithFloor.visualize();
    System.out.printf("With a floor in the cave, [%d] grains of sand come to rest%n", grainsOfSandWithFloor);
  }

  private List<Path> addFloor(List<Path> rockPaths) {
    var minX = minX(rockPaths);
    var maxX = maxX(rockPaths);
    var maxY = maxY(rockPaths);
    var floorY = maxY + 2;
    var floorMinX = minX - floorY;
    var floorMaxX = maxX + floorY;
    var pathString = String.format("%s,%s -> %s,%s", floorMinX, floorY, floorMaxX, floorY);
    var floor = new Path(pathString);
    var newPaths = new ArrayList<>(rockPaths);
    newPaths.add(floor);
    return newPaths;
  }

  private int minX(List<Path> rockPaths) {
    return rockPaths.stream().mapToInt(Path::minX).min().orElseThrow();
  }

  private int maxX(List<Path> rockPaths) {
    return rockPaths.stream().mapToInt(Path::maxX).max().orElseThrow();
  }

  private int minY(List<Path> rockPaths) {
    return rockPaths.stream().mapToInt(Path::minY).max().orElseThrow();
  }

  private int maxY(List<Path> rockPaths) {
    return rockPaths.stream().mapToInt(Path::maxY).max().orElseThrow();
  }

  class Cave {

    public static Position sandStartingPosition = new Position(500, 0);
    private final List<Path> rockPaths = new ArrayList<>();
    private final List<SandParticle> sandParticles = new ArrayList<>();
    private final Set<Position> allPositions = new HashSet<>();
    public final int minX;
    public final int maxX;
    private final int minY;
    public final int maxY;
    public Cave(List<Path> rockPaths) {

      this.rockPaths.addAll(rockPaths);

      this.allPositions.addAll(rockPaths.stream().flatMap(p -> p.positions.stream()).toList());

      this.minX = minX(rockPaths);
      this.maxX = maxX(rockPaths);
      this.minY = minY(rockPaths);
      this.maxY = maxY(rockPaths);
    }

    private int fill() {
      int i = 0;
      var sandParticle = new SandParticle(sandStartingPosition);
      while (!sandParticle.willFall(Stream.concat(allPositions.stream(), sandParticles.stream().map(s -> s.position)).collect(Collectors.toSet()), maxY)) {
        allPositions.add(sandParticle.position);
        sandParticles.add(sandParticle);
        sandParticle = new SandParticle(sandStartingPosition);
        if (isFull()) {
          allPositions.add(sandParticle.position);
          sandParticles.add(sandParticle);
          return sandParticles.size();
        }
        if (i % 100 == 0) {
          pruneAllPositions();
        }
        i++;
      }
      return this.sandParticles.size();
    }

    private void pruneAllPositions() {
      var rockPathPositions = rockPaths.stream().flatMap(p -> p.positions.stream()).collect(Collectors.toSet());
      for (var sandParticle : sandParticles) {
        var thisPosition = sandParticle.position;
        var above = new Position(thisPosition.x, thisPosition.y - 1);
        if (allPositions.contains(above) && !rockPathPositions.contains(above)) {
          allPositions.remove(thisPosition);
        }
      }
      System.out.println(allPositions.size());
    }

    private boolean isFull() {
      return !new SandParticle(sandStartingPosition).canMove(allPositions);
    }

    private void visualize() {

      var array = new Character[maxY + 1][maxX - minX + 1];

      for (int y = 0; y <= maxY; y++) {
        var row = array[y];
        for (int x = minX; x <= maxX; x++) {
          array[y][x - minX] = '.';
        }
      }

      rockPaths.forEach(p -> p.draw(array, minX));
      sandParticles.forEach(s -> s.draw(array, minX));

      array[0][500 - minX] = '+';
      TwoDimensionalArrayUtils.printGrid(array, minX, minY, List.of(minX, maxX, 500));
    }
  }

  static class SandParticle {

    private Position position;

    public SandParticle(Position position) {
      this.position = position;
    }

    public boolean willFall(Set<Position> allOtherPositions, int maxY) {
      while (canMove(allOtherPositions)) {
        move(allOtherPositions);
        if (position.y > maxY) {
          return true;
        }
      }
      return false;
    }

    public boolean canMove(Set<Position> allPositions) {
      var positionsBelow = positionsBelow(allPositions);
      return canMoveDown(positionsBelow) ||
          canMoveDiagonalLeft(positionsBelow) ||
          canMoveDiagonalRight(positionsBelow);
    }

    public boolean move(Set<Position> allPositions) {
      var positionsBelow = positionsBelow(allPositions);
      var canMoveDown = canMoveDown(positionsBelow);
      if (canMoveDown) {
        moveAllTheWayDown(allPositions);
        return true;
      }
      var canMoveDiagonalLeft = canMoveDiagonalLeft(positionsBelow);
      if (canMoveDiagonalLeft) {
        this.position = new Position(position.x - 1, position.y + 1);
        return true;
      }
      var canMoveDiagonalRight = canMoveDiagonalRight(positionsBelow);
      if (canMoveDiagonalRight) {
        this.position = new Position(position.x + 1, position.y + 1);
        return true;
      }
      return false;
    }

    private List<Position> positionsBelow(Set<Position> allPositions) {
      return allPositions.stream()
          .filter(p -> p.x() >= position.x - 1 && p.x() <= position.x + 1)
          .filter(p -> p.y() == position.y + 1)
          .toList();
    }

    private void moveAllTheWayDown(Set<Position> allPositions) {
      var everythingBelow  = allPositions.stream()
          .filter(p -> p.x == position.x)
          .filter(p -> p.y > position.y)
          .min(Comparator.comparing(Position::y));
      var yValue = everythingBelow.map(Position::y).map(i -> i - 1).orElse(position.y + 1);
      this.position = new Position(position.x, yValue);
    }

    private boolean canMoveDown(List<Position> positionsBelow) {
      return positionsBelow.stream()
          .filter(p -> p.x() == position.x)
          .filter(p -> p.y() == position.y + 1)
          .findFirst()
          .isEmpty();
    }

    private boolean canMoveDiagonalLeft(List<Position> positionsBelow) {
      return positionsBelow.stream()
          .filter(p -> p.x() == position.x - 1)
          .filter(p -> p.y() == position.y + 1)
          .findFirst()
          .isEmpty();
    }

    private boolean canMoveDiagonalRight(List<Position> positionsBelow) {
      return positionsBelow.stream()
          .filter(p -> p.x() == position.x + 1)
          .filter(p -> p.y() == position.y + 1)
          .findFirst()
          .isEmpty();
    }

    public void draw(Character[][] array, int gridMinX) {
      var y = position.y;
      var x = position.x - gridMinX;
      array[y][x] = 'o';
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

    public int minY() {
      return positions.stream()
          .min(Comparator.comparing(Position::y))
          .map(Position::y)
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
          .map(a -> new Integer[]{Integer.parseInt(a[0]), Integer.parseInt(a[1])})
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

  record Position(int x, int y) {
  }

}
