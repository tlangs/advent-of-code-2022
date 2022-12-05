package org.tlangs.question;

import org.tlangs.Utils;

import java.util.Comparator;
import java.util.stream.Stream;

public class Question2 implements Question {

  // Encoding the plays as a directed graph, using an adjacency list.
  // If a node has an edge to another node, it beats that node.
  private static final Plays[][] rules = new Plays[][] {
      {Plays.ROCK, Plays.SCISSORS},
      {Plays.SCISSORS, Plays.PAPER},
      {Plays.PAPER, Plays.ROCK}
  };

  private static final PlayComparator comparator = new PlayComparator();

  @Override
  public void answer(Stream<String> lines) {
    var totalScores =  lines.map(line -> {
      var plays = line.split(" ");
      var result1 = part1(plays[0], plays[1]);
      var result2 = part2(plays[0], plays[1]);
      return new int[] {result1, result2};
    });
    var summed = Utils.sumTwoQuestions(totalScores);
    System.out.printf("The total score after all games where I'm told what to play is [%d].%n", summed[0]);
    System.out.printf("The total score after all games where I'm told the outcome is [%d].%n", summed[1]);
  }

  private int part1(String theirPlay, String myPlay) {
    var theirs = Plays.fromCode(theirPlay);
    var mine = Plays.fromCode(myPlay);
    return calculateScore(theirs, mine);
  }

  private int part2(String theirPlay, String desiredResult) {
    var theirs = Plays.fromCode(theirPlay);
    var result = Outcomes.fromCode(desiredResult);
    var rule = getWinningRule(theirs);
    var mine = switch (result) {
      case LOSE -> rule[1];
      case DRAW -> rule[0];
      case WIN -> getLosingRule(theirs)[0];
    };
    return calculateScore(theirs, mine);
  }

  private static Plays[] getWinningRule(Plays play) {
    return getRule(play, 0);
  }

  private static Plays[] getLosingRule(Plays plays) {
    return getRule(plays, 1);
  }

  private static Plays[] getRule(Plays play, int index) {
    for (Plays[] rule : rules) {
      if (rule[index] == play) {
        return rule;
      }
    }
    throw new RuntimeException("No valid rule found for Play " + play);
  }

  private static int calculateScore(Plays theirs, Plays mine) {
    return mine.score() + (comparator.compare(mine, theirs) + 1) * 3;
  }

  static class PlayComparator implements Comparator<Plays> {
    @Override
    public int compare(Plays play1, Plays play2) {
      if (play1 == play2) {
        return 0;
      }
      var rule = getWinningRule(play1);
      if (rule[1] == play2) {
        return 1;
      } else {
        return -1;
      }
    }
  }

  // Parsing stuff down here
  enum Plays {
    ROCK, PAPER, SCISSORS;

    public static Plays fromCode(String code) {
      return switch (code) {
        case "A", "X" -> ROCK;
        case "B", "Y" -> PAPER;
        case "C", "Z" -> SCISSORS;
        default -> throw new RuntimeException("Not a valid code for a Play");
      };
    }

    public int score() {
      return this.ordinal() + 1;
    }
  }

  enum Outcomes {
    LOSE, DRAW, WIN;

    public static Outcomes fromCode(String code) {
      return switch (code) {
        case "X" -> LOSE;
        case "Y" -> DRAW;
        case "Z" -> WIN;
        default -> throw new RuntimeException("Not a valid code for an Outcome");
      };
    }
  }
}
