package org.tlangs.question;

import java.util.stream.Stream;

public interface Question {
  void answer(Stream<String> lines);
}
