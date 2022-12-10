package org.tlangs.question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Question10 implements Question {

  @Override
  public void answer(Stream<String> lines) {
    var cpu = new CPU(lines.map(this::parse).collect(Collectors.toCollection(ArrayList::new)));
    var valuesAtCycles = cpu.run();
    System.out.println(cpu);

    System.out.println("Part 1: ");
    var sumPart1 = 0;
    for (int i = 20; i < valuesAtCycles.size(); i = i + 40) {
      var valueAtCycle = i * valuesAtCycles.get(i - 1);
      sumPart1 += valueAtCycle;
      System.out.printf("\tThe value at cycle [%d] is [%d]%n", i, valueAtCycle);
    }
    System.out.printf("The total sum of the selected cycles is [%d]%n", sumPart1);

    draw(valuesAtCycles);
  }

  private Instruction parse(String line) {
    var splitted = line.split(" ");
    if (splitted.length == 1) {
      return new Instruction(1, 0);
    } else {
      return new Instruction(2, Integer.parseInt(splitted[1]));
    }
  }

  private void draw(List<Integer> valuesAtCycles) {
    for (int y = 0; y < 6; y++) {
      for (int x = 0; x < 40; x++) {
        var cycle = (y * 40) + x;
        var xRegister = valuesAtCycles.get(cycle);
        System.out.print((xRegister >= x - 1 && xRegister <= x + 1) ? "⬛️" : "⬜️");
      }
      System.out.println();
    }
  }

  class CPU {
    private int xRegister;
    private List<Instruction> currentInstructions = new ArrayList<>();
    private final Stack<Instruction> instructions = new Stack<>();

    private final List<Integer> valuesAtCycles = new ArrayList<>();

    public CPU(List<Instruction> instructions) {
      Collections.reverse(instructions);
      this.instructions.addAll(instructions);
      this.xRegister = 1;
    }

    public List<Integer> run() {
      while (!this.instructions.isEmpty() || !currentInstructions.isEmpty()) {
        tick();
      }
      tick();
      return this.valuesAtCycles;
    }

    private void tick() {
      if (!instructions.isEmpty() && currentInstructions.size() != 1) {
        currentInstructions.add(instructions.pop());
      }

      valuesAtCycles.add(xRegister);

      var ticked = this.currentInstructions.stream()
          .map(Instruction::decrement)
          .toList();
      var valueToAdd = ticked.stream().filter(Instruction::isComplete).mapToInt(Instruction::result).sum();
      xRegister += valueToAdd;

      this.currentInstructions = ticked.stream()
          .filter(i -> !i.isComplete()).collect(Collectors.toCollection(ArrayList::new));
    }

  }

  record Instruction(int cyclesLeft, int result) {

    public boolean isComplete() {
      return cyclesLeft == 0;
    }

    public Instruction decrement() {
      if (cyclesLeft == 0) {
        throw new RuntimeException("Cannot decrement an instruction's remaining cycles below 0");
      } else {
        return new Instruction(cyclesLeft - 1, result);
      }
    }
  }
}
