package org.tlangs.question;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Question5 implements Question {

  private static final Pattern MOVE_REGEX = Pattern.compile("move ([0-9]+) from ([0-9]+) to ([0-9]+)");
  @Override
  public void answer(Stream<String> lines) {
    var linesList = lines.toList();
    var foo = linesList.stream().takeWhile(l -> !l.isBlank()).toList();
    var crane9000 = new State(foo);
    var crane9001 = new State(foo);
    linesList.stream()
        .dropWhile(line -> !line.isBlank())
        .filter(line -> !line.isBlank())
        .forEach(line -> {
          var matcher = MOVE_REGEX.matcher(line);
          matcher.find();
          crane9000.execute9000(matcher.group(1), matcher.group(2), matcher.group(3));
          crane9001.execute9001(matcher.group(1), matcher.group(2), matcher.group(3));
        });
    System.out.printf("The top of the stacks reads [%s]%n with Crane 9000%n", crane9000.topOfStacks());
    System.out.printf("The top of the stacks reads [%s]%n with Crane 9001%n", crane9001.topOfStacks());

  }

  class State {
    private Map<String, Stack<String>> stacks = new HashMap<>();

    public State(List<String> lines) {
      var lastLine = lines.get(lines.size() - 1);
      var inputStacks = lastLine.trim().split("\\s+");
      for (String stack : inputStacks) {
        var index = lastLine.indexOf(stack);
        var contents = new Stack<Character>();
        for (String line : lines.subList(0, lines.size() - 1)) {
          if (line.length() >= index && !Character.isSpaceChar(line.charAt(index))) {
            contents.push(line.charAt(index));
          }
        }
        var thisStack = new Stack<String>();
        while (!contents.empty()) {
          thisStack.push(String.valueOf(contents.pop()));
        }
        stacks.put(stack, thisStack);

      }
    }

    public void execute9000(String count, String from, String to) {
      var fromStack = stacks.get(from);
      var toStack = stacks.get(to);
      var intCount = Integer.parseInt(count);
      for (int i = 0; i < intCount; i++) {
        toStack.push(fromStack.pop());
      }
    }

    public void execute9001(String count, String from, String to) {
      var fromStack = stacks.get(from);
      var toStack = stacks.get(to);
      var intCount = Integer.parseInt(count);
      var tmpStack = new Stack<String>();
      for (int i = 0; i < intCount; i++) {
        tmpStack.push(fromStack.pop());
      }
      while (!tmpStack.empty()) {
        toStack.push(tmpStack.pop());
      }
    }

    public String topOfStacks() {
      return this.stacks.values().stream().map(Stack::peek).collect(Collectors.joining());
    }
  }
}
