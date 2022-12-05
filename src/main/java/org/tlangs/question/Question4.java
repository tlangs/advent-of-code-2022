package org.tlangs.question;

import org.tlangs.Utils;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Question4 implements Question {
  @Override
  public void answer(Stream<String> lines) {
    var answers = lines.map(this::parse).map(sets -> new int[] {part1(sets), part2(sets)});
    var summed = Utils.sumTwoQuestions(answers);
    System.out.printf("There are [%s] groups of elves where one is completely redundant%n", summed[0]);
    System.out.printf("There are [%s] groups of elves where work is being duplicated%n", summed[1]);
  }

  public int part1(Set<Integer>[] sets) {
    if (sets[0].containsAll(sets[1]) || sets[1].containsAll(sets[0])) {
      return 1;
    } else {
      return 0;
    }
  }

  public int part2(Set<Integer>[] sets) {
    if (Utils.intersection(sets[0], sets[1]).isEmpty()) {
      return 0;
    } else {
      return 1;
    }
  }

  private Set<Integer>[] parse(String line) {
    var splitted = line.split(",");
    return new Set[] { parseSet(splitted[0]), parseSet(splitted[1]) };
  }

  private Set<Integer> parseSet(String unparsed) {
    var splitted = unparsed.split("-");
    var lower = splitted[0];
    var higher = splitted[1];
    return IntStream.range(Integer.parseInt(lower), Integer.parseInt(higher) + 1).boxed().collect(Collectors.toSet());
  }
}
