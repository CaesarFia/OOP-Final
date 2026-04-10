package oop.project.library.input;

import java.util.List;
import java.util.Map;

public record BasicArgs(
    List<String> positional,
    Map<String, String> named
) {}
