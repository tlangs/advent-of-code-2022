package org.tlangs.question;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Question7 implements Question {

  private final int TOTAL_SPACE = 70000000;
  private final int SPACE_NEEDED_FOR_UPDATE = 30000000;
  @Override
  public void answer(Stream<String> lines) {
    var tokens = lines.map(Question7::parse).toList();

    FilesystemDir fileSystem = null;
    FilesystemDir currentNode = null;
    for (var token : tokens) {
      switch (token) {
        case Cd c -> {
          if (fileSystem == null) {
            fileSystem = new FilesystemDir(c.dirName(), null);
            currentNode = fileSystem;
          }
          else {
            currentNode = currentNode.changeDir(c);
          }
        }
        case Ls l -> {}
        case Dir d -> currentNode.addDir(d);
        case File f -> currentNode.addFile(f);
        default -> throw new IllegalStateException("Unexpected value: " + token);
      }
    }

    Set<FilesystemDir> smallDirs = new HashSet<>();
    Set<FilesystemDir> bigDirs = new HashSet<>();

    var usedSize = fileSystem.size();
    var availableSize = TOTAL_SPACE - usedSize;
    var needToDelete = SPACE_NEEDED_FOR_UPDATE - availableSize;

    fileSystem.traverse((FilesystemDir dir) -> dir.size() < 100000, smallDirs);
    System.out.printf("The dirs with sizes less than 100000 are: [%s]%n", smallDirs.stream().map(FilesystemDir::name).collect(Collectors.joining(", ")));
    System.out.printf("Their combined size is: [%d]%n", smallDirs.stream().mapToInt(FilesystemDir::size).sum());

    fileSystem.traverse((FilesystemDir dir) -> dir.size() >= needToDelete, bigDirs);
    var smallestDirToDelete = bigDirs.stream().min(Comparator.comparingInt(FilesystemDir::size)).orElseThrow();

    System.out.printf("Need to delete dir [%s] with size [%d] for update.%n", smallestDirToDelete.name(), smallestDirToDelete.size());

  }


  private static Token parse(String line) {
    if (line.startsWith("$ ")) {
      var command = line.substring(2).split(" ");
      return switch (command[0]) {
        case "cd" -> new Cd(command[1]);
        case "ls" -> new Ls();
        case default -> throw new RuntimeException(String.format("Unknown command: [%s]", command[0]));
      };
    } else {
      var result = line.split(" ");
      return switch (result[0]) {
        case "dir" -> new Dir(result[1]);
        case default -> new File(Integer.parseInt(result[0]), result[1]);
      };
    }
  }

  static class FilesystemDir {
    private final Map<String, FilesystemDir> dirs = new HashMap<>();
    private final List<FilesystemFile> files = new ArrayList<>();
    private final String dirName;
    private final FilesystemDir parentDir;
    private Integer size;


    public FilesystemDir(String dirName, FilesystemDir parentDir) {
      this.dirName = dirName;
      this.parentDir = parentDir;
    }

    public String name() {
      return this.dirName;
    }

    public int size() {
      if (this.size == null) {
        this.size = this.dirs.values().stream().mapToInt(FilesystemDir::size).sum() + this.files.stream().mapToInt(FilesystemFile::size).sum();
      }
      return this.size;
    }

    public void traverse(Predicate<FilesystemDir> predicate, Collection<FilesystemDir> col) {
      if (predicate.test(this)) {
        col.add(this);
      }
      this.dirs.values().forEach(filesystemDir -> filesystemDir.traverse(predicate, col));
    }
    public void addDir(Dir dir) {
        this.dirs.put(dir.dirName(), new FilesystemDir(dir.dirName, this));
    }

    public FilesystemDir changeDir(Cd cd) {
      if (cd.dirName().equals("..")) {
        return this.parentDir;
      }
      return this.dirs.get(cd.dirName());
    }

    public void addFile(File file) {
      this.files.add(new FilesystemFile(file.size(), file.name()));
    }

    public List<FilesystemFile> listFiles() {
      return this.files;
    }
  }

  record FilesystemFile(int size, String fileName) {};

  interface Token {};
  interface Command {};
  interface Result {};
  record Cd(String dirName) implements Command, Token {}
  record Ls() implements Command, Token {}
  record Dir(String dirName) implements Result, Token {}
  record File(int size, String name) implements Result, Token {}


}
