package org.tlangs.question;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Question11 implements Question {
  @Override
  public void answer(Stream<String> lines) {
    List<List<String>> rawMonkeys = Arrays.stream(lines.collect(Collectors.joining("\n"))
            .split("\n\n"))
        .map(s -> Arrays.asList(s.split("\n")))
        .toList();

    partOne(rawMonkeys);
    partTwo(rawMonkeys);

  }

  private void partOne(List<List<String>> rawMonkeys) {
    var network = new HashMap<Integer, Monkey>();
    List<Monkey> monkeys = rawMonkeys.stream()
        .map(lines -> new Monkey(lines, (Long item) -> Math.floorDiv(item, 3), network))
        .peek(monkey -> network.put(monkey.id, monkey))
        .collect(Collectors.toList());
    for (int i = 1; i <= 20; i++) {
      playRound(monkeys, i);
    }
//    monkeys.forEach(monkey -> System.out.printf("Monkey %d inspected items %d times%n", monkey.id, monkey.itemsInspected));
    var topTwo = monkeys.stream()
        .sorted(Comparator.comparing(Monkey::getItemsInspected).reversed())
        .mapToLong(Monkey::getItemsInspected).limit(2).reduce(1, Math::multiplyExact);
    System.out.printf("The level of monkey business after 20 rounds is [%d]%n", topTwo);
  }

  private void partTwo(List<List<String>> rawMonkeys) {
    var network = new HashMap<Integer, Monkey>();
    List<Monkey> monkeys = rawMonkeys.stream()
        .map(lines -> new Monkey(lines, Function.identity(), network))
        .peek(monkey -> network.put(monkey.id, monkey))
        .collect(Collectors.toList());
    for (int i = 1; i <= 10000; i++) {
      playRound(monkeys, i);
    }
//    monkeys.forEach(monkey -> System.out.printf("Monkey %d inspected items %d times%n", monkey.id, monkey.itemsInspected));
    var topTwo = monkeys.stream()
        .sorted(Comparator.comparing(Monkey::getItemsInspected).reversed())
        .map(Monkey::getItemsInspected)
        .map(BigInteger::valueOf)
        .limit(2).reduce(BigInteger.ONE, BigInteger::multiply);
    System.out.printf("The level of monkey business after 10000 round, but less worrying, is [%d]%n", topTwo);
  }

  private void playRound(List<Monkey> monkeys, Integer round) {
    monkeys.forEach(Monkey::playKeepAway);
//    System.out.printf("After round %d, the monkeys are holding items with these worry levels:%n", round);
//    monkeys.forEach(Monkey::printState);
  }

  class Monkey {

    private static final Pattern MONKEY_ID_PATTERN = Pattern.compile("Monkey ([0-9]+):");
    private static final Pattern STARTING_ITEMS_PATTERN = Pattern.compile("Starting items: ([0-9, ]+)");
    private static final Pattern OPERATION_PATTERN = Pattern.compile("Operation: new = old ([*|+]) ([0-9|old]+)");
    private static final Pattern TEST_PATTERN = Pattern.compile("Test: divisible by ([0-9]+)");
    private static final Pattern TRUE_PATTERN = Pattern.compile("If true: throw to monkey ([0-9]+)");
    private static final Pattern FALSE_PATTERN = Pattern.compile("If false: throw to monkey ([0-9]+)");

    private final int id;
    private final Map<Integer, Monkey> network;
    private final Queue<Long> items = new LinkedList<>();
    private final Function<Long, Long> operation;
    private final Function<Long, Integer> test;
    private final Function<Long, Long> worryLevelModifier;

    private int itemsInspected;
    private Long divisor;

    public Monkey(List<String> startingState, Function<Long, Long> worryLevelModifier, Map<Integer, Monkey> network) {
      this.id = firstLong(MONKEY_ID_PATTERN.matcher(startingState.get(0))).intValue();
      this.network = network;

      this.items.addAll(parseStartingItems(startingState.get(1)));
      this.operation = parseOperation(startingState.get(2));
      this.test = parseTest(startingState.subList(3, startingState.size()));

      this.worryLevelModifier = worryLevelModifier;
    }



    public void playKeepAway() {
      var allDivisors = network.values().stream().map(m -> m.divisor).reduce(1L, Math::multiplyExact);
      while (!items.isEmpty()) {
        itemsInspected++;
        var thisItem = items.poll() % (allDivisors * 3);
        var postInspectionWorryLevel = operation.apply(thisItem);
        var didntBreakItWorryLevel = worryLevelModifier.apply(postInspectionWorryLevel);
        var monkeyToThrowTo = test.apply(didntBreakItWorryLevel);
        network.get(monkeyToThrowTo).receive(didntBreakItWorryLevel);
      }
    }

    private void receive(Long item) {
      items.offer(item);
    }

    private void printState() {
      System.out.printf("Monkey %d: %s%n",
          id, items.stream().map(Object::toString)
              .collect(Collectors.joining(", ")));
    }

    private List<Long> parseStartingItems(String line) {
      var matcher = STARTING_ITEMS_PATTERN.matcher(line);
      matcher.find();
      var itemsString = matcher.group(1);
      return Arrays.stream(itemsString.split(", ")).map(Long::parseLong).toList();
    }

    private Function<Long, Long> parseOperation(String line) {
      var matcher = OPERATION_PATTERN.matcher(line);
      matcher.find();
      var operator = matcher.group(1);
      var operand = matcher.group(2);
      BinaryOperator<Long> op =  switch (operator) {
        case "+" -> Math::addExact;
        case "*" -> Math::multiplyExact;
        default -> throw new UnsupportedOperationException("Monkeys don't support " + operator);
      };
      return switch (operand) {
        case "old" -> (Long old) -> op.apply(old, old);
        default -> (Long old) -> op.apply(old, Long.parseLong(operand));
      };
    }

    private Function<Long, Integer> parseTest(List<String> subList) {
      var divisor = firstLong(TEST_PATTERN.matcher(subList.get(0)));
      this.divisor = divisor;
      var trueMonkeyId = firstLong(TRUE_PATTERN.matcher(subList.get(1))).intValue();
      var falseMonkeyId = firstLong(FALSE_PATTERN.matcher(subList.get(2))).intValue();
      return (Long old) -> old % divisor == 0 ? trueMonkeyId : falseMonkeyId;
    }


    private Long firstLong(Matcher matcher) {
      matcher.find();
      return Long.parseLong(matcher.group(1));
    }

    public int getItemsInspected() {
      return itemsInspected;
    }
  }
}
