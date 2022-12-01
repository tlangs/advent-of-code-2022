package org.example;

import java.util.stream.Stream;

public interface Question {
  void answer(Stream<String> lines);
}
