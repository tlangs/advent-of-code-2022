package org.tlangs;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {

  public static <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
    return set1.stream().filter(set2::contains).collect(Collectors.toSet());
  }

  public static int[] sumTwoQuestions(Stream<int[]> ints) {
    return ints.reduce(new int[] {0, 0}, (int[] current, int[] acc) -> new int[] { current[0] + acc[0], current[1] + acc[1]});
  }
}
