package org.tlangs.question;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Question13 implements Question {
  @Override
  public void answer(Stream<String> lines) {
    lines.filter(s -> !s.isBlank()).map(s -> NestedList.parseFromString(s, Integer::parseInt)).forEach(System.out::println);
  }

  static abstract class NestedList<T> {
    public abstract List<NestedList<T>> values();

    public static <T> NestedList<T> parseFromString(String line, Function<String, T> elementParser) {
      if (line.startsWith("[")) {
        return parseList(elementsOfList(line), elementParser);
      } else {
        return new SingleValue<>(line, elementParser);
      }
    }

    public static <T> ListValue<T> parseList(String line, Function<String, T> elementParser) {
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

    public static <T> NestedList<T> parseElement(String element, Function<String, T> elementParser) {
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

  static class ListValue<T> extends NestedList<T> {

    private List<NestedList<T>> values = new ArrayList<>();


    public ListValue() {

    }

    @Override
    public List<NestedList<T>> values() {
      return this.values;
    }

    public void addElement(NestedList<T> element) {
      this.values.add(element);
    }

    @Override
    public String toString() {
      return "[" + this.values().stream().map(NestedList::toString).collect(Collectors.joining(",")) + "]";
    }
  }

  static class SingleValue<T> extends NestedList<T> {

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
  }
}
