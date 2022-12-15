package org.tlangs.utils.grid;


public class GridCoordinate {

  protected final int x;
  protected final int y;
  protected final char symbol;

  public GridCoordinate(int x, int y, char symbol) {
    this.x = x;
    this.y = y;
    this.symbol = symbol;
  }

  public int y() {
    return y;
  }

  public int x() {
    return x;
  };


  public void draw(Character[][] grid, int minX, int minY) {
    var y = y() - minY;
    var x = x() - minX;
    if ((y >= 0 && y < grid.length) && (x >= 0 && x < grid[y].length)) {
      grid[y() - minY][x() - minX] = symbol;
    }
  }

  public int manhattanDistanceTo(GridCoordinate that) {
    return Math.abs(this.x() - that.x()) + Math.abs(this.y() - that.y());
  }

  public boolean isSameCoordinate(GridCoordinate that) {
    return x() == that.x() && y() == that.y();
  }

  @Override
  public String toString() {
    return String.format("(%d, %d)", x(), y());
  }
}
