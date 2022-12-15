package org.tlangs.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.stream.IntStream;

public class IntArrayUtils {

  public static List<int[]> condenseRanges(List<int[]> ranges) {
    var sortedRanges = new ArrayList<>(ranges);
    sortedRanges.sort(Comparator.comparing(a -> a[0]));
    var result = new Stack<int[]>();
    for (int[] range : sortedRanges) {
      if (result.isEmpty()) {
        result.push(range);
        continue;
      }
      var lastRange = result.pop();
      if (range[0] < lastRange[0]) {
        throw new RuntimeException("Sorted ranges were not sorted!");
      }
      if (range[0] >= lastRange[0] && range[1] <= lastRange[1]) {
        // range is enclosed by lastRange
        result.push(lastRange);
        continue;
      }
      if (range[0] >= lastRange[0] && range[0] <= lastRange[1] + 1 && range[1] >= lastRange[1]) {
        // range extends lastRange
        result.push(new int[]{lastRange[0], range[1]});
        // range is distinct from lastRange
      } else {
        result.push(lastRange);
        result.push(range);
      }
    }
    return result;
  }


  public static IntStream intStreamExcludingRanges(List<int[]> ranges, int startInclusive, int endExclusive) {
    var queue = new LinkedList<>(condenseRanges(ranges));
    return IntStream.iterate(startInclusive - 1, (int i) -> i < endExclusive, (int i) -> {
      var peeked = queue.peek();
      if (i + 1 < peeked[0]) {
        return i + 1;
      } else {
        queue.poll();
        return peeked[1] + 1;
      }
    }).skip(1);
  }
}
