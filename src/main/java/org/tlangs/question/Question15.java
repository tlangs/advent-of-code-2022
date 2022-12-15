package org.tlangs.question;

import org.tlangs.utils.grid.GridCoordinate;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.tlangs.utils.IntArrayUtils.intStreamExcludingRanges;

public class Question15 implements Question {

  private static Pattern COORDINATE_PATTERN = Pattern.compile("x=([-0-9]+), y=([-0-9]+)");

  @Override
  public void answer(Stream<String> lines) {
    List<Sensor> sensors = parseLines(lines);

    var largestDistance = sensors.stream().mapToInt(s -> s.manhattanDistanceTo(s.closestBeacon)).max().orElse(0);

    var partOneMinX = sensors.stream().mapToInt(s -> Math.min(s.x(), s.closestBeacon.x())).min().orElse(0) - largestDistance;
    var partOneMaxX = sensors.stream().mapToInt(s -> Math.max(s.x(), s.closestBeacon.x())).max().orElse(0) + largestDistance;

    var row = 2000000;
    var count = IntStream.range(partOneMinX, partOneMaxX)
        .mapToObj(i -> new Point(i, row))
        .filter(p -> sensors.stream().anyMatch(s -> s.cannotHaveUnseenBeaconAt(p))).count();
    System.out.printf("Line %d has [%d] positions where there cannot be a beacon%n", row, count);

    int partTwoMax = 4000000;

    Optional<Point> missingPoint = IntStream.range(0, partTwoMax)
        .mapToObj(y -> {
          var ranges = sensors.stream()
              .map(s -> s.horizontalViewBoundaries(y))
              .filter(a -> a.length > 0).toList();
          return intStreamExcludingRanges(ranges, 0, partTwoMax).mapToObj(x -> new Point(x, y));
        }).flatMap(Function.identity())
        .findFirst();

    var tuningFrequency = missingPoint.map(p -> (p.x() * 4000000L) + p.y());
    System.out.printf("The tuning frequency of the undetected beacon is %d%n", tuningFrequency.orElse(0L));
  }

  private List<Sensor> parseLines(Stream<String> lines) {
    return lines.map(line -> {
      var splitted = line.split(": ");
      var beaconMatcher = COORDINATE_PATTERN.matcher(splitted[1]);
      beaconMatcher.find();
      var beacon = new Beacon(Integer.parseInt(beaconMatcher.group(1)), Integer.parseInt(beaconMatcher.group(2)));

      var sensorMatcher = COORDINATE_PATTERN.matcher(splitted[0]);
      sensorMatcher.find();
      return new Sensor(
          Integer.parseInt(sensorMatcher.group(1)),
          Integer.parseInt(sensorMatcher.group(2)),
          beacon);
    }).toList();
  }

  static final class Point extends GridCoordinate {
    public Point(int x, int y) {
      super(x, y, ' ');
    }
  }

  static final class Beacon extends GridCoordinate {

    Beacon(int x, int y) {
      super(x, y, 'B');
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) return true;
      if (obj == null || obj.getClass() != this.getClass()) return false;
      var that = (Beacon) obj;
      return this.isSameCoordinate(that);
    }
  }

  static final class Sensor extends GridCoordinate {
    private final Beacon closestBeacon;

    Sensor(int x, int y, Beacon closestBeacon) {
      super(x, y,'S');
      this.closestBeacon = closestBeacon;
    }

    @Override
    public void draw(Character[][] grid, int minX, int minY) {
      super.draw(grid, minX, minY);
      closestBeacon.draw(grid, minX, minY);
    }

    public int[] horizontalViewBoundaries(int row) {
      var viewDistance = manhattanDistanceTo(closestBeacon);
      if (row < y - viewDistance || row > y + viewDistance) {
        return new int[]{};
      }
      var vertical = Math.abs(row - y);
      var horizontal = viewDistance - vertical;
      return new int[] { x - horizontal, x + horizontal };
    }

    public void drawRanges(Character[][] grid, int minX, int minY) {
      var maxDistance = this.manhattanDistanceTo(closestBeacon);
      Set<GridCoordinate> sensorViews = new HashSet<>();
      for (int viewY = y - maxDistance - 1; viewY < y + maxDistance + 1; viewY++) {
        for (int viewX = x - maxDistance - 1; viewX < x + maxDistance + 1; viewX++) {
          var view = new GridCoordinate(viewX, viewY, '#');
          if (this.manhattanDistanceTo(view) <= maxDistance) {
            sensorViews.add(view);
          }
        }
      }
      sensorViews.forEach(s -> s.draw(grid, minX, minY));
    }

    public boolean pointInRange(Point point) {
      return this.manhattanDistanceTo(point) <= this.manhattanDistanceTo(closestBeacon);
    }

    public boolean cannotHaveUnseenBeaconAt(Point point) {
      return this.pointInRange(point) && !(this.closestBeacon.isSameCoordinate(point) || this.isSameCoordinate(point));
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) return true;
      if (obj == null || obj.getClass() != this.getClass()) return false;
      var that = (Sensor) obj;
      return this.isSameCoordinate(that) &&
          Objects.equals(this.closestBeacon, that.closestBeacon);
    }

    @Override
    public int hashCode() {
      return Objects.hash(x, y, closestBeacon);
    }
  }
}
