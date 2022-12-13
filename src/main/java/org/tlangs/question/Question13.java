package org.tlangs.question;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Question13 implements Question {
    @Override
    public void answer(Stream<String> lines) {

    }

    private Input parseList(String line) {

    }

    abstract class Input {
        public abstract List<Input> values();
    }

    class SingleValue extends Input {

        private char value;
        public SingleValue(char value) {
            this.value = value;
        }

        public List<Input> values() {
            return List.of(this);
        }

        @Override
        public String toString() {
            return Character.toString(value);
        }
    }

    class ListValue extends Input {

        private List<Input> values;

        @Override
        public List<Input> values() {
            return this.values;
        }

        @Override
        public String toString() {
            return "[" + this.values().stream().map(Input::toString).collect(Collectors.joining(", ")) + "]";
        }
    }
}
