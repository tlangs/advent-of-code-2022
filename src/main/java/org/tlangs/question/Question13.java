package org.tlangs.question;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Question13 implements Question {
  @Override
  public void answer(Stream<String> lines) {
    var listOfPairs = Arrays.stream(lines.collect(Collectors.joining("\n")).split("\n\n"))
        .map(s -> s.split("\n"))
        .map(arr -> List.of(
            NestedList.parseFromString(arr[0], Integer::parseInt),
            NestedList.parseFromString(arr[1], Integer::parseInt))
        ).toList();

    var comparedPackets = listOfPairs.stream().map(l -> isLeftSmaller(l.get(0), l.get(1))).toList();
    var sum = 0;
    for (int i = 0; i < comparedPackets.size(); i++) {
      var compared = comparedPackets.get(i);
      if (compared < 0) {
        sum += (i + 1);
      }
    }
    System.out.printf("The sum of indices where pairs are in order is [%d]%n", sum);
    var dividerPackets = List.of(
        NestedList.parseFromString("[[2]]", Integer::parseInt),
        NestedList.parseFromString("[[6]]", Integer::parseInt));

    var allPackets = Stream.concat(
            listOfPairs.stream()
                .flatMap(List::stream),
            dividerPackets.stream())
        .sorted(this::isLeftSmaller).toList();

    var divider1Index = allPackets.indexOf(dividerPackets.get(0)) + 1;
    var divider2Index = allPackets.indexOf(dividerPackets.get(1)) + 1;
    var decoderKey = divider1Index * divider2Index;

    System.out.printf("The decoder key for the distress signal is %d%n", decoderKey);

  }

  private <T extends Comparable<T>> int isLeftSmaller(NestedList<T> left, NestedList<T> right) {
//    System.out.printf("Compare %s vs %s%n", left, right);
    if (left instanceof SingleValue<T> leftSingleValue && right instanceof SingleValue<T> rightSingleValue) {
      return leftSingleValue.value.compareTo(rightSingleValue.value);
    }
    ListValue<T> leftListValue;
    ListValue<T> rightListValue;
    if (left instanceof SingleValue<T> leftSingleValue) {
      leftListValue = new ListValue<>(leftSingleValue);
//      System.out.printf("Mixed types; convert left to %s and retry comparison%n", leftListValue);
    } else {
      leftListValue = (ListValue<T>) left;
    }
    if (right instanceof SingleValue<T> rightSingleValue) {
      rightListValue = new ListValue<>(rightSingleValue);
//      System.out.printf("Mixed types; convert right to %s and retry comparison%n", rightListValue);
    } else {
      rightListValue = (ListValue<T>) right;
    }
    return isLeftListSmaller(leftListValue, rightListValue);
  }

  private <T extends Comparable<T>> int isLeftListSmaller(ListValue<T> leftListValue, ListValue<T> rightListValue) {

    for (int i = 0; i < leftListValue.values().size(); i++) {
      if (rightListValue.values().size() <= i) {
//        System.out.println("- Right side ran out of items, so inputs are not in the right order");
        return 1;
      } else {
        var leftValue = leftListValue.values().get(i);
        var rightValue = rightListValue.values().get(i);
        var compared = isLeftSmaller(leftValue, rightValue);
        if (compared > 0) {
//          System.out.println("- Right side is smaller, so inputs are not in the right order");
          return compared;
        }
        if (compared < 0) {
//          System.out.println("- Left side is smaller, so inputs are in the right order");
          return compared;
        }
      }
    }
    if (leftListValue.values().size() < rightListValue.values().size()) {
//      System.out.println("- Left side ran out of items, so inputs are in the right order");
      return -1;
    }
    return 0;
  }

  static abstract class NestedList<T extends Comparable<T>> {
    public abstract List<NestedList<T>> values();

    public static <T extends Comparable<T>> NestedList<T> parseFromString(String line, Function<String, T> elementParser) {
      if (line.startsWith("[")) {
        return parseList(elementsOfList(line), elementParser);
      } else {
        return new SingleValue<>(line, elementParser);
      }
    }

    public static <T extends Comparable<T>> ListValue<T> parseList(String line, Function<String, T> elementParser) {
      ListValue<T> result = new ListValue<>();
      if (line.isBlank()) {
        return result;
      }
      var head = line;
      String tail;

      while (head != null) {
        if (head.startsWith("[")) {
          var element = parseList(elementsOfList(head), elementParser);
          result.addElement(element);
          var indexOfComma = head.indexOf(",", element.toString().length() - 1);
          if (indexOfComma == -1) {
            tail = null;
          } else {
            tail = head.substring(indexOfComma + 1);
          }
        } else {
          var element = NestedList.parseElement(head.split(",")[0], elementParser);
          result.addElement(element);
          var indexOfComma = head.indexOf(",", element.toString().length() - 1);
          if (indexOfComma == -1) {
            tail = null;
          } else {
            tail = head.substring(indexOfComma + 1);
          }
        }
        head = tail;
      }
      return result;
    }

    public static <T extends Comparable<T>> NestedList<T> parseElement(String element, Function<String, T> elementParser) {
      if (element.startsWith("[")) {
        return parseList(element, elementParser);
      } else {
        return new SingleValue<>(element, elementParser);
      }
    }

    public static String elementsOfList(String string) {
      if (string.equals("[]")) {
        return "";
      }
      int openings = 0;
      var array = string.toCharArray();
      for (int i = 0; i < array.length; i++) {
        var c = array[i];
        if (c == ']') {
          openings--;
          if (openings == 0) {
            return string.substring(1, i);
          }
        }
        if (c == '[') {
          openings++;
        }
      }
      throw new RuntimeException("Could not find closing of " + string);
    }
  }

  static class ListValue<T extends Comparable<T>> extends NestedList<T> {

    private List<NestedList<T>> values = new ArrayList<>();


    public ListValue() {
    }

    public ListValue(SingleValue<T> singleValue) {
      this.values.add(singleValue);
    }

    @Override
    public List<NestedList<T>> values() {
      return this.values;
    }

    public void addElement(NestedList<T> element) {
      this.values.add(element);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ListValue<?> listValue = (ListValue<?>) o;
      return values.equals(listValue.values);
    }

    @Override
    public int hashCode() {
      return Objects.hash(values);
    }

    @Override
    public String toString() {
      return "[" + this.values().stream().map(NestedList::toString).collect(Collectors.joining(",")) + "]";
    }
  }

  static class SingleValue<T extends Comparable<T>> extends NestedList<T> {

    private T value;

    public SingleValue(String element, Function<String, T> elementParser) {
      this.value = elementParser.apply(element);
    }

    public List<NestedList<T>> values() {
      return List.of(this);
    }

    @Override
    public String toString() {
      return value.toString();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      SingleValue<?> that = (SingleValue<?>) o;
      return value.equals(that.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(value);
    }
  }
}
