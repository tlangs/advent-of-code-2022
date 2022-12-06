package org.tlangs.question;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Question6 implements Question {
  @Override
  public void answer(Stream<String> lines) {
    var line = lines.findFirst().orElseThrow();
    var startOfPacket = nextIndexAfterNRepeatingChars(line, 4);
    var startOfMessage = nextIndexAfterNRepeatingChars(line, 14);
    System.out.printf("The first marker for start-of-packet is after character [%s]%n", startOfPacket);
    System.out.printf("The first marker for start-of-message is after character [%s]%n", startOfMessage);
  }

  private int nextIndexAfterNRepeatingChars(String line, int n) {
    var nextIndex = 0;
    for (int i = 0, j = n; j < line.length(); i++, j++) {
      var window = line.substring(i, j);
      // Yeah, its kinda expensive, but oh well.
      if (window.chars().boxed().collect(Collectors.toSet()).size() == n) {
        nextIndex = j;
        break;
      }
    }
    return nextIndex;
  }
}
