package oop.project.library.scenarios;

import oop.project.library.input.Input;

import java.util.Map;

public final class Scenarios {

    public static Map<String, Object> parse(String command) {
        // Hacky extraction of base command, but necessary without subcommands.
        var index = command.indexOf(" ");
        var base = index != -1 ? command.substring(0, index) : command;
        var arguments = index != -1 ? " ".repeat(base.length()) + command.substring(index) : ""; //maintain index
        return switch (base) {
            case "input" -> input(arguments);
            case "add" -> ArgumentScenarios.add(arguments);
            case "sub" -> ArgumentScenarios.sub(arguments);
            case "fizzbuzz" -> ArgumentScenarios.fizzbuzz(arguments);
            case "difficulty" -> ArgumentScenarios.difficulty(arguments);
            case "date" -> ArgumentScenarios.date(arguments);
            case "mul" -> CommandScenarios.mul(arguments);
            case "div" -> CommandScenarios.div(arguments);
            case "echo" -> CommandScenarios.echo(arguments);
            case "search" -> CommandScenarios.search(arguments);
            case "dispatch" -> CommandScenarios.dispatch(arguments);
            default -> throw new AssertionError(base);
        };
    }

    private static Map<String, Object> input(String arguments) {
        try {
            return Map.of("arguments", new Input(arguments).parseBasicArgs());
        } catch (RuntimeException e) {
            throw new RuntimeException("Invalid input: " + e.getMessage(), e);
        }
    }

}
