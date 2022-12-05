package org.tlangs.question;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Question3 implements Question {

  @Override
  public void answer(Stream<String> lines) {
    var answers = batch(lines, 3).map(group -> {
      var groupList = group.toList();
      // Part 1
      var p1 = groupList.stream().map(this::splitRucksack)
          .map(splitted -> intersection(splitted[0], splitted[1]))
          .mapToInt(intersection -> intersection.stream().mapToInt(this::itemValue).sum())
          .sum();
      // Part 2
      var p2 = groupList.stream().map(this::stringToSetOfChar)
          .reduce(this::intersection)
          .stream().mapToInt(o -> o.stream().mapToInt(this::itemValue).sum())
          .sum();
      return new int[] { p1, p2 };
    }).reduce(new int[] {0, 0}, (int[] current, int[] acc) -> new int[] { current[0] + acc[0], current[1] + acc[1]});
    System.out.printf("The sum of all shared rucksack compartment items is [%s]%n", answers[0]);
    System.out.printf("The sum of badges carried by groups of elves is [%s]%n", answers[1]);
  }

  private Stream<Stream<String>> batch(Stream<String> lines, int batchSize) {
    AtomicInteger counter = new AtomicInteger();
    return lines.collect(Collectors.groupingBy(gr -> counter.getAndIncrement() / batchSize)).values().stream().map(List::stream);
  }


  private Set<Character>[] splitRucksack(String line) {
    var length = line.length();
    var midpoint = length / 2;
    return new Set[] {
        stringToSetOfChar(line.substring(0, midpoint)),
        stringToSetOfChar(line.substring(midpoint, length))
    };
  }

  private Set<Character> stringToSetOfChar(String string) {
    return string.chars().mapToObj(i -> (char) i).collect(Collectors.toSet());
  }

  private int itemValue(char item) {
    if (Character.isUpperCase(item)) {
      return (int) item - 38;
    } else {
      return (int) item - 96;
    }
  }

  private <T> Set<Character> intersection(Set<Character> set1, Set<Character> set2) {
    return set1.stream().filter(set2::contains).collect(Collectors.toSet());
  }
}
