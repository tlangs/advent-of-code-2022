package org.tlangs.question;

import org.tlangs.utils.TwoDimensionalArrayUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Question12 implements Question {

  @Override
  public void answer(Stream<String> lines) {
    var linesList = lines.toList();
    ArrayList<ArrayList<Adjacency>> adjacencies = parseAdjacencies(linesList);
    var lists = buildAdjacencyLists(adjacencies);

    var maxX = adjacencies.stream().max(Comparator.comparing(ArrayList::size)).get().size();
    var maxY = adjacencies.size();

    var path = search(lists);

    visualize(path, maxX, maxY);
    System.out.println(path.size() - 1);

  }

  private void visualize(ArrayList<Position> path, int maxX, int maxY) {
    char[][] lines = new char[maxY][maxX];
    for (int y = 0; y < maxY; y++) {
      for (int x = 0; x < maxX; x++) {
        lines[y][x] = '.';
      }
    }

    for (int i = 0; i < path.size(); i++) {
      var position = path.get(i);
      if (position.isEnd()) {
        lines[position.y][position.x] = 'E';
        break;
      }
      var symbol = position.isStart() ? 'S' : Moves.moveBetweenTwo(path.get(i + 1), position).arrow;
      lines[position.y][position.x] = symbol;
    }

    for (char[] line : lines) {
      for (char column : line) {
        System.out.print(Character.valueOf(column).toString());
      }
      System.out.println();
    }
    System.out.println(IntStream.range(0, maxX).mapToObj(i -> "-").collect(Collectors.joining()));
  }

  public ArrayList<Position> search(Map<Position, ArrayList<Position>> lists) {
    var start = lists.entrySet().stream().filter(e -> e.getKey().isStart()).findFirst().orElseThrow().getKey();

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
        if (thisPosition.isEnd()) {
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

    public Boolean isStart() {
      return adjacency.elevation == 'E';
    }

    public Boolean isEnd() {
      return adjacency.elevation == 'a';
    }

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

  private ArrayList<ArrayList<Adjacency>> parseAdjacencies(List<String> lines) {
    var linesArray = lines.stream().map(s -> s.chars().mapToObj(c -> (char) c).toArray(Character[]::new)).toArray(Character[][]::new);
    return TwoDimensionalArrayUtils.runComputation(linesArray, this::canMove, this::createAdjacency, Optional.of(1));
  }

  private Boolean canMove(Character thisOne, List<Character> list) {
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
