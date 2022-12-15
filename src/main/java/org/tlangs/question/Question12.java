package org.tlangs.question;

import org.tlangs.utils.grid.TwoDimensionalArrayUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Question12 implements Question {

  @Override
  public void answer(Stream<String> lines) {
    var linesList = lines.toList();
    var maxX = linesList.stream().max(Comparator.comparing(String::length)).orElse("").length();
    var maxY = linesList.size();

    var partOneAdjacencies = parseAdjacencies(linesList, this::canMovePart1);
    var partOneAdjacencyLists = buildAdjacencyLists(partOneAdjacencies);

    Predicate<Position> partOneStartFinder = (Position p) -> p.adjacency.elevation == 'S';
    Predicate<Position> partOneEndFinder = (Position p) -> p.adjacency.elevation == 'E';

    var partOnePath = search(
        partOneAdjacencyLists,
        partOneStartFinder,
        partOneEndFinder);

    System.out.printf("The shortest path from the start to the position with the best signal is [%d], visualized below:%n", partOnePath.size() - 1);
    visualize(partOnePath, maxX, maxY, partOneStartFinder, partOneEndFinder);
    System.out.println();


    var partTwoAdjacencies = parseAdjacencies(linesList, this::canMovePart2);
    var partTwoAdjacencyLists = buildAdjacencyLists(partTwoAdjacencies);
    Predicate<Position> partTwoStartFinder = (Position p) -> p.adjacency.elevation == 'E';
    Predicate<Position> partTwoEndFinder = (Position p) -> p.adjacency.elevation == 'a';

    var partTwoPath = search(
        partTwoAdjacencyLists,
        partTwoStartFinder,
        partTwoEndFinder);

    System.out.printf("The shortest path from the lowest elevation to the position with the best signal is [%d], visualized below:%n", partTwoPath.size() - 1);
    visualize(partTwoPath, maxX, maxY, partTwoStartFinder, partTwoEndFinder);
  }

  private void visualize(ArrayList<Position> path, int maxX, int maxY, Predicate<Position> startFinder, Predicate<Position> endFinder) {
    char[][] lines = new char[maxY][maxX];
    for (int y = 0; y < maxY; y++) {
      for (int x = 0; x < maxX; x++) {
        lines[y][x] = '.';
      }
    }

    for (int i = 0; i < path.size(); i++) {
      var position = path.get(i);
      if (endFinder.test(position)) {
        lines[position.y][position.x] = 'E';
        break;
      }
      var symbol = startFinder.test(position) ? 'S' : Moves.moveBetweenTwo(path.get(i + 1), position).arrow;
      lines[position.y][position.x] = symbol;
    }

    for (char[] line : lines) {
      for (char column : line) {
        System.out.print(Character.valueOf(column).toString());
      }
      System.out.println();
    }
  }

  public ArrayList<Position> search(Map<Position, ArrayList<Position>> lists,
                                    Predicate<Position> startFinder,
                                    Predicate<Position> endFinder) {
    var start = lists.entrySet().stream()
        .filter(e -> startFinder.test(e.getKey()))
        .findFirst().orElseThrow().getKey();

    var visited = new HashSet<Position>();
    Map<Position, ArrayList<Position>> leadingPositionToPathsFromStart = new HashMap<>();
    leadingPositionToPathsFromStart.put(start, new ArrayList<>());
    var adjacentPositions = new LinkedList<Position>();
    adjacentPositions.offer(start);
    while (!adjacentPositions.isEmpty()) {
      var thisPosition = adjacentPositions.poll();
      if (visited.contains(thisPosition)) {
        continue;
      }
      for (Position adjacentToThis : lists.get(thisPosition)) {
        var pathSoFar = leadingPositionToPathsFromStart.get(thisPosition);
        var newPath = new ArrayList<>(pathSoFar);
        newPath.add(thisPosition);
        if (endFinder.test(thisPosition)) {
          return newPath;
        } else {
          leadingPositionToPathsFromStart.put(adjacentToThis, newPath);
          adjacentPositions.offer(adjacentToThis);
        }
      }
      leadingPositionToPathsFromStart.remove(thisPosition);
      visited.add(thisPosition);
    }
    return new ArrayList<>();
  }

  private Map<Position, ArrayList<Position>> buildAdjacencyLists(ArrayList<ArrayList<Adjacency>> adjacencies) {
    var result = new HashMap<Position, ArrayList<Position>>();
    for (int y = 0; y < adjacencies.size(); y++) {
      var line = adjacencies.get(y);
      for (int x = 0; x < line.size(); x++) {
        var node = new Position(x, y, line.get(x));
        result.put(node, new ArrayList<>());
      }
    }
    var positions = result.keySet().stream().collect(Collectors.toMap(Function.identity(), Function.identity()));
    for (var thisPosition : result.keySet()) {
      thisPosition.adjacency.possibleMoves.stream()
          .map(m -> positions.get(new Position(thisPosition.x + m.x, thisPosition.y + m.y, null)))
          .forEach(p -> result.get(thisPosition).add(p));
    }

    return result;
  }

  record Position(int x, int y, Adjacency adjacency) {
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Position node = (Position) o;
      return x == node.x && y == node.y;
    }

    @Override
    public int hashCode() {
      return Objects.hash(x, y);
    }
  }

  private ArrayList<ArrayList<Adjacency>> parseAdjacencies(List<String> lines,
                                                           BiFunction<Character, List<Character>, Boolean> edgeFinder) {
    var linesArray = lines.stream().map(s -> s.chars().mapToObj(c -> (char) c).toArray(Character[]::new)).toArray(Character[][]::new);
    return TwoDimensionalArrayUtils.runComputation(linesArray, edgeFinder, this::createAdjacency, Optional.of(1));
  }

  private Boolean canMovePart1(Character thisOne, List<Character> list) {
    var thisChar = heightChar(thisOne);
    if (list.isEmpty()) {
      return false;
    }
    var thatChar = heightChar(list.get(0));
    return thisChar >= thatChar - 1;
  }

  private Boolean canMovePart2(Character thisOne, List<Character> list) {
    var thisChar = heightChar(thisOne);
    if (list.isEmpty()) {
      return false;
    }
    var thatChar = heightChar(list.get(0));
    return thisChar <= thatChar + 1;
  }

  private Character heightChar(Character thisOne) {
    if (thisOne == 'S') {
      return 'a';
    }
    if (thisOne == 'E') {
      return 'z';
    }
    return thisOne;
  }

  private Adjacency createAdjacency(Character thisOne, Boolean fromLeft, Boolean fromRight, Boolean fromTop, Boolean fromBottom) {
    var moves = new ArrayList<Moves>();
    if (fromLeft) {
      moves.add(Moves.LEFT);
    }
    if (fromRight) {
      moves.add(Moves.RIGHT);
    }
    if (fromTop) {
      moves.add(Moves.UP);
    }
    if (fromBottom) {
      moves.add(Moves.DOWN);
    }
    return new Adjacency(thisOne, moves);
  }

  record Adjacency(Character elevation, List<Moves> possibleMoves) {
  }

  enum Moves {
    LEFT(-1, 0, '<'),
    RIGHT(1, 0, '>'),
    UP(0, -1, '^'),
    DOWN(0, 1, 'v');


    private int x;
    private int y;

    private char arrow;

    Moves(int x, int y, char arrow) {
      this.x = x;
      this.y = y;
      this.arrow = arrow;
    }

    public static Moves moveBetweenTwo(Position a, Position b) {
      var deltaX = a.x - b.x;
      var deltaY = a.y - b.y;
      if ((deltaX != 0 && deltaY != 0) || deltaX == deltaY) {
        throw new RuntimeException("Cannot get move for non-adjacent positions");
      }
      if (deltaX == 0) {
        return deltaY < 0 ? UP : DOWN;
      } else {
        return deltaX > 0 ? RIGHT : LEFT;
      }
    }
  }
}
